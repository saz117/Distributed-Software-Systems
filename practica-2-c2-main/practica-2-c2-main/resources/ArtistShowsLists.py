from flask import Flask
from flask_restful import Resource, Api, reqparse
from sqlalchemy import UniqueConstraint

from models.artist import ArtistModel
from models.show import ShowModel
from lock import lock

app = Flask(__name__)
api = Api(app)


class ArtistShowsLists(Resource):
    def get(self, id):
        with lock.lock:
            #coger artista
            artist = ArtistModel.find_by_id(id)
            if artist is None:
                return {"message": "An error occurred searching the artist."}, 500
            shows = artist.shows
            return {'shows': [x.json() for x in shows]}
