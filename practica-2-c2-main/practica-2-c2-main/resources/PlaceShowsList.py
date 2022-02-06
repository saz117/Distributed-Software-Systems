from flask import Flask
from flask_restful import Resource, Api, reqparse
from sqlalchemy import UniqueConstraint

from models.show import ShowModel
from models.place import PlaceModel
from lock import lock

app = Flask(__name__)
api = Api(app)


class PlaceShowsList(Resource):
    def get(self, id):
        with lock.lock:
            #coger show
            place = PlaceModel.find_by_id(id)
            if place is None:
                return {"message": "An error occurred searching the place."}, 500
            shows = ShowModel.query.filter_by(place=place).all()
            return {'shows': [x.json() for x in shows]}
