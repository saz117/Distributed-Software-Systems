from flask import Flask
from flask_restful import Resource, Api, reqparse
from sqlalchemy import UniqueConstraint

from models.accounts import auth
from models.artist import ArtistModel, DisciplineModel
from lock import lock

app = Flask(__name__)
api = Api(app)


class Artist(Resource):

    def get(self, id):
        with lock.lock:
            # Comprobado #http://127.0.0.1:5000/artist/1
            artistModel = ArtistModel.find_by_id(id)
            if artistModel is not None:
                return {'artist': artistModel.json()}, 200 if artistModel else 404
            return {"message": "An error occurred searching the artist."}, 500

    @auth.login_required(role='admin')
    def post(self):
        with lock.lock:
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
            new_artist = ArtistModel(data['name'], data['country'], data['genre'])
            discpl = data['disciplines']
            if discpl is not None:
                for x in discpl:
                    # buscar la disciplia y despues añadirla
                    dis = DisciplineModel.query.filter_by(name=x).first()
                    new_artist.discipline.append(dis)
            try:
                new_artist.save_to_db()
            except:
                return {"message": "An error occurred inserting the artist."}, 500

            # realmente aquí siempre existirá
            return {'artist': new_artist.json()}, 200

    @auth.login_required(role='admin')
    def delete(self, id):
        with lock.lock:
            artistElem = ArtistModel.find_by_id(id)
            # artist = next(iter([x for x in ArtistModel.json() if x.id == id]), None)
            if artistElem is None:
                return {'message': "Artist with id [{}] not exists".format(id)}, 200
            try:
                artistElem.delete_from_db()
            except:
                return {"message": "An error occurred inserting the artist."}, 500
            return {'message': "Artist with id [{}] has been removed".format(id)}, 200

    @auth.login_required(role='admin')
    def put(self, id):
        with lock.lock:
            # mirar si existe el identificador
            artistElem = ArtistModel.find_by_id(id)
            if artistElem is None:
                # crear el artista, añadirlo y retornarlo
                return Artist.post(self)

            # modificar los valores del artista actual y retornarlo
            # es lo mismo que eliminarlo y volverlo a crear con los nuevos valores
            # Coger los datos que vienen del request
            parser = reqparse.RequestParser()  # create parameters parser from request
            # define all input parameters need and its type
            parser.add_argument('name', type=str, required=True, help="This field cannot be left blanck")
            parser.add_argument('country', type=str, required=True, help="This field cannot be left blanck")
            parser.add_argument('disciplines', type=str, required=True, help="This field cannot be left blanck",
                                action="append")  # action = "append" is needed to determine that is a list of strings
            # Comprbamos que todos los parametros esten el body del request
            data = parser.parse_args()

            # recuperar el artist de la tupla del get, coger al artista, coger sus disciplinas
            # artistElem=artist[0]['artist']
            # listFinalDisciplines=set(artistElem['disciplines'] + data['disciplines'])
            # transformarlo a lista porque lo devuelve como valores en diccionario
            # artist = {'id': id, 'name': data['name'], 'country': data['country'], 'disciplines': sorted(list(listFinalDisciplines))}
            # artist = {'name': data['name'], 'country': data['country'], 'disciplines': data['disciplines']}
            artistElem.name = data['name']
            artistElem.country = data['country']
            artistElem.discipline = []
            for x in data['disciplines']:
                # buscar la disciplia y despues añadirla
                dis = DisciplineModel.query.filter_by(name=x).first()
                artistElem.discipline.append(dis)

            # guardar cambios
            try:
                artistElem.save_to_db()
            except:
                return {"message": "An error occurred inserting the artist."}, 500
            return {'artist': artistElem.json()}, 200 if artistElem else 404

# Genral
# api.add_resource(Artist, '/artist/<int:id>')
# api.add_resource(Artist, '/artist/<int:id>', '/artist')

# if __name__ == '__main__':
# app.run()
# app.run(port=5000, debug=True)
