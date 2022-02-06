from flask import Flask
from flask_restful import Resource, Api, reqparse
from sqlalchemy import UniqueConstraint

from models.artist import ArtistModel
from models.show import ShowModel
from lock import lock

app = Flask(__name__)
api = Api(app)


class ArtistList(Resource):
    def get(self):
        with lock.lock:
            # coger show
            artists = ArtistModel.query.all()
            return {'artists': [x.json() for x in artists]}