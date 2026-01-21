<template>
  <div class="row justify-content-md-center mt-5">
    <div class="col-4">
      <div class="card">
        <div class="card-body">
          <h5 class="card-title mb-4">Sign In</h5>
          <form>
            <p v-if="Object.keys(validationErrors).length != 0" class='text-center'>
              <small class='text-danger'>Incorrect Username or Password</small>
            </p>
            <div class="mb-3">
              <label htmlFor="email" class="form-label">Username</label>
              <input
                v-model="email"
                type="text"
                class="form-control"
                id="email"
                name="email"
              />
            </div>
            <div class="mb-3">
              <label htmlFor="password" class="form-label">Password</label>
              <input
                v-model="password"
                type="password"
                class="form-control"
                id="password"
                name="password"
              />
            </div>
            <div class="d-grid gap-2">
              <button
                :disabled="isSubmitting"
                @click="loginAction()"
                type="button"
                class="btn btn-primary btn-block">
                Login
              </button>
              <p class="text-center">
                Don't have account? 
                <router-link to="/register">Register here</router-link>
              </p>
            </div>
          </form>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios';

export default {
  name: 'LoginPage',
  data() {
    return {
      email: '',
      password: '',
      validationErrors: {},
      isSubmitting: false,
    };
  },
  created() {
    if(localStorage.getItem('token') != "" && localStorage.getItem('token') != null){
      this.$router.push('/dashboard')
    }
  },
  methods: {
    loginAction() {
      this.isSubmitting = true
      let payload = {
        username: this.email,
        password: this.password,
      }
      axios.post('/api/auth/login', payload)
        .then(response => {
          const { accessToken, username, roles } = response.data
          localStorage.setItem('token', accessToken)
          localStorage.setItem('username', username)
          localStorage.setItem('roles', JSON.stringify(roles))
          this.$router.push('/dashboard')
          return response
        })
        .catch(error => {
          this.isSubmitting = false
          if (error.response) {
            this.validationErrors = error;
          } else {
            this.validationErrors = { message: 'Network or server error' };
          }
          return error
        });
    }
  },
};
</script>