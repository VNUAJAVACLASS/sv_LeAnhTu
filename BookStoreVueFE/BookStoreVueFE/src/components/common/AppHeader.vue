<script setup>
import { useAuthStore } from '@/stores/auth.store'
import { useRouter } from 'vue-router'
import Navbar from './Navbar.vue'

const auth = useAuthStore()
const router = useRouter()

const logout = () => {
  auth.logout()
  router.push('/login')
}
</script>

<template>
  <header class="header">
    <h2 @click="$router.push('/')">📚 BookStore</h2>

    <nav>
      <button v-if="!auth.isLoggedIn" @click="$router.push('/login')">Login</button>
      <button v-if="!auth.isLoggedIn" @click="$router.push('/register')">Register</button>

      <span v-if="auth.isLoggedIn">
        👤 {{ auth.user.username }}
      </span>
      
      <button
      v-if="auth.isLoggedIn"
      @click="$router.push('/profile')"
    >
      Trang cá nhân
    </button>

      <button
        v-if="auth.isAdmin"
        @click="$router.push('/admin')"
      >
        Admin
      </button>

      <button v-if="auth.isLoggedIn" @click="logout">Logout</button>

      <!-- THÊM NAVBAR Ở ĐÂY -->
      <Navbar />
    </nav>
  </header>
</template>
