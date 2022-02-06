---
layout: page
title: Heroku Deployment
---
---

# From Development to Production. 

## Deploying on Heroku

Heroku was one of the first platform as a service providers. It started as a hosting option for Ruby based applications, but then grew to support many other languages like Java, Node.js and our favorite, Python.

In essence, deploying a web application to Heroku requires just uploading the application using git. Heroku looks for a file called Procfile in the application's root directory for instructions on how to execute the application. For Python projects Heroku also expects a requirements.txt file that lists all the module dependencies that need to be installed.



1. Creating Heroku account
Before we can deploy to Heroku we need to have an account with them. So head over to [heroku.com](https://www.heroku.com/) and create an account.
Once you are logged in you have access to a dashboard, where all your apps can be managed. 

2. Installing the Heroku client

Heroku offers a tool called the "Heroku client" that we'll use to create and manage our application. This tool is available for Windows, Mac OS X and Linux. If there is a [Heroku toolbelt](https://toolbelt.heroku.com/) download for your platform then that's the easiest way to get the Heroku client tool installed.

The first thing we should do with the client tool is to login to our account:

    $ heroku login
    

### Creating a Heroku app

To create a new Heroku app you just use the create command from the root directory of the application 

      $ heroku apps:create <<nomgrup>>-ticketmonster
      
Link your local repository to heroku. 

	   $ heroku git:remote -a <<nomgrup>>-ticketmonster 


At the end of the deployment your should have these files to the root directory of your app:

* requirements.txt
* Procfile
* config.py
* /templates/index.html
* /static
* /models
* /resources
* app.py
* db.py

### The requirements.txt
    
Heroku does not provide a web server. Instead, it expects the application to start its own server on the port number given in environment variable $PORT.

We know the Flask web server is not good for production use because it is single process and single threaded, so we need a better server. The Heroku tutorial for Python suggests `gunicorn`, a pre-fork style web server written in Python, so that's the one we'll use.

The `gunicorn` web server needs to be added to the `requirements.txt` inside the app directory. Add also all pyhon packages dependences for your project. The `requirements.txt` file should be in the root of the project and should be like this. To know the versions of your specifics packages related to Flask, use `pip freeze`. Example:

```
gunicorn==19.7
python-decouple==3.3
whitenoise==3.3.1
psycopg2==2.8.2
Flask==1.1.2
Flask-SQLAlchemy==2.4.1
Flask-RESTful==0.3.8
Flask-Cors==3.0.8
Flask-Migrate==2.5.3
Flask-HTTPAuth==4.0.0
passlib==1.7.2
itsdangerous==1.1.0
```

### The Procfile
The last requirement is to tell Heroku how to run the application. For this Heroku requires a file called  just `Procfile` in the root folder of the Github.

This file is extremely simple, it just defines process names and the commands associated with them (file Procfile):

```
   web: gunicorn -w 1 -k gthread --threads 4 app:app
```

We are going to use one worker of type gthread and 4 threads for our app. You can tune the thread value. If you are not using Locks (see below) set --threads to 1. 

The web label is associated with the web server. Heroku expects this task and will use it to start our application.

### The config.py

We gonna need a `config.py` file to control the development and production envoironment variables.
 
```python 

from decouple import config
class Config:
    pass

class ProductionConfig(Config):
    DEBUG = False
    SQLALCHEMY_DATABASE_URI = config('DATABASE_URL', default='localhost')
    SQLALCHEMY_TRACK_MODIFICATIONS = False
    STATIC_FOLDER = "/static"
    TEMPLATE_FOLDER = "/templates"
    SECRET_KEY = config('SECRET_KEY', default='localhost')

class DevelopmentConfig(Config):
    DEBUG = True
    SQLALCHEMY_DATABASE_URI = 'sqlite:///data.db'
    SQLALCHEMY_TRACK_MODIFICATIONS = False
    STATIC_FOLDER = "/P2_VUE_WEBPACK/frontend/dist/static"
    TEMPLATE_FOLDER = "/P2_VUE_WEBPACK/frontend/dist"
    SECRET_KEY = "kdsfklsmfakfmafmadslvsdfasdf"

config = {
    'development': DevelopmentConfig,
    'production': ProductionConfig
}

```

Now we have to modify the `app.py`: delete all the configuration variables and load them from config.py depending on the enviroment:

```python 
from decouple import config as config_decouple
from config import config
	
app = Flask(__name__)
environment = config['development']
if config_decouple('PRODUCTION', cast=bool ,default=False):
    environment = config['production']
   
app.config.from_object(environment) 
	
```
Remove the secret_key from `db.py` too.
In model/accounts remove the `secret_key import from db.py` and add this code:

```python
from flask import g, current_app
```
and replace `secret_key` occurrence for `current_app.secret_key`

Add the Config varaibles PRODUCTION set to True and your own SECRET_KEY with :
```
 heroku config:set PRODUCTION=True  -a <<nomgrup>-ticketmonster>

 heroku config:set SECRET_KEY=Your_own_key -a <<nomgrup>-ticketmonster>
```

Chek them with 

`heroku config -a <<nomgrup>-ticketmonster>`


### Database
We should use a production database. Heroku comes with a postgress free instance that we can use. Go to your Heroku desktop and [install the postgress addon](https://elements.heroku.com/addons/heroku-postgresql). 

	
`heroku  addons:create heroku-postgresql:hobby-dev -a <<nomgrup>-ticketmonster>`
	


### Preparing our previous code for production.

We need to prepare our previous code for production. 

* Replace all the links `localhost:5000` in the vue components for `https://<<nomgrup>-ticketmonster>.herokuapp.com/`.
* Regenerate the vue `/dist` folder: 
	`npm run build`

#### Creating a Singleton Lock SafeThread to be used in concurrent write requests operation

Our gunicorn settings is monoprocess (one worker) and multiThread, so we have to ensure our **post**, **delete** and **put** methods are safeThread using locks. These locks has to be shared with the rest of threads accessing to the same restful endpoint (so they have to be Singleton) and the implemetation of this Sigleton has to be SafeThread to. Here's the code of this Lock class: 

`lock.py` 

```python
import threading

class my_Lock(object):
   __singleton_lock = threading.Lock()
   __singleton_instance = None

   @classmethod
   def getInstance(cls):
      if not cls.__singleton_instance:
         with cls.__singleton_lock:
            if not cls.__singleton_instance:
               cls._singleton_instance = cls()
      return cls.__singleton_instance

   def __init__(self):
      """ Virtually private constructor. """
      if my_Lock.__singleton_instance != None:
         raise Exception("This class is a singleton!")
      else:
         my_Lock.__singleton_instance = self
         self.lock = threading.Lock()

lock = my_Lock.getInstance()
```

for example in `resources/orders.py`

```python
from lock import lock
....
    def post(self, username): 
        data = self.parser.parse_args()
        with lock.lock:
        	....
		 return ....
```

### Deploying the code

Now you can deploy your code:

Add, commit and push all your local files to your repository

```
git add *
git commit -a -m"ready to deploy"
```

If your app is not in the root directory in your github repo, link the subdirectory where it is:

`git subtree push --prefix subdirectori_practica_github heroku master  `

If your app is in the root:

`git push heroku master`



If you have no errors in the process, now you can create the Database tables:

`heroku  run bash -a <<nomgrup>-ticketmonster>`

```bash 
flask db init
flask db migrate -m "Initial migration."
flask db upgrade
```

Look into your sqlite for your admin account and write down the values.

```
sqlite3 data.db 
select * from accounts;
```

Now, install in your local machine the postgress client `psql` and execute the heroku pg:psql command  and insert your admin account from the sqlite db. 

```
heroku pg:psql -a <<nomgrup>-ticketmonster>
INSERT INTO accounts VALUES(1,'admin','THE_HASH....',200);
```


Now the application is online:

`heroku open -a <<nomgrup>-ticketmonster>`

To check the logs you can access to them:

`heroku logs -a <<nomgrup>-ticketmonster>`


    
### Be aware of some limitations of heroku free plan:

- The application “sleeps” after 30 minutes of inactivity. The first access after it might be slow.
- The free PostgreSQL database has a limitation of 10K rows.  
- Heroku deploys in `https` protocol, instead of `http`. So all the links must be changed to `https` protocol 

(Note: with https://education.github.com/pack you have Hobby Dino plan for free!)


