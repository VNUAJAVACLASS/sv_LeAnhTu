<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import api from '@/api/axios'
import Swal from 'sweetalert2'

const router = useRouter()

const username = ref('')
const password = ref('')
const confirmPassword = ref('')
const error = ref('')
const isSubmitting = ref(false)

const submit = async () => {
  // Validate thiếu trường
  if (!username.value || !password.value || !confirmPassword.value) {
    error.value = 'Vui lòng nhập đầy đủ thông tin'
    return
  }

  // Validate độ dài mật khẩu
  if (password.value.length < 8 || password.value.length > 20) {
    error.value = 'Mật khẩu phải từ 8 đến 20 ký tự'
    return
  }

  // Validate xác nhận mật khẩu
  if (password.value !== confirmPassword.value) {
    error.value = 'Mật khẩu xác nhận không khớp'
    return
  }

  try {
    isSubmitting.value = true
    error.value = ''

    await api.post('/auth/register', {
      username: username.value,
      password: password.value
    })

    await Swal.fire({
      icon: 'success',
      title: 'Đăng ký thành công!',
      text: 'Hãy đăng nhập để vào hệ thống!',
      confirmButtonText: 'OK'
    })

    router.push('/login')
  } catch (e) {
    if (e.response?.data) {
      error.value = e.response.data
    } else {
      error.value = 'Đăng ký thất bại. Vui lòng thử lại!'
    }
  } finally {
    isSubmitting.value = false
  }
}

</script>

<template>
  <div class="register-page">
    <div class="auth-box">
      <h2>📝 Đăng ký tài khoản</h2>

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
          placeholder="Nhập mật khẩu (tối thiểu 8 ký tự)"
          @keyup.enter="submit"
        />
      </div>

      <div class="form-group">
        <label>Xác nhận mật khẩu:</label>
        <input
          v-model="confirmPassword"
          type="password"
          placeholder="Nhập lại mật khẩu"
          @keyup.enter="submit"
        />
      </div>

      <button @click="submit" class="btn-register" :disabled="isSubmitting">
        {{ isSubmitting ? 'Đang đăng ký...' : 'Đăng ký' }}
      </button>

      <p class="error" v-if="error">{{ error }}</p>

      <div class="login-link">
        <p>Đã có tài khoản?
          <router-link to="/login">Đăng nhập ngay</router-link>
        </p>
      </div>
    </div>
  </div>
</template>

<style scoped>
.register-page {
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

.btn-register {
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

.btn-register:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 5px 15px rgba(102, 126, 234, 0.3);
}

.btn-register:disabled {
  opacity: 0.6;
  cursor: not-allowed;
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

.login-link {
  text-align: center;
  margin-top: 20px;
  color: #7f8c8d;
}

.login-link a {
  color: #667eea;
  text-decoration: none;
  font-weight: 600;
}

.login-link a:hover {
  text-decoration: underline;
}
</style>