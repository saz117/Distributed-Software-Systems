from flask import Flask
from flask_restful import Resource, Api, reqparse
from sqlalchemy import UniqueConstraint

from models.accounts import auth
from models.artist import ArtistModel
from models.show import ShowModel
from lock import lock

app = Flask(__name__)
api = Api(app)


class ShowArtistsList(Resource):

    def get(self, show_id, artist_id):
        with lock.lock:
            # coger show
            show = ShowModel.find_by_id(show_id)
            artist = ArtistModel.find_by_id(artist_id)
            if artist in show.artists:
                return artist.json()
            return {"message": "No está el artista asociado al espectaculo."}, 500

    def get(self, show_id):
        with lock.lock:
            # coger show
            show = ShowModel.find_by_id(show_id)
            if not show:
                return {"message": "No está el artista asociado al espectaculo."}, 500
            artists = show.artists
            return {'artists': [x.json() for x in artists]}

    @auth.login_required(role='admin')
    def post(self, show_id, artist_json, artist_id=None):
        with lock.lock:
            # caso1
            if artist_id != None:
                artist = ArtistModel.find_by_id(artist_id)
                if artist.id != artist_json['id']:
                    return {"message": "No coinciden los id de los artistas."}, 500
                return {'artist': artist}, 200

            # caso2
            artist = ArtistModel.query.filter_by(name=artist_json['name']).first()
            if artist == None:
                return {"message": "No se ha encontrado el artista."}, 500

            show = ShowModel.find_by_id(show_id)
            show.artists.append(artist)
            try:
                show.save_to_db()
            except:
                return {"message": "An error occurred modifying the show."}, 500
            return {'show': show}, 200

    @auth.login_required(role='admin')
    def delete(self, show_id, artist_id):
        with lock.lock:
            show = ShowModel.find_by_id(show_id)
            artist = ArtistModel.find_by_id(artist_id)
            if artist in show.artists:
                show.artists.remove(artist)
                return show.artists
            return {"message": "No está el artista asociado al espectaculo. No se ha posido borrar."}, 500
