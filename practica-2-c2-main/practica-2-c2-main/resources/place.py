from flask import Flask
from flask_restful import Resource, Api, reqparse

from models.accounts import auth
from models.place import PlaceModel
from lock import lock

app = Flask(__name__)
api = Api(app)


class Place(Resource):

    def get(self, id):
        with lock.lock:
            # Comprobado #http://127.0.0.1:5000/place/1
            placeModel = PlaceModel.find_by_id(id)
            if placeModel is None:
                return {"message": "An error occurred searching the place."}, 500
            return {'place': placeModel.json()}, 200 if placeModel else 404

    @auth.login_required(role='admin')
    def post(self):
        with lock.lock:
            # Coger los datos que vienen del request
            parser = reqparse.RequestParser()  # create parameters parser from request
            # define all input parameters need and its type
            parser.add_argument('name', type=str, required=True, help="This field cannot be left blanck")
            parser.add_argument('city', type=str, required=True, help="This field cannot be left blanck")
            parser.add_argument('country', type=str, required=True, help="This field cannot be left blanck")
            parser.add_argument('capacity', type=int, required=True, help="This field cannot be left blanck")
            # Comprbamos que todos los parametros esten el body del request
            data = parser.parse_args()

            new_place = PlaceModel(data['name'],data['city'],data['country'],data['capacity'])
            # realmente aquí siempre existirá
            try:
                new_place.save_to_db()
            except:
                return {"message": "An error occurred inserting the place."}, 500
            return {'place': new_place.json()}, 200

    @auth.login_required(role='admin')
    def delete(self, id):
        with lock.lock:
            placeElem = PlaceModel.find_by_id(id)
            # artist = next(iter([x for x in ArtistModel.json() if x.id == id]), None)
            if placeElem is None:
                return {'message': "Place with id [{}] not exists".format(id)}, 200
            try:
                placeElem.delete_from_db()
            except:
                return {"message": "An error occurred inserting the place."}, 500
            return {'message': "Place with id [{}] has been removed".format(id)}, 200

    @auth.login_required(role='admin')
    def put(self, id):
        with lock.lock:
            # mirar si existe el identificador
            placeElem = PlaceModel.find_by_id(id)
            if placeElem is None:
                return Place.post(self)

            # modificar los valores del place actual y retornarlo
            # es lo mismo que eliminarlo y volverlo a crear con los nuevos valores
            # Coger los datos que vienen del request
            parser = reqparse.RequestParser()  # create parameters parser from request
            # define all input parameters need and its type
            parser.add_argument('name', type=str, required=True, help="This field cannot be left blanck")
            parser.add_argument('city', type=str, required=True, help="This field cannot be left blanck")
            parser.add_argument('country', type=str, required=True, help="This field cannot be left blanck")
            parser.add_argument('capacity', type=int, required=True, help="This field cannot be left blanck")
            # Comprbamos que todos los parametros esten el body del request
            data = parser.parse_args()
            # recuperar el place de la tupla del get, coger al place, coger sus disciplinas
            # placeElem=place[0]['place']
            # listFinalDisciplines=set(placeElem['disciplines'] + data['disciplines'])
            # transformarlo a lista porque lo devuelve como valores en diccionario
            # place = {'id': id, 'name': data['name'], 'country': data['country'], 'disciplines': sorted(list(listFinalDisciplines))}
            placeElem.name = data['name']
            placeElem.city = data['city']
            placeElem.country = data['country']
            placeElem.capacity = data['capacity']
            # guardar cambios
            try:
                placeElem.save_to_db()
            except:
                return {"message": "An error occurred inserting the place."}, 500
            return {'place': placeElem.json()}, 200 if placeElem else 404

# Genral
# api.add_resource(Place, '/place/<int:id>')
# api.add_resource(Place, '/place/<int:id>', '/place')

# if __name__ == '__main__':
# app.run()
# app.run(port=5000, debug=True)
