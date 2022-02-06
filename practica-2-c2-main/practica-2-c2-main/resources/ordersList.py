from flask import Flask, g
from flask_restful import Resource, Api, reqparse
from sqlalchemy import UniqueConstraint, create_engine
from sqlalchemy.orm import sessionmaker

from db import db
from models.artist import ArtistModel
from models.orders import OrdersModel
from models.show import ShowModel
from models.accounts import AccountsModel, auth
from lock import lock

app = Flask(__name__)
api = Api(app)


class OrdersList(Resource):

    @classmethod
    def return_orders(cls):
        return AccountsModel.orders.all()

    def get(self):
        with lock.lock:
            orders = OrdersModel.query.all()
            return {'orders': [x.json() for x in orders]}

    @auth.login_required(role='user')
    def post(self, username):
        with lock.lock:
            # Coger los datos que vienen del request
            parser = reqparse.RequestParser()  # create parameters parser from request
            # define all input parameters need and its type
            parser.add_argument('id_show', type=int, required=True, help="This field cannot be left blanck",
                                action="append")
            parser.add_argument('tickets_bought', type=int, required=True, help="This field cannot be left blanck",
                                action="append")

            data = parser.parse_args()
            ordenes = []
            shows = data['id_show']
            tick = data['tickets_bought']
            #account = AccountsModel.find_by_username(username)
            account = AccountsModel.find_by_username(username)
            if account is None:
                return {"message": "An error occurred searching the username."}, 500
            if g.user.username != account.username:
                return {"message": "An error occurred the username not is the user logged."}, 400

            sumTotalPrice=0
            for i, show_id in enumerate(shows):
                """
                account = AccountsModel.find_by_username(username)
                if account is None:
                    return {"message": "An error occurred searching the username."}, 500
                print("Entra 48 OrlderList")
                print("g.user.username", g.user.username)
                print("account.username", account.username)
                if g.user.username != account.username:
                    return {"message": "An error occurred the username not is the user logged."}, 400
                """
                show = ShowModel.find_by_id(show_id)
                if show is None:
                    return {"message": "An error occurred searching the show."}, 500

                totalPrice = tick[i] * show.price
                """
                if account.available_money < totalPrice:
                    return {"message": "Not enought money."}, 500
                """
                sumTotalPrice=sumTotalPrice+totalPrice
                if show.total_available_tickets < tick[i]:
                    return {"message": "Not enought avaible tickets."}, 500
                """
                new_order = OrdersModel(show_id, tick[i])
                new_order.username = account.username
                account.orders.append(new_order)
                show.total_available_tickets = show.total_available_tickets - tick[i]
                account.available_money = account.available_money - totalPrice
                """
                # Transaction
                """
                with db.session() as session:
                    session.add(new_order)
                    session.add(show)
                    session.add(account)
                    session.commit()
                    orderultima=OrdersModel.query.filter_by(id=new_order.id).first()
                    ordenes.append(orderultima)
                """
                #db.session.begin()

            print("sumTotalPrice", sumTotalPrice)
            if account.available_money < sumTotalPrice:
                return {"message": "Not enought money."}, 500

            for i, show_id in enumerate(shows):
                account = AccountsModel.find_by_username(username)
                new_order = OrdersModel(show_id, tick[i])
                new_order.username = account.username
                account.orders.append(new_order)
                show.total_available_tickets = show.total_available_tickets - tick[i]
                account.available_money = account.available_money - totalPrice
                try:
                    db.session.add(new_order)
                    db.session.add(show)
                    db.session.add(account)
                    db.session.commit()
                    orderultima = OrdersModel.query.filter_by(id=new_order.id).first()
                    ordenes.append(orderultima)
                except:
                    db.session.rollback()

                g.user=AccountsModel.find_by_username(username)
            return {'orders': [x.json() for x in ordenes]}, 200 if ordenes else 404
