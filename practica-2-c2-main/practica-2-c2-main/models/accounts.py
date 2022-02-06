from db import db #, secret_key
from passlib.apps import custom_app_context as pwd_context
from itsdangerous import (TimedJSONWebSignatureSerializer as Serializer, BadSignature, SignatureExpired)
from flask_httpauth import HTTPBasicAuth
from flask import g, current_app

auth = HTTPBasicAuth()


class AccountsModel(db.Model):
    __tablename__ = 'accounts'

    username = db.Column(db.String(30), primary_key=True, unique=True, nullable=False)
    password = db.Column(db.String(), nullable=False)
    # 0 not admin/ 1 is admin
    is_admin = db.Column(db.Integer, nullable=False)
    available_money = db.Column(db.Integer)
    # 1-N
    orders = db.relationship('OrdersModel', backref='orders', lazy=True)

    def __init__(self, username, available_money=200, is_admin=0):
        self.username = username
        self.available_money = available_money
        self.is_admin = is_admin

    # EXERCICI 3:
    def json(self):
        return {'username': self.username, 'is_admin': self.is_admin, 'available_money': self.available_money}

    def save_to_db(self):
        db.session.add(self)
        db.session.commit()

    def delete_from_db(self):
        db.session.delete(self)
        db.session.commit()

    def hash_password(self, password):
        self.password = pwd_context.encrypt(password)

    def verify_password(self, password):
        return pwd_context.verify(password, self.password)

    def generate_auth_token(self, expiration=600):
        s = Serializer(current_app.secret_key, expires_in=expiration)
        return s.dumps({'username': self.username})

    @classmethod
    def verify_auth_token(cls, token):
        s = Serializer(current_app.secret_key)
        try:
            data = s.loads(token)
        except SignatureExpired:
            return None  # valid token, but expired
        except BadSignature:
            return None  # invalid token

        user = cls.query.filter_by(username=data['username']).first()

        return user

    @classmethod
    def find_by_username(cls, username):
        return AccountsModel.query.filter_by(username=username).first()  # Realmente solo devolverá uno


@auth.verify_password
def verify_password(token, password):
    user = AccountsModel.verify_auth_token(token)
    # user = AccountsModel.find_by_username('user1')
    if user is not None:
        g.user = user
        return user


@auth.get_user_roles
def get_user_roles(user):
    if user.is_admin == 0:
        return ['user']
    else:
        return ['admin']
