from importlib import resources

from flask import Flask

from flask_restful import Api

from resources.ArtistShowsLists import ArtistShowsLists
from resources.PlaceShowsList import PlaceShowsList
from resources.ShowArtist import ShowArtist
from resources.ShowList import ShowList
from resources.ArtistList import ArtistList
from resources.PlaceList import PlaceList
from resources.artist import Artist
from resources.place import Place
from resources.show import Show
from resources.ShowArtistsList import ShowArtistsList
from resources.orders import Orders
from resources.ordersList import OrdersList
from resources.accounts import Accounts, AccountsList, Login
from db import db #, secret_key
from flask_migrate import Migrate
from models.artist import ArtistModel
from models.show import ShowModel
from models.place import PlaceModel
from models.accounts import AccountsModel
from models.orders import OrdersModel
from flask_cors import CORS
from flask import render_template
from decouple import config as config_decouple
from config import config

"""
app = Flask(__name__,
         static_folder="frontend/dist/static",
         template_folder="frontend/dist")
"""

app = Flask(__name__)
environment = config['development']
if config_decouple('PRODUCTION', cast=bool, default=False):
    environment = config['production']

app.config.from_object(environment)

# Conectar automaticamente con nuestra base de datos + reducir consumo
app.config.from_object(__name__)
CORS(app, resources={r'/*': {'origins': '*'}})
#CORS(app)
"""
app.config['SQLALCHEMY_DATABASE_URI'] = 'sqlite:///data.db'
app.config['SQLALCHEMY_TRACK_MODIFICATIONS'] = False
app.config['SECRET_KEY'] = secret_key
"""
migrate = Migrate(app, db)
db.init_app(app)
api = Api(app)


# Sesio3 Configurant els directoris static i template de Flask.
@app.route('/')
def render_vue():
    return render_template("index.html")

"""
@app.route('/')
def hello_world():
    return 'Hello World!'
"""


# Ejercicio1
# Al acceder a: http://127.0.0.1:5000/python aparecerá ese mensage
@app.route('/python')
def like_python():
    return 'I like Python!'

"""
# Ejercicio2
# Al acceder a: http://127.0.0.1:5000/artist aparecerá la data de artists
#@app.route('/artist', methods=['GET'])
#def get_artists():
#    art = Artist()
#    return {'artists': art.get(1)}


# http://127.0.0.1:5000/shows
#@app.route('/show/<int:show_id>/artists', methods=['GET'])
#def get_showArtistsList(show_id):
#    sharli = ShowArtistsList()
#    return {'artist': sharli.get(show_id)}


# Al acceder a: http://127.0.0.1:5000/show aparecerá la data de shows
@app.route('/show', methods=['GET'])
def get_shows():
    sho = Show()
    return {'shows': sho.get(1)}


# Al acceder a: http://127.0.0.1:5000/place aparecerá la data de places
@app.route('/place', methods=['GET'])
def get_places():
    pla = Place()
    return {'places': pla.get(1)}



# Ejercicio 6
# http://127.0.0.1:5000/artists
@app.route('/artists', methods=['GET'])
def get_artistList():
    return {'artistList': ArtistModel}


# http://127.0.0.1:5000/places
@app.route('/places', methods=['GET'])
def get_placeList():
    return {'placeList': PlaceModel}


# http://127.0.0.1:5000/shows
@app.route('/shows', methods=['GET'])
def get_showList():
    return {'showList': ShowModel}
"""

# Fusion de los ejecutables
api.add_resource(Artist, '/artist/<int:id>', '/artist')
api.add_resource(ArtistList, '/artists')
api.add_resource(Place, '/place/<int:id>', '/place')
api.add_resource(PlaceList, '/places')
api.add_resource(Show, '/show/<int:id>', '/show')
api.add_resource(ShowList, '/shows')
api.add_resource(ShowArtistsList, '/show/<int:show_id>/artists')
api.add_resource(ShowArtist, '/show/<int:id_show>/artist/<int:id_artist>', '/show/<int:id_show>/artist')
api.add_resource(ArtistShowsLists, '/artist/<int:id>/shows')
api.add_resource(PlaceShowsList, '/place/<int:id>/shows')
api.add_resource(Orders, '/order/<string:username>')
api.add_resource(OrdersList, '/orders', '/orders/<string:username>')
api.add_resource(Accounts, '/account/<string:username>', '/account')
api.add_resource(AccountsList, '/accounts')
api.add_resource(Login, '/login')

if __name__ == '__main__':
    # app.run()
    app.run(port=5000, debug=True)
