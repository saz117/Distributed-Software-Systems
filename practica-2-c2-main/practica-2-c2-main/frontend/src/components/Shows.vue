<!-- Crear un component -->

<!-- Metodos -->
<template>
  <div id="app">
    <div class="container">
      <div class='page-header float-right'>
        <div class = "row no-gutters">
          <div class='btn-toolbar'>
            <div class='btn-group'>
              <button v-if="logged" class="btn btn-danger btn-lg"
                      @click=logout() > Logout</button>
              <button v-if="!logged" class="btn btn-primary btn-lg"
                      @click=login() > Login</button>
            </div>
          </div>
        </div>
        <div class = "row no-gutters">
          <h5>Total tickets: {{ this.getSuma() }}</h5>
        </div>
        <div class = "row no-gutters">
          <h5>Available money: {{ this.money }} €</h5>
        </div>
      </div>
    </div>
    <div class = "container">
      <img src="../../../figures/cabecera.png" alt="cabecera" width="100%" style="margin-bottom: 30px;"><br>
      <button v-if="logged && is_admin==1" class="btn btn-success btn-lg" @click=addNewEvent() style="margin: 25px;">Add New Event</button>
      <button v-if="logged && is_admin==1" class="btn btn-success btn-lg" @click=updateEvent() style="margin: 25px;">Update Event</button>
    </div>
    <div v-if=isShowingCart class="container">
      <div class="row">
          <div class="col-lg-4 col-md-6 mb-4" v-for="(show) in shows" :key="show.id">
          <!--Cards-->
          <div class="card">
            <!--<img class="card-img-top" :src="show.image" alt="Card image cap">-->
            <img class="card-img-top" :src="img" alt="Card image cap">
            <div class="card-body">
          <br>
          <h3 class = "card-title">{{ show.name }}</h3>
              <div v-for="(artist) in show.artists" :key="artist.id">
            <h5>{{ artist.name }}</h5>
          </div>
          <h6>{{ show.city }}</h6>
          <h6>{{ show.place.name }}</h6>
          <h6>{{ show.date }}</h6>
          <h6>{{ show.price }} € </h6>
          <div class="card-footer">
          <h5>{{ show.total_available_tickets }}</h5>
          <button v-if="logged && is_admin==0"
                  class="btn btn-success btn-lg" @click=addEventToCart(show)
                  :disabled="money_available < price || tickets_availables < 1"> Add to cart</button>
            <p></p>
            <b-button v-if="logged && is_admin==1" variant="dark" @click=showWhereModifyArtist(show)>Add Artist to Event</b-button>
            <p></p>
            <b-button v-if="logged && is_admin==1" variant="dark" @click=eventWhereModifyArtist(show)>Delete Artist in Event</b-button>
            <p></p>
            <b-button v-if="logged && is_admin==1" variant="danger" @click=removeShow(show)>Delete event</b-button>
          </div>
            </div>
          </div>
        </div>
      </div>
      <button v-if="logged && is_admin==0"
              class="btn btn-success btn-lg"
              @click=veureCistella() > Veure cistella</button>
    </div>
  <div v-else class="container">
  <table v-if="shows_added.length > 0" style="margin: auto;">
  <thead>
      <tr>
        <th>Event Name</th>
        <th>Quantity</th>
        <th>Price (€)</th>
        <th>Total</th>
        <th></th>
      </tr>
      </thead>
      <tr v-for="(show) in shows_added" :key="show.id">
        <td>{{show.name}}</td>
        <td>{{entradas[shows_added.indexOf(show)]}}
        <button class="btn btn-danger btn-lg"
                    @click=decrementarEntrada(show)
                    :disabled="entradas[shows_added.indexOf(show)] < 1 ">-</button>
          <button class="btn btn-success btn-lg"
                    @click=incremenetarEntrada(show)
                    :disabled="entradas[shows_added.indexOf(show)] >= show.total_available_tickets ">+</button>
        </td>
        <td>{{show.price}}  €</td>
        <td>{{entradas[shows_added.indexOf(show)]*show.price}} €</td>
        <td><button class="btn btn-danger btn-lg"
                    @click=eliminarDelCarro(show) > Delete from cart</button></td>
      </tr>
    </table>
    <p v-else>Your cart is currently empty.</p>
    <button class="btn btn-success btn-lg"
              @click=veurePanellEnrere() > Enrere</button>
    <button class="btn btn-success btn-lg"
              @click=addCompras()
              :disabled="shows_added.length < 1 "> Finalitza Compra</button>
              <!--@click=addCompras()  TODO-REPAS-->
    </div>
    <!--Formulario1-->
    <b-modal ref="addShowModal"
      id="event-modal"
      title="Add new event"
      hide-footer>
      <b-form @submit="onSubmit" @reset="onReset" v-if="show">
        <b-form-group label="Name:" label-for="input-1">
        <b-form-input
          id="input-1"
          v-model="addShowForm.name"
          placeholder="Enter event name"
          required
        ></b-form-input>
        </b-form-group>

        <b-form-group label="Price:" label-for="input-2">
        <b-form-input
          id="input-2"
          v-model="addShowForm.price"
          type="number"
          placeholder="Enter price"
          required
        ></b-form-input>
        </b-form-group>

        <b-form-group label="Date:" label-for="input-3">
        <b-form-datepicker
          id="input-3"
          v-model="addShowForm.date"
          required
        ></b-form-datepicker>
        </b-form-group>

        <b-form-group label="Place:" label-for="input-6">
        <b-form-input
          id="input-6"
          v-model="addShowForm.place"
          placeholder="Enter name place"
          required
        ></b-form-input>
        </b-form-group>

        <b-form-group label="city:" label-for="input-8">
        <b-form-input
          id="input-8"
          v-model="addShowForm.city"
          placeholder="Enter city"
        ></b-form-input>
        </b-form-group>

        <b-form-group label="country:" label-for="input-9">
        <b-form-input
          id="input-9"
          v-model="addShowForm.country"
          placeholder="Enter country"
        ></b-form-input>
        </b-form-group>

        <b-form-group label="Tickets:" label-for="input-7">
        <b-form-input
          id="input-7"
          v-model="addShowForm.total_available_tickets"
          type="number"
          placeholder="Enter tickets"
        ></b-form-input>
        </b-form-group>

        <b-button type="submit" variant="primary">Submit</b-button>
        <b-button type="reset" variant="danger">Reset</b-button>
      </b-form>
    </b-modal>

    <!--Formulario2-->
    <b-modal ref="editShowModal"
      id="event-modal"
      title="Update event"
      size="lg"
      hide-footer>
      <b-form @submit="onSubmitUpdate" @reset="onResetUpdate" v-if="show">
        <b-form-group label="Id:" label-for="input-0">
        <b-form-input
          id="input-0"
          v-model="editShowForm.id"
          @change="Cargarid()"
          type="number"
          placeholder="Enter id"
          required
        ></b-form-input>
        </b-form-group>

        <b-form-group label="Name:" label-for="input-1">
        <b-form-input
          id="input-1"
          v-model="editShowForm.name"
          placeholder="Enter event name"
        ></b-form-input>
        </b-form-group>

        <b-form-group label="Price:" label-for="input-2">
        <b-form-input
          id="input-2"
          v-model="editShowForm.price"
          type="number"
          placeholder="Enter price"
          required
        ></b-form-input>
        </b-form-group>

        <b-form-group label="Date:" label-for="input-3">
        <b-form-datepicker
          id="input-3"
          v-model="editShowForm.date"
        ></b-form-datepicker>
        </b-form-group>

        <b-form-group label="Place:" label-for="input-6">
        <b-form-input
          id="input-6"
          v-model="editShowForm.place"
          placeholder="Enter name place"
        ></b-form-input>
        </b-form-group>

        <b-form-group label="city:" label-for="input-8">
        <b-form-input
          id="input-8"
          v-model="editShowForm.city"
          placeholder="Enter city"
        ></b-form-input>
        </b-form-group>

        <b-form-group label="country:" label-for="input-9">
        <b-form-input
          id="input-9"
          v-model="editShowForm.country"
          placeholder="Enter country"
        ></b-form-input>
        </b-form-group>

        <b-form-group label="Tickets:" label-for="input-7">
        <b-form-input
          id="input-7"
          v-model="editShowForm.total_available_tickets"
          type="number"
          placeholder="Enter tickets"
          required
        ></b-form-input>
        </b-form-group>

        <b-button type="submit" variant="primary">Submit</b-button>
        <b-button type="reset" variant="danger">Reset</b-button>
      </b-form>
    </b-modal>

    <!--Formulario3-->
    <b-modal ref="addArtistModal"
      id="event-modal"
      title="Add artist"
      hide-footer>
      <b-form @submit="onSubmitAddArtistInShow" @reset="onResetAddArtistInEvent" v-if="show">

        <b-form-group label="Name Artist:" label-for="input-1">
        <b-form-input
          id="input-1"
          v-model="addArtistForm.name"
          placeholder="Enter artist name"
          required
        ></b-form-input>
        </b-form-group>

        <b-form-group label="Country artist:" label-for="input-6">
        <b-form-input
          id="input-6"
          v-model="addArtistForm.country"
          placeholder="Enter country artist"
          required
        ></b-form-input>
        </b-form-group>

        <b-form-group label="Genre artist:" label-for="input-7">
        <b-form-input
          id="input-7"
          v-model="addArtistForm.genre"
          placeholder="Enter genre artist"
          required
        ></b-form-input>
        </b-form-group>

        <b-button type="submit" variant="primary">Submit</b-button>
        <b-button type="reset" variant="danger">Reset</b-button>
      </b-form>
    </b-modal>

    <!--Formulario4-->
    <b-modal ref="deleteArtistModal"
      id="event-modal"
      title="Delete artist"
      hide-footer>
      <b-form @submit="onSubmitDeleteArtistInShow" v-if="show">

        <b-form-group label="Id Artist:" label-for="input-1">
        <b-form-input
          id="input-1"
          v-model="deleteArtistForm.id"
          placeholder="Enter artist id"
          required
        ></b-form-input>
        </b-form-group>

        <b-form-group label="Name Artist:" label-for="input-2">
        <b-form-input
          id="input-2"
          v-model="deleteArtistForm.name"
          placeholder="Enter artist name"
        ></b-form-input>
        </b-form-group>

        <b-button type="submit" variant="primary">Submit</b-button>
        <b-button type="reset" variant="danger">Reset</b-button>
      </b-form>
    </b-modal>
  </div>
</template>

<script>
import axios from 'axios'
export default {
  data () {
    return {
      message: 'My first component',
      img: 'https://esiro.es/wp-content/uploads/2018/08/aditya-chinchure-494048-unsplash.jpg',
      tickets_bought: 0,
      tickets_availables: 100,
      price: 28,
      money_available: 500,
      money: 0,
      shows_added: [],
      entradas: [],
      isShowingCart: true,
      logged: false,
      is_admin: 0,
      token: '',
      username: '',
      shows: [],
      addShowForm: {
        place: '',
        name: '',
        date: '',
        price: '',
        city: '',
        country: '',
        total_available_tickets: ''
      },
      show: true,
      editShowForm: {
        id: '',
        name: '',
        place: '',
        date: '',
        price: '',
        city: '',
        country: '',
        total_available_tickets: ''
      },
      show_to_modify: '',
      addArtistForm: {
        id: '',
        name: '',
        country: '',
        genre: ''
      },
      deleteArtistForm: {
        id: '',
        name: ''
      }
    }
  },
  methods: {
    /* Exerici 1 */
    buyTickets () {
      this.tickets_bought += 1
      this.refresh_ticket_money()
    },
    refresh_ticket_money () {
      this.tickets_availables -= 1
      this.money_available -= this.price
    },
    returnTickets () {
      this.tickets_availables += 1
      this.tickets_bought -= 1
      this.money_available += this.price
    },
    getImage () {
      return ''
    },
    addEventToCart (show) {
      if (!this.shows_added.includes(show)) {
        this.shows_added.push(show)
        this.entradas.push(1)
        console.log(this.shows_added)
      } else {
        const i = this.shows_added.indexOf(show)
        const e = this.entradas[i]
        this.entradas.splice(i, 1, e + 1)
      }
    },
    decrementarEntrada (show) {
      const i = this.shows_added.indexOf(show)
      const e = this.entradas[i]
      this.entradas.splice(i, 1, e - 1)
      if (this.entradas[i] === 0) {
        this.eliminarDelCarro(show)
      }
    },
    incremenetarEntrada (show) {
      const i = this.shows_added.indexOf(show)
      const e = this.entradas[i]
      this.entradas.splice(i, 1, e + 1)
    },
    eliminarDelCarro (show) {
      const i = this.shows_added.indexOf(show)
      /* Con el splice eliminamos posicion i, 1 elemento */
      this.shows_added.splice(i, 1)
      this.entradas.splice(i, 1)
    },
    getShows () {
      const path = 'https://c2-ticketmonster1.herokuapp.com/shows'
      axios.get(path)
        .then((res) => {
          this.shows = res.data.shows
        })
        .catch((error) => {
          console.error(error)
        })
    },
    finalizePurchase () {
      for (let i = 0; i < this.shows_added.length; i += 1) {
        const parameters = {
          id_show: this.shows_added[i].id,
          tickets_bought: this.entradas[i]
        }
        this.addPurchase(parameters)
      }
    },
    addPurchase (parameters) {
      const path = `https://c2-ticketmonster1.herokuapp.com/orders/${this.username}`
      axios.post(path, parameters, {
        auth: {username: this.token}
      })
        .then(() => {
          console.log('Order done')
        })
        .catch((error) => {
          // eslint-disable-next-line
          console.log(error)
          this.getShows()
        })
    },
    addCompras () {
      const path = `https://c2-ticketmonster1.herokuapp.com/orders/${this.username}`
      const arrshows = []
      for (let i = 0; i < this.shows_added.length; i += 1) {
        arrshows.push(this.shows_added[i].id)
      }
      const parameters = {
        id_show: arrshows,
        tickets_bought: this.entradas
      }
      axios.post(path, parameters, {
        auth: {username: this.token}
      })
        .then(() => {
          console.log('Orders done')
          alert("S'ha finalitzat correctament la compra")
          for (let i = 0; i < this.shows_added.length;) {
            this.eliminarDelCarro(this.shows_added[i])
          }
        })
        .catch((error) => {
          // eslint-disable-next-line
          alert("No s'ha pogut finalitzar la compra")
          console.log(error)
          this.getShows()
        })
    },
    veureCistella () {
      this.isShowingCart = false
    },
    veurePanellEnrere () {
      this.getAvailabeMoney()
      this.isShowingCart = true
    },
    login () {
      this.$router.replace({ path: '/userlogin' })
    },
    logout () {
      this.$router.replace({ path: '/' })
      this.logged = false
      this.is_admin = 0
      this.token = ''
      this.username = ''
      this.money = 0
      this.tickets_bought = 0
      this.entradas = []
    },
    getSuma () {
      let total = 0
      this.entradas.forEach(function (a) {
        total += a
      })
      console.log(total)
      return total
    },
    getParameters (str) {
      let params = new URLSearchParams(location.search)
      return params.get(str)
    },
    getAvailabeMoney () {
      if (this.username === '' || !this.logged) {
        return 0
      }
      const path = 'https://c2-ticketmonster1.herokuapp.com/account/' + this.username
      axios.get(path)
        .then((res) => {
          this.money = res.data.account.available_money
          this.is_admin = res.data.account.is_admin
          // return this.available_money
        })
        .catch((error) => {
          // eslint-disable-next-line
          console.error(error)
          this.username = ''
          this.logged = false
          alert('Username or Password incorrect')
        })
    },
    addNewEvent () {
      this.$refs['addShowModal'].show()
    },
    updateEvent () {
      this.$refs['editShowModal'].show()
    },
    onReset (evt) {
      evt.preventDefault()
      this.initForm()
      this.show = false
      this.$nextTick(() => {
        this.show = true
      })
    },
    onSubmit (evt) {
      evt.preventDefault()
      this.$refs.addShowModal.hide()
      const parameters = {
        name: this.addShowForm.name,
        date: this.addShowForm.date,
        price: this.addShowForm.price,
        place: this.addShowForm.place,
        city: this.addShowForm.city,
        country: this.addShowForm.country,
        total_available_tickets: this.addShowForm.total_available_tickets
      }
      this.addShow(parameters)
      this.initForm()
    },
    initForm () {
      this.addShowForm.place = ''
      this.addShowForm.name = ''
      this.addShowForm.date = ''
      this.addShowForm.price = ''
      this.addShowForm.city = ''
      this.addShowForm.country = ''
      this.addShowForm.total_available_tickets = ''
      this.editShowForm.id = ''
      this.editShowForm.name = ''
      this.editShowForm.place = ''
      this.editShowForm.date = ''
      this.editShowForm.price = ''
      this.editShowForm.city = ''
      this.editShowForm.country = ''
      this.editShowForm.total_available_tickets = ''
      this.addArtistForm.name = ''
      this.addArtistForm.country = ''
      this.addArtistForm.genre = ''
      this.deleteArtistForm.id = ''
      this.deleteArtistForm.name = ''
    },
    addShow (parametres) {
      const path = `https://c2-ticketmonster1.herokuapp.com/show`
      axios.post(path, parametres, {auth: {username: this.token}})
        .then(() => {
          console.log('Orders done')
          this.getShows()
        })
        .catch((error) => {
          // eslint-disable-next-line
          console.log(error)
          this.getShows()
        })
    },
    onSubmitUpdate (evt) {
      evt.preventDefault()
      this.$refs.editShowModal.hide()
      const parameters = {
        id: this.editShowForm.id,
        name: this.editShowForm.name,
        place: this.editShowForm.place,
        date: this.editShowForm.date,
        price: this.editShowForm.price,
        city: this.editShowForm.city,
        country: this.editShowForm.country,
        total_available_tickets: this.editShowForm.total_available_tickets
      }
      this.updateShow(parameters)
      this.initForm()
    },
    updateShow (parametres) {
      const path = `https://c2-ticketmonster1.herokuapp.com/show/${this.editShowForm.id}`
      axios.put(path, parametres, {auth: {username: this.token}})
        .then(() => {
          console.log('Orders done')
          this.getShows()
        })
        .catch((error) => {
          // eslint-disable-next-line
          console.log(error)
          this.getShows()
        })
    },
    onResetUpdate (evt) {
      evt.preventDefault()
      this.initForm()
      this.show = false
      this.$nextTick(() => {
        this.show = true
      })
    },
    showWhereModifyArtist (show) {
      this.show_to_modify = show
      this.$refs['addArtistModal'].show()
    },
    onSubmitAddArtistInShow (evt) {
      evt.preventDefault()
      this.$refs.addArtistModal.hide()
      const parameters = {
        name: this.addArtistForm.name,
        country: this.addArtistForm.country,
        genre: this.addArtistForm.genre
      }
      this.addNewArtist(parameters)
    },
    addNewArtist (parametres) {
      const path = `https://c2-ticketmonster1.herokuapp.com/artist`
      axios.post(path, parametres, {auth: {username: this.token}})
        .then(() => {
          console.log('Orders done')
          this.addArtistInShow(parametres)
          this.getShows()
        })
        .catch((error) => {
          // eslint-disable-next-line
          console.log(error)
          this.addArtistInShow(parametres)
          this.getShows()
        })
    },
    addArtistInShow (parametres) {
      /* todo ejercicio 3.7 */
      const showid = this.show_to_modify.id
      const path = `https://c2-ticketmonster1.herokuapp.com/show/${showid}/artist`
      axios.post(path, parametres, {auth: {username: this.token}})
        .then(() => {
          console.log('Orders done')
          this.getShows()
        })
        .catch((error) => {
          // eslint-disable-next-line
          console.log(error)
          this.getShows()
        })
      this.initForm()
    },
    onResetAddArtistInEvent  (evt) {
      evt.preventDefault()
      this.initForm()
      this.show = false
      this.$nextTick(() => {
        this.show = true
      })
    },
    eventWhereModifyArtist (show) {
      this.show_to_modify = show
      this.$refs['deleteArtistModal'].show()
    },
    onSubmitDeleteArtistInShow (evt) {
      evt.preventDefault()
      this.$refs.deleteArtistModal.hide()
      this.deleteArtistInEvent()
    },
    deleteArtistInEvent () {
      const showid = this.show_to_modify.id
      const artistid = this.deleteArtistForm.id
      const path = `https://c2-ticketmonster1.herokuapp.com/show/${showid}/artist/${artistid}`
      axios.delete(path, {auth: {username: this.token}})
        .then(() => {
          console.log('Orders done')
          this.getShows()
        })
        .catch((error) => {
          // eslint-disable-next-line
          console.log(error)
          this.getShows()
        })
      this.initForm()
    },
    deleteNewArtist (parametres) {
    },
    removeShow (show) {
      let showid = show.id
      const path = `https://c2-ticketmonster1.herokuapp.com/show/${showid}`
      axios.delete(path, {auth: {username: this.token}})
        .then(() => {
          console.log('Orders done')
          this.initForm()
          this.getShows()
        })
        .catch((error) => {
          // eslint-disable-next-line
          console.log(error)
          this.getShows()
        })
    },
    Cargarid () {
      const showid = this.editShowForm.id
      const path = `https://c2-ticketmonster1.herokuapp.com/show/${showid}`
      axios.get(path)
        .then((res) => {
          this.editShowForm.name = res.data.show.name
          this.editShowForm.place = res.data.show.place.name
          this.editShowForm.date = res.data.show.date
          this.editShowForm.price = res.data.show.price
          this.editShowForm.city = res.data.show.place.city
          this.editShowForm.country = res.data.show.place.country
          this.editShowForm.total_available_tickets = res.data.show.total_available_tickets
          // return this.available_money
        })
        .catch((error) => {
          // eslint-disable-next-line
          console.error(error)
        })
      // console.log('estoy en el evento de cambiar id: ' + formid)
    }
  },
  created () {
    this.getShows()
    this.logged = this.$route.query.logged
    this.username = this.$route.query.username
    this.token = this.$route.query.token
    this.getAvailabeMoney()
  }
}
</script>
