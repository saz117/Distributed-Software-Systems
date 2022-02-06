from flask import Flask
from flask_restful import Resource, Api, reqparse
from sqlalchemy import UniqueConstraint

from models.place import PlaceModel
from models.show import ShowModel
from lock import lock

app = Flask(__name__)
api = Api(app)


class PlaceList(Resource):
    def get(self):
        with lock.lock:
            # coger show
            places = PlaceModel.query.all()
            return {'places': [x.json() for x in places]}