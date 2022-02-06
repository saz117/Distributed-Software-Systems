from datetime import datetime
import dateutil.parser
from db import db

# Ejercicio 3: tabla intermedia entre artists y shows N-N
artists_in_shows = db.Table('artists_in_shows',
                            db.Column('id', db.Integer, primary_key=True),
                            db.Column('artist_id', db.Integer, db.ForeignKey('artists.id')),
                            db.Column('show_id', db.Integer, db.ForeignKey('shows.id')))


# Ejercicio 2.1
class ShowModel(db.Model):
    __tablename__ = 'shows'  # This is table name
    __table_args__ = (db.UniqueConstraint('name', 'date',
                                          'price'),)  # per a evitar l'existència d'un event amb el mateix ('name','date','price')

    id = db.Column(db.Integer, primary_key=True)  # TODO-REPAS al ser PK no haría conceptualmente pues es unico, pero dicen todos
    name = db.Column(db.String(30), nullable=False)
    date = db.Column(db.DateTime, nullable=False)
    price = db.Column(db.Float, nullable=False)
    total_available_tickets = db.Column(db.Integer)
    # Ejercicio 3: atributo para relacionar place y shows 1-N
    place_id = db.Column(db.Integer, db.ForeignKey('places.id'))
    place = db.relationship("PlaceModel")

    # Ejercicio 3: atributo para relacionar tabla intermedia entre artists y shows N-N
    #artists_id = db.Column(db.Integer, db.ForeignKey('artists.id'))  # para el many to many
    artists = db.relationship("ArtistModel", secondary=artists_in_shows, backref=db.backref('shows'))

    def __init__(self, name, date, price, total_tickets=100):
        # Del id se encarga SQLAlchemy al guardarlo este tipo de datos (PK) en la base de datos
        self.name = name
        self.date = date
        self.price = price
        self.total_available_tickets = total_tickets
        
    # DEBERES 1:
    def json(self):
        formatted_datetime = self.date.isoformat()
        return {'id': self.id, 'name': self.name, 'date': formatted_datetime, 'price': self.price, 'total_available_tickets': self.total_available_tickets, 'place': self.place.json(),
                'artists': [art.json() for art in self.artists]}

    # DEBERES 2:
    def save_to_db(self):
        db.session.add(self)
        db.session.commit()

    # DEBERES 2:
    def delete_from_db(self):
        db.session.delete(self)
        db.session.commit()

    # DEBERES 3:
    @classmethod
    def find_by_id(cls, id):
        return ShowModel.query.filter_by(id=id).first()  # Realmente solo devolverá uno
