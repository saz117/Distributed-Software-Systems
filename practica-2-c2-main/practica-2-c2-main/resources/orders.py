from flask import Flask
from flask_restful import Resource, Api, reqparse
from sqlalchemy import UniqueConstraint
from db import db
from models.artist import ArtistModel
from models.show import ShowModel
from resources.ordersList import OrdersList
from models.orders import OrdersModel
from models.accounts import AccountsModel, auth, g
from lock import lock

app = Flask(__name__)
api = Api(app)


class Orders(Resource):

    def get(self, username):
        with lock.lock:
            #
            user = AccountsModel.find_by_username(username)
            orders = OrdersModel.query.filter_by(username=user)
            if orders is None:
                return {"message": "An error occurred searching the orders."}, 500
            return {'orders': [x.json() for x in orders]}, 200 if orders else 404

    @auth.login_required(role='user')
    def post(self, username):
        with lock.lock:
            if g.user.username != username:
                return {"message": "The username not math whit the username logged."}, 400

            # Coger los datos que vienen del request
            parser = reqparse.RequestParser()  # create parameters parser from request
            # define all input parameters need and its type
            parser.add_argument('id_show', type=int, required=True, help="This field cannot be left blanck")
            parser.add_argument('tickets_bought', type=int, required=True, help="This field cannot be left blanck")

            data = parser.parse_args()
            # consultar el usuaio actual
            account = AccountsModel.find_by_username(username)
            if account is None:
                return {"message": "An error occurred searching the username."}, 500

            id_show = data['id_show']
            show = ShowModel.find_by_id(id_show)
            if show is None:
                return {"message": "An error occurred searching the show."}, 500

            tickets_bought = data['tickets_bought']
            totalPrice = tickets_bought * show.price
            if account.available_money < totalPrice:
                return {"message": "Not enought money."}, 500

            if show.total_available_tickets < tickets_bought:
                return {"message": "Not enought avaible tickets."}, 500

            new_order = OrdersModel(id_show, tickets_bought)
            # new_order.username=account
            account.orders.append(new_order)
            show.total_available_tickets = show.total_available_tickets - tickets_bought
            account.available_money = account.available_money - totalPrice

            # Transaction
            db.session.begin()
            try:
                db.session.add(new_order)
                db.session.flush()
                db.session.add(show)
                db.session.flush()
                db.session.add(account)
                db.session.flush()
                db.session.commit()
            except:
                db.session.rollback()

            return {'orders': new_order.json()}, 200 if new_order else 404
