Sessió 5
=========

Afegint seguretat al nostre backend
--------

En aquesta secció modificarem i afegirem alguns models i funcions al nostre AccountsModel, per afegir seguretat bàsica i autenticació.

### Desa la contrasenya d’usuari xifrada

No és una bona idea emmagatzemar contrasenyes o informació sensible (número de targeta de crèdit, etc.) sense xifrar aquesta informació.
En aquest cas, farem servir la biblioteca python `passlib` per a la comprovació de contrasenyes (` Els valors predeterminats són SHA256-Crypt en sistemes de 32 bits, SHA512-Crypt en sistemes de 64 bits`).

Primer instal·leu `passlib` fent:
	
	    pip3 install passlib


I afegiu aquests dos mètodes a `models/accounts.py`:

```python
from passlib.apps import custom_app_context as pwd_context

class AccountsModel(db.Model):

    #PREVIOUS CODE HERE

    def hash_password(self, password):
        self.password = pwd_context.encrypt(password)
    def verify_password(self, password):
        return pwd_context.verify(password, self.password)
```

### Exercici 1:

1. Comproveu o modifiqueu AccountsModel `__init__` (suprimiu la inicialització de contrasenya falsa) i inicialitzeu-ho només amb valors de nom d'usuari, `available_money` i `is_admin`.
2. Creeu recursos als mètodes `/resources/accounts.py` relacionats amb aquests punts finals (afegiu-los a  `app.py`):

```python
    api.add_resource(Accounts, '/account/<string:username>', '/account')
    api.add_resource(AccountsList, '/accounts')
```

on s'inclouen:

- **get**(self,username): obtenir informació del compte amb un nom d'usuari
- **post**(self): creeu un compte nou passant `username` i `password`. Utilitzeu `hash_ password` quan creeu un compte i utilitzeu `save_to_db()` quan deseu un usuari nou (primer heu de crear un usuari nou i després afegir una contrasenya hash mitjançant el mètode `.hash_ password (password)`).
- **delete**(self,username): suprimiu un compte relacionat amb un nom d'usuari (recordeu també suprimir totes les comandes relacionades).

i AccountsList conté:

 -   **get**(self): obtenir informació sobre tots els comptes

 
Recordeu que heu de retornar missatges descriptius i un codi si no es poden publicar algunes peticions.

### Autorització per Token

Una manera senzilla de tenir un sistema d’inici de sessió segur és utilitzar tokens en lloc d’enviar noms d’usuari o contrasenyes en el cos de les peticions. Aquest token sol tenir una vida útil fixa i hi conté informació codificada (nom d’usuari, etc.).
Per generar aquest token utilitzarem una senzilla biblioteca de python anomenada "itsdangerous" que hem d'instal·lar:    

     pip3 install itsdangerous

A continuació, afegirem alguns mètodes a /models/account.py:

```python
from itsdangerous import (TimedJSONWebSignatureSerializer as Serializer, BadSignature, SignatureExpired)

class AccountsModel(db.Model): 

    #PREVIOUS CODE HERE 

    def generate_auth_token(self, expiration=600):
        s = Serializer(secret_key, expires_in=expiration)
        return s.dumps({'username': self.username}) 

    @classmethod
    def verify_auth_token(cls, token):
        s = Serializer(secret_key)
        try:
            data = s.loads(token)
        except SignatureExpired:
            return None  # valid token, but expired
        except BadSignature:
            return None  # invalid token

        user = cls.query.filter_by(username=data['username']).first() 

        return user
```

on:

- **generate\_auth\_token**: retorna un token vàlid amb informació de nom d'usuari incrustada i la caducitat definida per un valor d'entrada.

- **verify\_auth\_token**: si el token d’entrada és vàlid, extreurà la informació del nom d’usuari i retornarà aquest usuari.
    
Com podeu veure al codi anterior, hi ha un paràmetre de `secret key` que el mètode **generate\_auth\_token** utilitza com a llavor de generació i el mètode **verify\_auth\_token** l'utilitza per recuperar les dades incrustades en el nostre token. Definirem una clau secreta a `db.py`:

```python
secret_key = "1q2s3f5g7jggujbffrhnbcdgh78jbhd"
```

Aquesta "clau secreta" pot ser un número, una frase o un conjunt aleatori de números i lletres que només la vostra aplicació hauria de conèixer. Hem d'importar aquesta clau a `app.py`:

```python
from db import db, secret_key

app.config['SECRET_KEY'] = secret_key
```

I també importeu a `/models/accounts`:

```python
from db import db, secret_key
```

### Exercici 2:

1. Creeu un login.py nou a la carpeta de recursos amb el mètode post (`api.add_ resource(Login, ’/login’)`):
	- **post**(self):
		- `username` i `password` com a paràmetres obligatoris de la sol·licitud. 
		-  Comproveu si aquest "nom d'usuari" i "contrasenya" corresponen a un dels nostres usuaris (utilitzeu **verify\_password**) i retorneu un token vàlid (utilitzeu **generate\_auth\_token** sobre l'usuari retornat):

	```python
        return {'token': token.decode('ascii')}, 200
    ```
        
   - torna un missatge descriptiu i 404 si no es troba l'usuari
   - torna un missatge descriptiu i 400 si la contrasenya no és vàlida

2. Valideu-ho amb la creació de dos usuaris (un amb privilegis d'administrador) si l'inici de sessió funciona correctament. Podeu utilitzar els següents exemples de terminals:
    
  - Obteniu tots els comptes corrents:
   
   			>>>import requests
   			>>>r = requests.get('http://127.0.0.1:5000/accounts')
   			>>>r.json()
            {'accounts': [{'username': 'npujol', 'is_admin': 0, 'available_money': 20},
                            {'username': 'test', 'is_admin': 0, 'available_money': 200},
                            {'username': 'user1', 'is_admin': 0, 'available_money': 20}]} 
 - 	Afegiu un compte nou (sense privilegis d'administrador):
 
 		 	>>>r = requests.post('http://127.0.0.1:5000/account', data = {'username':'user', 'password':'1234'})
            >>>r.json()
             {'username': 'user', 'is_admin': 0, 'available_money': 200}

            >>>r
            <Response [201]>

            >>>r = requests.get('http://127.0.0.1:5000/accounts')
            >>>r.json()
            {'accounts': [{'username': 'npujol', 'is_admin': 0, 'available_money': 20},
                     {'username': 'test', 'is_admin': 0, 'available_money': 200},
                     {'username': 'user1', 'is_admin': 0, 'available_money': 20},
                     {'username': 'user', 'is_admin': 0, 'available_money': 200}]}
                     
 - Afegiu un account nou (amb privilegis d'administrador, amb el Terminal):

 			>>> from models.accounts import AccountsModel
            >>>from models.orders import OrdersModel
            >>>from models.shows import ShowModel
            >>>from models.artist import ArtistModel
            >>>from db import db
            >>>new_account = AccountsModel(username='admin', is_admin=1)
            >>>new_account.hash_password('admin') #'username:'admin', password:'admin'
            >>>new_account.json()
            {'username': 'admin', 'is_admin': 1, 'available_money': 200}

  -   En aquest moment podem fer un cop d'ull a l'aspecte d'una contrasenya amb hash:

			>>>new_account.password
            '$6$rounds=656000$GelL4xX9ZikfYQ7r$C81e1B9ic.1kXDn3DO2HlsRDazIIWWxI36pIj5cWEkLPDNtPsul/JU8Id1nyWRieZfqK90rv5Dy7zP4OcQG4s1' 
			  >>>from sqlalchemy import create_engine
            >>>from sqlalchemy.orm import sessionmaker
            >>>engine = create_engine('sqlite:///data.db')
            >>>Session = sessionmaker(bind=engine)
            >>>session = Session()
            >>>session.add(new_account)
            >>>session.commit()
            >>>session.close()
            
            
	- Comproveu si el nou usuari es troba a la taula d'accounts:

				>>>r = requests.get('http://127.0.0.1:5000/accounts')
       		>>>r.json()
            {'accounts': [{'username': 'npujol', 'is_admin': 0, 'available_money': 20},
              {'username': 'test', 'is_admin': 0, 'available_money': 200},
              {'username': 'user1', 'is_admin': 0, 'available_money': 20},
               {'username': 'user', 'is_admin': 0, 'available_money': 200}],
              {'username': 'admin', 'is_admin': 1, 'available_money': 200}]}

	-  Obteniu el token de /login:

			>>>r = requests.post('http://127.0.0.1:5000/login', data = {'username':'user', 'password':'1234'})
            >>>r
            <Response [200]>
            >>>r.json()
            {'token': 'eyJhbGciOiJIUzUxMiIsImlhdCI6MTU4ODQ0MzI2MywiZXhwIjoxNTg4NDQzODYzfQ.eyJ1c2VybmFtZSI6InVzZXIxIn0.Ljh3fTLiFlkVNatfdByiosdOUWesjDHMvxr_5SQeml0leGSdByVGFhl4_i7ZNQD0duu_TBdygcmqDYTLqf-XAQ'}
            >>>data = r.json()
            >>>data['token']
			'eyJhbGciOiJIUzUxMiIsImlhdCI6MTU4ODQ0MzI2MywiZXhwIjoxNTg4NDQzODYzfQ.eyJ1c2VybmFtZSI6InVzZXIxIn0.Ljh3fTLiFlkVNatfdByiosdOUWesjDHMvxr_5SQeml0leGSdByVGFhl4_i7ZNQD0duu_TbdygcmqDYTLqf-XAQ' 
           	

	- Comproveu si el token prové del nostre usuari (el farem servir en altres exercicis):


		    >>>from itsdangerous import (TimedJSONWebSignatureSerializer as Serializer, BadSignature, SignatureExpired)
            >>>my_token = data['token']
            >>>secret_key = "1q2s3f5g7jggujbffrhnbcdgh78jbhd"
            >>>s = Serializer(secret_key)
            >>>data = s.loads(my_token)
            >>>data
            {'username': 'user'}   
            
### EndPoints amb comprovació de permisos.
 
Fins ara, totes les dades estaven disponibles per a tots els usuaris que podien fer una sol·licitud. A localhost això no és un problema, però al desplegament permetríem a tots els usuaris que puguin fer una sol·licitud afegir, modificar o eliminar elements de la nostra base de dades.
Mitjançant un simple HTTPauth i tokens evitarem aquest tipus de sol·licituds no desitjades.
Instal·leu HTTPAuth: 

	  pip3 install Flask-HTTPAuth
	  
Definiu dues funcions a "models/accounts.py" fora del AccountsModel classe, com:

```python
from flask_httpauth import HTTPBasicAuth
from flask import g

auth = HTTPBasicAuth()

#AccountsModel CODE

 
@auth.verify_password
def verify_password(token, password):
    #CODE HERE

@auth.get_user_roles
def get_user_roles(user):
    #CODE HERE
```

### Exercici 3:

1.  Afegeix codi a **verify\_password(token,password)**:
	- En el nostre cas no utilitzarem la contrasenya, sinó l’estructura predeterminada que utilitza la biblioteca HTTPBasicAuth.
	- Donat un token, hem de retornar el compte (objecte sencer) relacionat amb ell (utilitzeu verify\_auth\_token). Si l'usuari existeix, torneu-lo i feu una còpia en una variable global de Flask anomenada g (`from flask import g` `g.user = usuari`). Aquesta variable ens ajudarà a compartir informació entre mètodes i durarà tota la vida de la sol·licitud. Si l'usuari no existeix, no cal retornar res.

	
2.  Afegeix codi a **get\_user\_roles(user)**:
	-  Donat un objecte "AccountsModel" (aquí "user") retorna \['admin'\] si aquest usuari és is\_admin = 1 o \['user'\] en cas contrari.

3. Afegiu el decorador relacionat amb les funcions anteriors per als mètodes post de `resources/orders.py` (importeu tots els mòduls necessaris)

	```python
		@auth.login_required(role='user')
        def post(self, username):  
	```

	Aquest decorador cridarà **verify\_password** i, com que es defineix un rol, també cridarà **get\_user\_role**. Només podrà utilitzar el mètode POST si és un usuari "registrat" ​​amb rol "usuari". Afegirem un token vàlid al mètode POST per a poder validar aquest usuari.

	- Afegiu seguretat per assegurar-vos que el nom d'usuari passat al endpoint i el nom d'usuari que ha generat el token siguin el mateix usuari. Utilitzeu la variable flask g generada per **verify\_password** (g.user.username). Si només fem servir un decorador com a `@auth.login_required()`, només es comprovarà si hi ha un usuari registrat (si té un token vàlid).

	- Si no és el mateix, torneu un "missatge" descriptiu amb codi 400. 

	- Si no ho heu fet abans, assegureu-vos que l'usuari tingui prou diners per comprar i que hi hagi prou bitllets disponibles i torneu el "missatge" descriptiu i el codi si passa això. Comproveu aquests valors fins i tot si us ocupeu d'això a frontend (mai no sabeu quines aplicacions poden utilitzar la vostra API)

	Podeu comprovar que aquesta part funciona correctament seguint aquests exemples de terminal, però primer obteniu un token vàlid de `/login` i després:

 		>>>from requests.auth import HTTPBasicAuth
    	>>>r = requests.post('http://127.0.0.1:5000/order/user', data = {'id_show':2, 'tickets_bought':2},auth=HTTPBasicAuth(my_token, ''))
    	>>>r
    	<Response [201]>

   		 >>>r.json()
    	{'show_city': 'Barcelona',
     	'show_date': '2020-07-03',
     	'show_name': 'Festival Cruilla 2020',
    	 'id': 6,
    	 'tickets_bought': 2,
    	 'username': 'user'}
     
  	Proveu el mateix però amb endponits amb nom d'usuari diferents:
  
 		>>>r = requests.post('http://127.0.0.1:5000/order/npujol', data = {'id_show':2, 'tickets_bought':2},auth=HTTPBasicAuth(my_token, ''))
   		 >>>r
   		 <Response [400]>
    	>>>r.json()
    	{"message": "Bad authorization user"}
4. Afegiu el decorador `login_ required(role=’admin’)` a tots els mètodes que creieu que només podrien restringir-se a l'administrador (normalment relacionats amb afegir, suprimir o modificar dades).
Feu una prova manual addicional per assegurar-vos que la vostra API funciona correctament amb requests o Postman.

Login Frontend
--------------

Comencem pel disseny del component. En primer lloc, creeu-ne un de nou
component anomenat `Login.vue`. A més, aneu al fitxer `index.js` i afegiu la
ruta:

```html
import Vue from 'vue'
import Router from 'vue-router'
import Shows from '@/components/Shows.vue'
import Login from '@/components/Login.vue'

Vue.use(Router)

export default new Router({
  mode: 'history',
  base: process.env.BASE_URL,
  routes: [
    {
      path: '/',
      name: 'Shows',
      component: Shows
    },
    {
      path: '/userlogin',
      name: 'Login',
      component: Login
    }
  ]
})
```
Un cop l'estructura estigui llesta, podem dissenyar la vista d'inici de sessió com:
![image](figures/image015.png)


Com podeu veure, tenim dos textos d’entrada i tres botons.
Si parem atenció a cada botó:

Botó Log In
-------

Comprova si el nostre nom d’usuari i contrasenya ja estan registrats. Per comprovar això, primer hem de desar el nom d'usuari i la contrasenya escrits per l'usuari.
Per fer-ho, podem utilitzar `vmodel=”username”` i `vmodel=”password”`:

```html
	<div class="form-label-group">
      <label for="inputEmail">Username</label>
      <input type="username" id="inputUsername" class="form-control"
      placeholder="Username" required autofocus v-model="username">
    </div>
    <div class="form-label-group">
      <br>
      <label for="inputPassword">Password</label>
      <input type="password" id="inputPassword" class="form-control"
      placeholder="Password" required v-model="password">
    </div>

```
On el nom d'usuari i la contrasenya són variables creades a `data()-> return`.

	data () {
        return {
          username: '',
          password: '',
          
Per comprovar l’usuari, hem de fer POST a /login per obtenir el token que utilitzarem més endavant:

```html
checkLogin () {
          const parameters = {
            username: this.username,
            password: this.password
          }
          const path = `http://localhost:5000/login`
          axios.post(path, parameters)
            .then((res) => {
              this.logged = true
              this.token = res.data.token
              this.find_match = true
              this.getAccount()
            })
            .catch((error) => {
              // eslint-disable-next-line
              console.error(error)
              this.user = ''
              alert('Username or Password incorrect')
            })
        },
```
   D’altra banda, hi ha una eina per canviar la ruta actual del component
un altre:

	    this.$router.replace({ path: '/', query: { username: this.username, logged: this.logged } })

Necessitareu informació sobre l’usuari que la passem mitjançant una consulta. "username" conté el nom d'usuari actual, "logged" és un booleà que mostra si l'usuari ha iniciat la sessió correctament i altres característiques que implementarem en el proper exercici.

Per consumir la informació de la consulta des de la vista `shows`, podem utilitzar la línia següent a `created()`:

	 created () {
        this.logged = this.$route.query.logged
        this.username = this.$route.query.username
        
### Exercici 4:
 
 1. Creeu un usuari mitjançant el shell de Flask si encara no teniu cap usuari a la base de dades creat. 
 2. Creeu un mètode getAccount() que faci un GET al backend per mirar si l'usuari, que ha iniciat sessió, és administrador o no i deseu-lo en una variable anomenada `is_admin`
 3. Creeu una alerta per mostrar si l’usuari ha iniciat la sessió.
 4. Canvieu la ruta actual a '/' desant la informació actual mitjançant la query i consumeix la informació del component Show '/' a created ():
 	 -  username

    -   logged

    -   is\_admin

    -   token

Botó Create Account
--------------

![image](figures/image016.png)

Aquest botó és un formulari on l'usuari introduirà les seves dades i les enviarà. El botó Envia crida a un mètode POST on les dades es guarden a la taula de comptes.
Abans d’enviar POST hauríem de plantejar-nos com obtenir les dades a enviar. Per obtenir aquestes dades, podem utilitzar Forms i emmagatzemar-los en un objecte.
En el nostre formulari, hem de recopilar la informació necessària per enviar-la a Flask per POST o PUT.
Primer de tot, hem de crear un objecte per emmagatzemar les dades:

```html
addUserForm: {
        username: '',
        password: ''
      }
```
A continuació, creeu un mètode d'objecte inicialitzador:

```html
initForm () {
      this.addUserForm.username = ''
      this.addUserForm.password = ''
    },
```
Després de definir l’objecte, s’ha de completar mitjançant un formulari (<https://bootstrap-vue.org/docs/components/form>).
Després, mitjançant el mètode onSubmit hauríem de cridar el mètode a POST. Finalment, crideu al mètode initForm per reiniciar els paràmetres.

### Exercici 5:

1. Creeu un formulari per desar les dades
2.  Creeu un mètode POST per enviar les noves dades d'usuari mitjançant path i paràmetres:

 ```html
    const path = 'http://localhost:5000/account'
    ```

    ```html
    const parameters = {
          username: this.addUserForm.username,
          password: this.addUserForm.password
          } 
```

3. Alerta a l'usuari si el compte s'ha creat o ja existeix

Botó Back To Shows
--------------
Torna a la pàgina de l'espectacle, però envia informació diferent amb la query,
tal com ho fa el botó SIGN IN.  En aquest cas, logged = False i el nom d'usuari no són necessaris.

```html
this.logged = false
this.$router.replace({ path: '/', query: { logged: this.logged } })
```
### Exercici 6:

1. Creeu un mètode per substituir la ruta i enllaceu el botó per tornar als espectacles.


Comprar amb seguretat
---------------------

Per proporcionar seguretat per a les compres de cada usuari, hauríem d’utilitzar el token obtingut quan l’usuari prem INICIAR SESSIÓ. Per fer-ho, a l’addPurchase (paràmetres) que ja heu creat a l’última sessió, canvieu-lo per:

```html
addPurchase (parameters) {
      const path = `http://localhost:5000/order/${this.username}`
      axios.post(path, parameters, {
        auth: {username: this.token}
      })
        .then(() => {
          console.log('Order done')
        })
        .catch((error) => {
          // eslint-disable-next-line
          console.log(error)
          this.getEvents()
        })
    },
```

Fixeu-vos que estem utilitzant paràmetres d'autenticació amb token com a nom d'usuari.


### Informació d’usuaris i espectacles


Per deixar clara la interacció de l'usuari, hauríem de mostrar-li la informació següent:

### Exercici 7:

1. A la visualització d'espectacles, mostreu els diners disponibles i les entrades afegides a la cistella. Recordeu que els diners disponibles sempre són controlats per
    la base de dades i no ha de canviar amb la interacció de VUE, només cal canviar després de comprar entrades o actualitzar la pàgina.

    ![image](figures/image017.png)
    
2. A la visualització d'espectacles, mostreu les entrades disponibles per a cada esdeveniment. Com que els diners disponibles, sempre són controlats per la base de dades i  no ha de canviar amb la interacció de VUE, només canvieu-ho després de comprar entrades o actualitzar la pàgina.