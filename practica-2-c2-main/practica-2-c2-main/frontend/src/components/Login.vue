<template>
  <div id="app">
  <div v-if=showsignin class="container">
  <div class="card">
    <h1> Sing In </h1>
    <div class="form-label-group">
      <label for="inputUsername">Username</label>
      <input type="username" id="inputUsername" class="form-control"
      placeholder="Username" required autofocus v-model="username">
    </div>
    <div class="form-label-group">
      <br>
      <label for="inputPassword">Password</label>
      <input type="password" id="inputPassword" class="form-control"
      placeholder="Password" required v-model="password">
    </div>
    <p></p>
    <button class="btn btn-primary btn-lg "
            @click=checkLogin() > Sign In</button>
    <p></p>
    <button class="btn btn-success btn-lg"
            @click=createAccount() > Create Account</button>
    <p></p>
    <button class="btn btn-secondary btn-lg"
            @click=backtoEvents() > Back to Events</button>
    <p></p>
  </div>
  </div>

    <div v-else class="container">
    <b-form @submit="onSubmit" @reset="onReset">
      <b-form-group
        id="input-group-1"
        label="Username:"
        label-for="input-1">
        <b-form-input
          id="input-1"
          v-model="addUserForm.username"
          placeholder="Enter username"
          required
        ></b-form-input>
      </b-form-group>

      <b-form-group id="input-group-2" label="Password:" label-for="input-2">
        <b-form-input
          id="input-2"
          v-model="addUserForm.password"
          placeholder="Enter password"
          required
        ></b-form-input>
      </b-form-group>

      <b-button type="submit" variant="primary">Submit</b-button>
      <b-button type="reset" variant="danger">Back</b-button>
    </b-form>
  </div>

  </div>
</template>

<script>
import axios from 'axios'
export default {
  data () {
    return {
      username: '',
      password: '',
      name: 'Login',
      addUserForm: {
        username: '',
        password: ''
      },
      showsignin: true
    }
  },
  methods: {
    initForm () {
      this.addUserForm.username = ''
      this.addUserForm.password = ''
    },
    createAccount () {
      this.showsignin = false
    },
    checkLogin () {
      const parameters = {
        username: this.username,
        password: this.password
      }
      const path = 'https://c2-ticketmonster1.herokuapp.com/login'
      axios.post(path, parameters)
        .then((res) => {
          this.logged = true
          this.token = res.data.token
          this.find_match = true
          this.getAccount()
          alert("S'ha iniciat sessiò")
          this.$router.replace({ path: '/', query: { username: this.username, logged: this.logged, token: this.token } })
        })
        .catch((error) => {
          // eslint-disable-next-line
          console.error(error)
          this.username = ''
          alert('Username or Password incorrect')
        })
    },
    getAccount () {
      const path = 'https://c2-ticketmonster1.herokuapp.com/account/' + this.username
      axios.get(path)
        .then((res) => {
          this.is_admin = res.data.account.is_admin
        })
        .catch((error) => {
          // eslint-disable-next-line
          console.error(error)
          this.username = ''
          alert('Username or Password incorrect')
          // TODO-repas
        })
    },
    onSubmit (event) {
      event.preventDefault()
      // const path = 'http://localhost:5000/account'
      const path = 'https://c2-ticketmonster1.herokuapp.com/account'
      const parameters = {
        username: this.addUserForm.username,
        password: this.addUserForm.password
      }
      axios.post(path, parameters)
        .then((res) => {
          this.logged = true
          this.token = res.data.token
          this.find_match = true
          this.getAccount()
          this.username = this.addUserForm.username
          alert('Compte creat satisfactoriament')
          // TODO-repas
        })
        .catch((error) => {
          // eslint-disable-next-line
          console.error(error)
          this.username = ''
          alert("Error al crear l'usuari")
        })
    },
    onReset (event) {
      event.preventDefault()
      // Reset our form values
      this.initForm()
      // Trick to reset/clear native browser form validation state
      this.showsignin = false
      this.$nextTick(() => {
        this.showsignin = true
      })
    },
    backtoEvents () {
      this.logged = false
      this.$router.replace({ path: '/', query: { logged: this.logged } })
      // this.$router.replace({ path: '/', query: { username: this.username, logged: this.logged } })
    }
  },
  created () {
    this.logged = this.$route.query.logged
    this.username = this.$route.query.username
  }
}
</script>

<style scoped>

</style>
