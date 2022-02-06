from flask import Flask
from flask_restful import Resource, Api, reqparse
from sqlalchemy import UniqueConstraint
from models.accounts import auth
from models.artist import ArtistModel, DisciplineModel
from models.show import ShowModel
from lock import lock

app = Flask(__name__)
api = Api(app)


class ShowArtist(Resource):
    def get(self, id_show, id_artist):
        with lock.lock:
            #coger show
            show = ShowModel.find_by_id(id_show)
            if show is None:
                return {"message": "An error occurred searching the show."}, 500
            artists = show.artists
            artist = ArtistModel.find_by_id(id_artist)
            if artist is None:
                return {"message": "An error occurred searching the artist."}, 500
            if artist not in artists:
                return {"message": "An error occurred the artist not in show."}, 500
            return {'artist': artist.json()}

    @auth.login_required(role='admin')
    def post(self, id_show):
        with lock.lock:
            #coger show
            show = ShowModel.find_by_id(id_show)
            if show is None:
                return {"message": "An error occurred searching the show."}, 500
            # Coger los datos que vienen del request
            parser = reqparse.RequestParser()  # create parameters parser from request
            # define all input parameters need and its type
            parser.add_argument('name', type=str, required=True, help="This field cannot be left blanck")
            parser.add_argument('country', type=str, required=True, help="This field cannot be left blanck")
            parser.add_argument('genre', type=str, help="This field cannot be left blanck")
            parser.add_argument('disciplines', type=str, help="This field cannot be left blanck",
                                action="append")  # action = "append" is needed to determine that is a list of strings
            # Comprbamos que todos los parametros esten el body del request
            data = parser.parse_args()
            # new_artist2.discipline.append(new_discipline1)
            artist = ArtistModel.query.filter_by(name=data['name']).first()
            if artist is None:
                artist = ArtistModel(data['name'], data['country'], data['genre'])
                discpl = data['disciplines']
                if discpl is not None:
                    for x in discpl:
                        # buscar la disciplia y despues añadirla
                        dis = DisciplineModel.query.filter_by(name=x).first()
                        artist.discipline.append(dis)
                try:
                    artist.save_to_db()
                except:
                    return {"message": "An error occurred inserting the artist."}, 500
            show.artists.append(artist)
            show.save_to_db()
            return {'show': show.json()}, 200

    @auth.login_required(role='admin')
    def delete(self, id_show, id_artist):
        with lock.lock:
            show = ShowModel.find_by_id(id_show)
            if show is None:
                return {"message": "An error occurred searching the show."}, 500
            artist = ArtistModel.find_by_id(id_artist)
            if artist is None:
                return {"message": "An error occurred searching the artist."}, 500
            show.artists.remove(artist)
            show.save_to_db()
            return {'show': show.json()}, 200


