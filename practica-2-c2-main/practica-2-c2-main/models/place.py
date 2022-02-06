from db import db


# EJERCICIO 2.2
class PlaceModel(db.Model):
    __tablename__ = 'places'  # This is table name
    __table_args__ = (db.UniqueConstraint('name', 'city', 'country', 'capacity'),)

    id = db.Column(db.Integer,primary_key=True)  # TODO-REPAS al ser PK no haría conceptualmente pues es unico, pero dicen todos
    name = db.Column(db.String(30), nullable=False)
    city = db.Column(db.String(30), nullable=False)
    country = db.Column(db.String(30), nullable=False)
    capacity = db.Column(db.Integer, nullable=False)

    # shows = db.relationship('ShowModel', backref = 'place') #One to many con ShowModel

    def __init__(self, name, city, country, capacity):
        self.name = name
        self.city = city
        self.country = country
        self.capacity = capacity

    # DEBERES 1:
    def json(self):
        return {'id': self.id, 'name': self.name, 'city': self.city, 'country': self.country, 'capacity': self.capacity}

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
        return PlaceModel.query.filter_by(id=id).first()  # Realmente solo devolverá uno
