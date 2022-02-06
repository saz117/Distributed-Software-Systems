from flask import Flask
from flask_restful import Resource, Api, reqparse
from sqlalchemy import UniqueConstraint

from models.show import ShowModel
from lock import lock

app = Flask(__name__)
api = Api(app)


class ShowList(Resource):
    def get(self):
        with lock.lock:
            # coger show
            shows = ShowModel.query.all()
            return {'shows': [x.json() for x in shows]}