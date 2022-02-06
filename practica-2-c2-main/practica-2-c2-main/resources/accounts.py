from flask_restful import Resource, Api, reqparse
from models.accounts import AccountsModel, auth
from models.orders import OrdersModel
from lock import lock

class Accounts(Resource):
    #@auth.login_required(role='admin')
    def get(self, username):
        with lock.lock:
            user = AccountsModel.find_by_username(username)
            return {'account': user.json()}, 200 if user else 404

    #@auth.login_required(role='admin')
    def post(self):
        with lock.lock:
            # Coger los datos que vienen del request
            parser = reqparse.RequestParser()  # create parameters parser from request
            # define all input parameters need and its type
            parser.add_argument('username', type=str, required=True, help="This field cannot be left blanck")
            parser.add_argument('password', type=str, required=True, help="This field cannot be left blanck")
            parser.add_argument('available_money', type=int)
            parser.add_argument('is_admin', type=int)

            data = parser.parse_args()
            if data['available_money'] is None and data['is_admin'] is None:
                user = AccountsModel(data['username'])
            elif data['available_money'] is None:
                user = AccountsModel(data['username'], data['available_money'])
            elif data['is_admin'] is None:
                user = AccountsModel(data['username'], is_admin=['is_admin'])
            else:
                user = AccountsModel(data['username'], data['available_money'], data['is_admin'])
            user.hash_password(data['password'])
            user.save_to_db()
            return {'account': user.json()}, 200 if user else 404

    @auth.login_required(role='admin')
    def delete(self, username):
        with lock.lock:
            user = AccountsModel.find_by_username(username)
            orders = OrdersModel.query.filter_by(username=username).all()
            for order in orders:
                order.delete_from_db()
            user.delete_from_db()
            return {"message": "Username and orders deleted."}, 200


class AccountsList(Resource):
    @auth.login_required(role='admin')
    def get(self):
        with lock.lock:
            users = AccountsModel.query.all()
            return {'accounts': [user.json() for user in users]}, 200 if users else 404


class Login(Resource):
    def post(self):
        with lock.lock:
            # Coger los datos que vienen del request
            parser = reqparse.RequestParser()  # create parameters parser from request
            # define all input parameters need and its type
            parser.add_argument('username', type=str, required=True, help="This field cannot be left blanck")
            parser.add_argument('password', type=str, required=True, help="This field cannot be left blanck")

            data = parser.parse_args()
            user = AccountsModel.find_by_username(data['username'])
            if user is None:
                return {"message": "Username no exist"}, 404
            verify = user.verify_password(data['password'])
            if not verify:
                return {"message": "Password incorrect"}, 400 #TODO 404 porque no? Y YO QUE SE no me acuerdo
            token = user.generate_auth_token()
            return {'token': token.decode('ascii')}, 200
