Sessió 3
=========

Diseeny Web
-----

En aquesta part, desenvoluparem el frontend de la nostra aplicació.
Al final d'aquesta part, hauríem de veure el següent disseny:

**Vista LOG IN**

![image](figures/signin.png)

**Vista Shows sense usuari Loggat**

![image](figures/image002.png)

**Vista shows amb l'usuari loggat**

![image](figures/image003.png)

**Vista de la Cistella de la Compra**

![image](figures/image004.png)

Introducció a Vue
--------

Vue és un framework progressiu per construir interfícies d'usuari, és a dir,
al costat del client. Tot i que Vue és més senzill que React o Angular, és
extremadament potent i es pot utilitzar per crear aplicacions avançades i
ofereix la creació de projectes de manera estructurada. Consisteix en un conjunt de
biblioteques opcionals i biblioteques de tercers, i compta amb una
comunitat en creixement. A més, Vue s'està convertint en molt popular i té un futur brillant.

Instal·lació de Vue
-----
Els següents passos s'han desenvolupat a la versió  4.5.12 de vue/cli, node.js versió 14.16.1 i npm versió v6.14.12.
Després d’instal·lar Vue, comprovem la versió del node:

	node --version

Si encara no teniu el node instal·lat o teniu una versió antiga, aneu a
<https://nodejs.org/en/download/> i instal·leu el paquet. Per verificar la instal·lació, feu servir les línies d’ordres següents:

	node --version
    npm --version
    
En cas que estigueu a Linux, haureu d'instal·lar Node Version Manager (NVM).
Us permetrà triar una versió de node específica. Per instal·lar-lo, podeu seguir aquesta guia:
<https://phoenixnap.com/kb/install-latest-node-js-and-nmp-on-ubuntu>. 

Hi ha alguns mètodes d'instal·lació per instal·lar Vue, en aquest cas l'instal·larem a través de Vue CLI.

 	 npm install -g @vue/cli
     npm install -g @vue/cli-init
    
Per obtenir més assistència, consulteu la següent guia d'instal·lació:
<https://cli.vuejs.org/guide/installation.html>. 
Després de la instal·lació, comproveu amb la línia d'ordres:

    vue --version

Crear i configurar un entorn de projecte
----
Primer de tot, aneu a la carpeta desitjada on voleu guardar el projecte. Després d'això, executeu a la línia d'ordres

    vue init webpack <name-project>

per exemple: 
	
	vue init webpack frontend
Seleccioneu la configuració tal com es mostra a
imatge següent:

![image](figures/image005.png)

Podeu interactuar amb les fletxes, les tecles d'entrada i l'espai.
Un cop creat el projecte, parem atenció als fitxers principals. La carpeta `/ src` té la següent estructura:

![image](figures/src.png)

- **App.vue**: s'encarrega de representar els components

- **assets**: on deseu tots els recursos, com ara imatges

- **components**: tots els components dels projectes. Tots els components tenen la seva pròpia plantilla html i codi JavaScript

- **main.js**: inicialitza i configura l'aplicació Vue

- **index.js**: enruta els components del vostre projecte

Hello world in Vue
-----------
Per executar la nostra primera aplicació Vue:

    cd <name-project>
    npm run dev
   
En cas que feu servir el WebStorm de JetBrains, obriu el projecte anomenat frontend i creu una configuració  amb el boto de Run. Feu que sigui del tipus npm i l'script dev.

Després d'executar aquestes línies d'ordres, aneu a <http://localhost:8080/> al navegador.

![image](figures/image006.png)

![image](figures/image007.png)

Crear un component
------

Hem vist com executar un projecte. Ara veurem com crear el nostre propi component. Canvieu la plantilla html del fitxer `App.vue` com:

```html
<template>
    <router-view/>
</template>
```

A més, creeu un component nou anomenat `Shows.vue` a la carpeta de components. En aquest fitxer, copieu i enganxeu el codi següent:

```html
<template>
<div id="app">
<h1> {{ message }} </h1>
</div>
</template>

<script>

export default {
  data () {
    return {
      message: 'My first component'
    }
  }
}

</script>
```

Com podeu veure aquí, tenim dos blocs anomenats "template" i "script". Com hem esmentat anteriorment, el primer bloc correspon a la visualització en html i el segon pertany al codi JavaScript. A "return" podem declarar les variables que utilitzarem al codi. També podeu interactuar incloent-hi les referències de codi a la plantilla html.
Paral·lelament, aneu a `index.js` per encaminar el nou component:

```html
import Vue from 'vue'
import Router from 'vue-router'
import Shows from '@/components/Shows'

Vue.use(Router)

export default new Router({
  mode: 'history',
  base: process.env.BASE_URL,
  routes: [
    {
      path: '/',
      name: 'Shows',
      component: Shows
    }
  ]

})
```
Comprovem el nostre nou component <http://localhost:8080/>

![image](figures/image008.png)

Bootstrap
---------

Bootstrap (<https://www.w3schools.com/whatis/whatis_bootstrap.asp>) és el framework CSS més popular per al desenvolupament de llocs web responsius i per a mòbils. Conté plantilles de disseny basades en CSS i JavaScript per a tipografia, formularis, botons, navegació i altres components de la interfície.


Per instal·lar-lo al nostre projecte, executeu la comanda següent en la línia d'ordres:

	npm install --save bootstrap-vue
	
A més, descarregueu els fitxers compilats [aquí] (https://getbootstrap.com/docs/4.0/getting-started/download/). Extreu els fitxers a una carpeta nova anomenada bootstrap dins del vostre projecte:

![image](figures/image009.png)

Després de la instal·lació, configureu el fitxer `main.js` important el Bootstrap:


```html
import BootstrapVue from 'bootstrap-vue'
import '@/../bootstrap/css/bootstrap.css'
import Vue from 'vue'
import App from './App.vue'
import router from './router'

Vue.use(BootstrapVue)
Vue.config.productionTip = false

new Vue({
  router,
  render: (h) => h(App)
}).$mount('#app')
```

Ara podem consumir les plantilles Bootstrap predefinides.``

Mètodes
-----

A part de les variables, també podem definir mètodes i utilitzar-los en un bloc d'html. Definim una variable anomenada `tickets_bought` i un mètode
per incrementar aquest valor mitjançant un botó de Bootstrap a `Shows.vue`.

```html
<template>
  <div id="app">
    <h1> {{ message }} </h1>
    <button class="btn btn-success btn-lg" @click="buyTickets"> Buy ticket </button>
    <h4> Total tickets bought: {{ tickets_bought }} </h4>
  </div>
</template>

<script>
export default {
  data () {
    return {
      message: 'My first component',
      tickets_bought: 0
    }
  },
  methods: {
    buyTickets () {
      this.tickets_bought += 1
    }
  }
}
</script>
```

Com podeu veure, també podeu cridar a mètodes en un bloc html mitjançant `@click = "buyTickets"`, com en aquest cas.

### Exercici 1

Declareu un mètode per restar el total d’entrades comprades de les entrades totals. (i el botó d’interacció que s’anomena `Return Ticket`). A més, afegiu una variable `money_available` i `price_event` i resteu el preu de cada espectacle a la variable `money_available`. Utilitzeu el preu, els diners disponibles i les entrades diponibles que vulgueu. Mostra-ho al lloc web.

### Exercici 2

Desactiveu el botó de compra si els diners disponibles no són suficients per comprar un ticket.
D’altra banda, desactiveu el botó de ticket si el total de bitllets és 0.
Per fer-ho, podeu utilitzar la propietat `:disabled="variable1 < variable2">` en els botons.

Conectant Vue i Flask
--------------

Ara hem de connectar el Framework de Frontend de VUE (node.js) amb el Framework de Backend de Flask (python). Però primer haurem de permetre que es puguin fer "requests" des del client (javascript del navegador) al servidor web. Per això haurem de permetre-ho en el nostre backend de flask.

### Activant CORS (Cross-Origin Resource Sharing)

CORS és el mecanisme per gestionar les sol·licituds d’origen creuat. Una sol·licitud de recurs de fora de l’origen es coneix com a sol·licitud d’origen creuat. En aquesta pràctica, crearem una aplicació de frontend que sol·licitarà informació a Flask. Per habilitar l'accés, hem d'instal·lar la llibreria `flask-cors` de python.

	pip install flask-cors

Després d'això, importeu el CORS i configureu l'aplicació per tal d'acceptar sol·licituds de tots els orígens:

```python
from flask_cors import CORS

app = Flask(__name__)
app.config.from_object(__name__)

api = Api(app)

CORS(app, resources={r'/*': {'origins': '*'}})

app.config['SQLALCHEMY_DATABASE_URI'] = 'sqlite:///data.db'
app.config['SQLALCHEMY_TRACK_MODIFICATIONS'] = False 
migrate = Migrate(app, db)
db.init_app(app)
```

Així doncs, així concediu accés a l’aplicació Flask des de l’exterior.

### Configurant els directoris static i template de Flask.


Primer de tot, aneu a la vostra aplicació Vue i compileu el vostre codi amb la línia següent:

	npm run build
	
Amb WebStorm podeu crear una nova configuració d'execució npm run i amb la comanda build.

Es crearà una nova carpeta anomenada **dist** on podrem trobar el codi frontend del nostre projecte (HTML, CSS, JS). Ara, la nostra aplicació Vue té l'estructura següent:

![image](figures/image012.png)


Ara és hora d’indicar a Flask els camins a consumir des de `app.py`:

```python
app = Flask(__name__,
         static_folder="frontend/dist/static",
         template_folder="frontend/dist")
```

I declareu una ruta per renderitzar la plantilla:

```python
from flask import render_template

@app.route('/')
def render_vue():
    return render_template("index.html")
```


Si executeu l'aplicació Flask i obriu <http://127.0.0.1:5000/>, hauríeu de veure l'última configuració de Vue però allotjada a l'aplicació Flask:

![image](figures/image013.png)
