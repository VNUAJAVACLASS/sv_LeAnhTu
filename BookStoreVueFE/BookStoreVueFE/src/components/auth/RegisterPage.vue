<template>
  <div class="row justify-content-md-center mt-5">
    <div class="col-4">
      <div class="card">
        <div class="card-body">
          <h5 class="card-title mb-4">Register</h5>
          <form>
            <p v-if="Object.keys(validationErrors).length != 0" class='text-center'>
              <small class='text-danger'>Register fail!</small>
            </p>
            <div class="mb-3">
              <label htmlFor="name" class="form-label">Name</label>
              <input
                type="text"
                class="form-control"
                id="name"
                name="name"
                v-model="name"
              />
            </div>
            <div class="mb-3">
              <label htmlFor="email" class="form-label">Username</label>
              <input
                type="email"
                class="form-control"
                id="email"
                name="email"
                v-model="email"
              />
            </div>
            <div class="mb-3">
              <label htmlFor="password" class="form-label">Password</label>
              <input
                type="password"
                class="form-control"
                id="password"
                name="password"
                v-model="password"
              />
            </div>
            <div class="mb-3">
              <label htmlFor="confirm_password" class="form-label">Confirm Password</label>
              <input
                type="password"
                class="form-control"
                id="confirm_password"
                name="confirm_password"
                v-model="confirmPassword"
              />
            </div>
            <div class="d-grid gap-2">
              <button
                :disabled="isSubmitting"
                @click="registerAction()"
                type="button"
                class="btn btn-primary btn-block">
                Register Now
              </button>
              <p class="text-center">
                Have already an account 
                <router-link to="/">Login here</router-link>
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
import Swal from 'sweetalert2';

export default {
  name: 'RegisterPage',
  data() {
    return {
      name: '',
      email: '',
      password: '',
      confirmPassword: '',
      validationErrors: {},
      isSubmitting: false,
    };
  },
  created() {
    if(localStorage.getItem('token') != "" && localStorage.getItem('token') != null){
      this.$router.push('/dashboard')
    }
  },
  watch: {
    confirmPassword(newVal) {
      if (newVal && this.password && newVal !== this.password) {
        Swal.fire({
          icon: 'error',
          title: 'Mật khẩu không khớp!',
          text: 'Vui lòng nhập lại mật khẩu cho trùng khớp.',
          confirmButtonText: 'OK'
        });
      }
    }
  },
  methods: {
    registerAction() {
      if (this.password !== this.confirmPassword) {
        return;
      }
      
      this.isSubmitting = true
      let payload = {
        name: this.name,
        username: this.email,
        password: this.password,
        password_confirmation: this.confirmPassword
      }
      axios.post('/api/auth/register', payload)
        .then(response => {
          Swal.fire({
            icon: 'success',
            title: 'Đăng ký thành công!',
            text: 'Hãy đăng nhập để vào hệ thống!',
            confirmButtonText: 'OK'
          }).then(() => {
            this.$router.push('/')
          })
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