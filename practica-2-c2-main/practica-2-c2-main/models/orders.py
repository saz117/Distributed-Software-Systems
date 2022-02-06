from db import db
from models.show import ShowModel


class OrdersModel(db.Model):
    __tablename__ = 'orders'

    id = db.Column(db.Integer, primary_key=True)
    username = db.Column(db.String(30), db.ForeignKey('accounts.username'), nullable=False)
    id_show = db.Column(db.Integer, nullable=False)
    tickets_bought = db.Column(db.Integer, nullable=False)

    def __init__(self, id_show, tickets_bought):
        self.id_show = id_show
        self.tickets_bought = tickets_bought

    def json(self):
        show = ShowModel.find_by_id(self.id_show)
        return {'username': self.username, 'show': show.json(), 'tickets_bought': self.tickets_bought}

    def save_to_db(self):
        db.session.add(self)
        db.session.commit()

    def delete_from_db(self):
        db.session.delete(self)
        db.session.commit()
