from db import db
disciplines = ('THEATRE', 'MUSIC', 'DANCE', 'CIRCUS', 'OTHER')

disciplines_in_artist = db.Table('disciplines_in_artist',
                             db.Column('id', db.Integer, primary_key=True),
                             db.Column('artist_id', db.Integer, db.ForeignKey('artists.id')),
                             db.Column('discipline_id', db.Integer, db.ForeignKey('discipline.id')))


class DisciplineModel(db.Model):
    __tablename__ = 'discipline'  # This is table name
    id = db.Column(db.Integer, primary_key=True)
    name = db.Column(db.Enum(*disciplines,name='disciplines_types'),nullable=False)

    def __init__(self, name):
        #Del id se encarga SQLAlchemy al guardarlo este tipo de datos (PK) en la base de datos
        self.name = name

    # DEBERES 1:
    def json(self):
        return {'id': self.id, 'name': self.name}

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
        return DisciplineModel.query.filter_by(id=id).first() #Realmente solo devolverá uno

class ArtistModel(db.Model):
    __tablename__ = 'artists' #This is table name

    id = db.Column(db.Integer, primary_key=True)
    #name = db.Column(db.String(30))
    name = db.Column(db.String(30), unique = True, nullable=False)
    # EJERCICIO 1
    country = db.Column(db.String(30),nullable=False)
    genre = db.Column(db.String(15),nullable=True)
    # EJERCICIO 4
    #discipline = db.Column(db.Enum(*disciplines),nullable=False)
    discipline_id = db.Column(db.Integer, db.ForeignKey('discipline.id'))  # para el many to many
    discipline = db.relationship("DisciplineModel", secondary=disciplines_in_artist, backref=db.backref('artists'))

    #shows_id = db.Column(db.Integer, db.ForeignKey('shows.id')) # N-N ArtistModel y ShowModel


    def __init__(self, name, country, genre='M'):
        #Del id se encarga SQLAlchemy al guardarlo este tipo de datos (PK) en la base de datos
        self.name = name
        self.country = country
        self.genre = genre
        #self.discipline = discipline

    # DEBERES 1:
    def json(self):
        return {'id': self.id, 'name': self.name, 'discipline': [disc.json() for disc in self.discipline]}

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
        return ArtistModel.query.filter_by(id=id).first()  #Realmente solo devolverá uno