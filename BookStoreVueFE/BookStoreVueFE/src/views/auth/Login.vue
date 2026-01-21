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
    
    // Kiểm tra role sau khi login
    if (auth.isAdmin) {
      router.push('/admin') // Admin vào trang quản lý
    } else {
      router.push('/') // User vào trang chủ
    }
  } catch (e) {
    error.value = 'Sai tài khoản hoặc mật khẩu'
  }
}
</script>

<template>
  <div class="login-page">
    <div class="auth-box">
      <h2>🔐 Đăng nhập</h2>

      <div class="form-group">
        <label>Tên đăng nhập:</label>
        <input 
          v-model="username" 
          type="text"
          placeholder="Nhập username" 
          @keyup.enter="submit"
        />
      </div>

      <div class="form-group">
        <label>Mật khẩu:</label>
        <input 
          v-model="password" 
          type="password" 
          placeholder="Nhập mật khẩu"
          @keyup.enter="submit"
        />
      </div>

      <button @click="submit" class="btn-login">Đăng nhập</button>

      <p class="error" v-if="error">{{ error }}</p>

      <div class="register-link">
        <p>Chưa có tài khoản? 
          <router-link to="/register">Đăng ký ngay</router-link>
        </p>
      </div>
    </div>
  </div>
</template>

<style scoped>
.login-page {
  min-height: 80vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.auth-box {
  max-width: 400px;
  width: 100%;
  padding: 40px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 10px 30px rgba(0,0,0,0.2);
}

.auth-box h2 {
  text-align: center;
  margin-bottom: 30px;
  color: #2c3e50;
}

.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  font-weight: 600;
  color: #2c3e50;
}

.auth-box input {
  width: 100%;
  padding: 12px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 14px;
  transition: 0.3s;
}

.auth-box input:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.btn-login {
  width: 100%;
  padding: 14px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 16px;
  font-weight: 600;
  transition: 0.3s;
}

.btn-login:hover {
  transform: translateY(-2px);
  box-shadow: 0 5px 15px rgba(102, 126, 234, 0.3);
}

.error {
  color: #e74c3c;
  text-align: center;
  font-size: 14px;
  margin-top: 15px;
  background: #ffe6e6;
  padding: 10px;
  border-radius: 4px;
}

.register-link {
  text-align: center;
  margin-top: 20px;
  color: #7f8c8d;
}

.register-link a {
  color: #667eea;
  text-decoration: none;
  font-weight: 600;
}

.register-link a:hover {
  text-decoration: underline;
}
</style>