from flask import Flask
from flask_sqlalchemy import SQLAlchemy
#import models here
from data import shows, artists, places
from flask_restful import Api
from resources.artist import Artist
from resources.place import Place
from resources.show import Show
from db import db
from flask_migrate import Migrate
from models.artist import ArtistModel, DisciplineModel
from models.show import ShowModel
from models.place import PlaceModel

app = Flask(__name__)
app.config['SQLALCHEMY_DATABASE_URI'] = 'sqlite:///data.db'
app.config['SQLALCHEMY_TRACK_MODIFICATIONS'] = False
db = SQLAlchemy(app)
db.init_app(app)



# EJERICIO 5

# PASO 1: eliminar carpeta migrations y archivo data.db
# PASO 2: ejecutar en terminal las siguientes 3 lineas
#flask db init
#flask db migrate -m "Initial migration"
#flask db upgrade

#Paso 3: ejecutar en la terminal la instrucción (para mi: python add_data.py):
#python3 add_data.py

#Paso 4: ver como se actualiza la base de datos

new_discipline1 = DisciplineModel('THEATRE')
new_discipline2 = DisciplineModel('MUSIC')
new_discipline3 = DisciplineModel('DANCE')
new_discipline4 = DisciplineModel('CIRCUS')
new_discipline5 = DisciplineModel('OTHER')
db.session.add(new_discipline1)
db.session.add(new_discipline2)
db.session.add(new_discipline3)
db.session.add(new_discipline4)
db.session.add(new_discipline5)
db.session.commit()

new_artist1 = ArtistModel('La Calòrica','Spain')
new_artist2= ArtistModel('Txarango','Spain')
new_artist1.discipline.append(new_discipline2)
new_artist1.discipline.append(new_discipline3)
new_artist2.discipline.append(new_discipline1)
new_artist2.discipline.append(new_discipline4)
db.session.commit()

from datetime import datetime
new_show1=ShowModel('El gran Circ',datetime.strptime('2021-07-04', "%Y-%m-%d"),'50.0')
new_show2=ShowModel('Tour de France',datetime.strptime('2021-07-05', "%Y-%m-%d"),'100.0')
db.session.add(new_show1)
db.session.add(new_show2)
db.session.commit()

new_show1.artists.append(new_artist1)
new_show2.artists.append(new_artist2)
db.session.add(new_show1)
db.session.add(new_show2)
db.session.commit()


new_place1=PlaceModel('Sants Estacio','Barcelona','Spain',100)
new_place2=PlaceModel('Lorca','Murcia','Germany',5000)
db.session.add(new_place1)
db.session.add(new_place2)
db.session.commit()

new_show1.place = new_place1
new_show1.total_available_tickets=new_show1.place.capacity
new_show2.place = new_place2
new_show2.total_available_tickets=new_show2.place.capacity
db.session.commit()

from models.accounts import AccountsModel
from models.orders import OrdersModel
test_user=AccountsModel(username='test')
db.session.add(test_user)
db.session.commit() 