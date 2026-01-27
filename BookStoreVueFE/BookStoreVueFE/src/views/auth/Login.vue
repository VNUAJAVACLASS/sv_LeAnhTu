<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth.store'
import { useCartStore } from '@/stores/cart.store'

const router = useRouter()
const auth = useAuthStore()
const cartStore = useCartStore()

const username = ref('')
const password = ref('')
const error = ref('')
const isSubmitting = ref(false)

const isValid = computed(() => {
  if (!username.value || !password.value) return false
  if (password.value.length < 5 || password.value.length > 20) return false
  return true
})

const submit = async () => {
  // Chặn ngay từ frontend
  if (!username.value || !password.value) {
    error.value = 'Vui lòng nhập đầy đủ thông tin'
    return
  }

  if (password.value.length < 5 || password.value.length > 20) {
    error.value = 'Mật khẩu phải từ 5 đến 20 ký tự'
    return
  }

  try {
    error.value = ''
    isSubmitting.value = true

    // CHỈ TỚI ĐÂY MỚI GỬI BACKEND
    await auth.login(username.value, password.value)

    cartStore.initCart()

    if (auth.isAdmin) {
      router.push('/admin')
    } else {
      router.push('/')
    }
  } catch (e) {
    error.value = 'Sai tài khoản hoặc mật khẩu'
  } finally {
    isSubmitting.value = false
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
          v-model.trim="username"
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
          placeholder="Mật khẩu (5–20 ký tự)"
          @keyup.enter="submit"
        />
      </div>

      <button
        class="btn-login"
        :disabled="!isValid || isSubmitting"
        @click="submit"
      >
        {{ isSubmitting ? 'Đang đăng nhập...' : 'Đăng nhập' }}
      </button>

      <p class="error" v-if="error">{{ error }}</p>

      <div class="register-link">
        <p>
          Chưa có tài khoản?
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
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
}

.btn-login:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.error {
  color: #e74c3c;
  text-align: center;
  margin-top: 15px;
  background: #ffe6e6;
  padding: 10px;
  border-radius: 4px;
}

.register-link {
  text-align: center;
  margin-top: 20px;
}

.register-link a {
  color: #667eea;
  font-weight: 600;
}
</style>
