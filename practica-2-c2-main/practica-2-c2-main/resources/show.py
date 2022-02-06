from flask import Flask
from flask_restful import Resource, Api, reqparse
from db import db
from models.accounts import auth
from models.artist import ArtistModel
from models.place import PlaceModel
from models.show import ShowModel, artists_in_shows
import dateutil.parser
from lock import lock

app = Flask(__name__)
api = Api(app)


class Show(Resource):

    def get(self, id):
        with lock.lock:
            # Comprobado #http://127.0.0.1:5000/show/1
            showElem = ShowModel.find_by_id(id)
            if showElem == None:
                return {"message": "An error occurred searching the show."}, 500
            return {'show': showElem.json()}, 200 if showElem else 404

    @auth.login_required(role='admin')
    def post(self):
        with lock.lock:
            # Coger los datos que vienen del request
            parser = reqparse.RequestParser()  # create parameters parser from request
            # define all input parameters need and its type
            parser.add_argument('name', type=str, required=True, help="This field cannot be left blanck")
            parser.add_argument('date', type=str,required=True, help="This field cannot be left blanck")
            #parser.add_argument('date', type=lambda s: datetime.datetime.strptime(s, '%Y-%m-%d'))
            parser.add_argument('price', type=float, required=True, help="This field cannot be left blanck")
            parser.add_argument('place', type=str, required=True, help="This field cannot be left blanck")
            parser.add_argument('city', type=str, help="This field cannot be left blanck")
            parser.add_argument('country', type=str, help="This field cannot be left blanck")
            parser.add_argument('total_available_tickets', required=True, type=int, help="This field can be left blanck")
            parser.add_argument('artist', type=str, help="This field cannot be left blanck",
                                action="append")  # action = "append" is needed to determine that is a list of strings
            # p = parser.args[1]     #TODO
            # Comprbamos que todos los parametros esten el body del request
            data = parser.parse_args()
            yourdate = dateutil.parser.parse(data['date'])
            new_show = ShowModel(data['name'], yourdate, data['price'], data['total_available_tickets'])
            #new_show1.place = new_place1
            new_place = PlaceModel.query.filter_by(name=data['place']).first()
            if new_place is None:
                new_place = PlaceModel(data['place'], data['city'], data['country'], data['total_available_tickets'])
            new_show.place = new_place

            #conseguir el artist a partir del nombre
            #el artista debe existir
            artists = data['artist']
            if artists is not None:
                for x in artists:
                    artist = ArtistModel.query.filter_by(name=x).first()
                    new_show.artists.append(artist)
            try:
                new_show.save_to_db()
            except:
                return {"message": "An error occurred inserting the show."}, 500
            # realmente aquí siempre existirá
            return {'show': new_show.json()}, 200

    @auth.login_required(role='admin')
    def delete(self, id):
        with lock.lock:
            showElem = ShowModel.find_by_id(id)
            # artist = next(iter([x for x in ArtistModel.json() if x.id == id]), None)
            if showElem == None:
                return {'message': "Place with id [{}] not exists".format(id)}, 200
            try:
                showElem.delete_from_db()
            except:
                return {"message": "An error occurred inserting the show."}, 500
            return {'message': "Show with id [{}] has been removed".format(id)}, 200

    @auth.login_required(role='admin')
    def put(self, id):
        with lock.lock:
            # mirar si existe el identificador
            showElem = ShowModel.find_by_id(id)
            if showElem == None:
                return Show.post(self)

            # modificar los valores del show actual y retornarlo
            # es lo mismo que eliminarlo y volverlo a crear con los nuevos valores
            # Coger los datos que vienen del request
            parser = reqparse.RequestParser()  # create parameters parser from request
            # define all input parameters need and its type
            parser.add_argument('name', type=str, help="This field cannot be left blanck")
            parser.add_argument('date', type=str, help="This field cannot be left blanck")  # type=date  #TODO
            parser.add_argument('price', type=float, required=True, help="This field cannot be left blanck")
            parser.add_argument('place', type=str,  help="This field cannot be left blanck")  # action = "append" is needed to determine that is a list of strings
            parser.add_argument('city', type=str, help="This field cannot be left blanck")
            parser.add_argument('country', type=str, help="This field cannot be left blanck")
            parser.add_argument('total_available_tickets', required=True, type=int, help="This field can be left blanck")
            parser.add_argument('artist', type=str, help="This field cannot be left blanck",
                                action="append")  # action = "append" is needed to determine that is a list of strings
            # p = parser.args[1]     #TODO
            # Comprbamos que todos los parametros esten el body del request
            data = parser.parse_args()
            # recuperar el show de la tupla del get, coger al show, coger sus disciplinas
            # showElem=show[0]['show']
            # listFinalDisciplines=set(showElem['disciplines'] + data['disciplines'])
            # transformarlo a lista porque lo devuelve como valores en diccionario
            # show = {'id': id, 'name': data['name'], 'country': data['country'], 'disciplines': sorted(list(listFinalDisciplines))}
            if data['date'] is not None and data['date'] !='':
                yourdate = dateutil.parser.parse(data['date'])
                showElem.date =yourdate
            if data['name'] is not None and data['name'] !='':
                showElem.name = data['name']
            if data['price'] != 0:
                showElem.price = data['price']
            if data['total_available_tickets'] !=0:
                showElem.total_available_tickets = data['total_available_tickets']
            if data['place'] is not None and data['place'] !='':
                new_place = PlaceModel.query.filter_by(name=data['place']).first()
                if new_place is None:
                    new_place = PlaceModel(data['place'], data['city'], data['country'], data['total_available_tickets'])
                showElem.place = new_place
            #showElem.place = data['place']
            artists = data['artist']
            if artists is not None:
                for x in data['artist']:
                    artist = ArtistModel.query.filter_by(name=x).first()
                    showElem.artists.append(artist)
            # sustituir el show por este nuevo
            try:
                showElem.save_to_db()
            except:
                return {"message": "An error occurred inserting the show."}, 500
            return {'show': showElem.json()}, 200 if showElem else 404

# Genral
# api.add_resource(Show, '/show/<int:id>')
# api.add_resource(Show, '/show/<int:id>', '/show')

# if __name__ == '__main__':
# app.run()
# app.run(port=5000, debug=True)
