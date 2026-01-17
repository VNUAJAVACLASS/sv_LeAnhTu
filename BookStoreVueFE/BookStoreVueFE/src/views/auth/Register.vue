<script setup>
import { ref } from 'vue'
import api from '@/api/axios'
import { useRouter } from 'vue-router'

const router = useRouter()

const username = ref('')
const password = ref('')

const submit = async () => {
  await api.post('/auth/register', {
    username: username.value,
    password: password.value
  })
  router.push('/login')
}
</script>

<template>
  <div class="auth-box">
    <h2>Register</h2>

    <input v-model="username" placeholder="Username" />
    <input v-model="password" type="password" placeholder="Password" />

    <button @click="submit">Register</button>
  </div>
</template>

<style scoped>
.auth-box {
  max-width: 300px;
  margin: 80px auto;
  display: flex;
  flex-direction: column;
}
</style>
