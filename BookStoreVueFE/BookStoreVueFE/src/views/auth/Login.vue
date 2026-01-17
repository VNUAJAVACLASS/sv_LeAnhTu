<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth.store'

const router = useRouter()
const auth = useAuthStore()

const username = ref('')
const password = ref('')
const error = ref('')

const submit = async () => {
  try {
    error.value = ''
    await auth.login(username.value, password.value)
    router.push('/')
  } catch (e) {
    error.value = 'Sai tài khoản hoặc mật khẩu'
  }
}

</script>

<template>
  <div class="auth-box">
    <h2>Login</h2>

    <input v-model="username" placeholder="Username" />
    <input v-model="password" type="password" placeholder="Password" />

    <button @click="submit">Login</button>

    <p class="error" v-if="error">{{ error }}</p>
  </div>
</template>

<style scoped>
.auth-box {
  max-width: 400px;
  margin: 80px auto;
  padding: 30px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.1);
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.auth-box h2 {
  text-align: center;
  margin-bottom: 10px;
  color: #333;
}

.auth-box input {
  padding: 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
}

.auth-box button {
  padding: 12px;
  background: #007bff;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 16px;
  font-weight: 500;
}

.auth-box button:hover {
  background: #0056b3;
}

.error {
  color: red;
  text-align: center;
  font-size: 14px;
}
</style>