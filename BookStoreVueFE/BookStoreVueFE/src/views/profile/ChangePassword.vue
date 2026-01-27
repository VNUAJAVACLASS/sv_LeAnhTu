<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import api from '@/api/axios'
import Swal from 'sweetalert2'

const router = useRouter()

const currentPassword = ref('')
const newPassword = ref('')
const confirmPassword = ref('')
const loading = ref(false)
const error = ref('')

const goBack = () => {
  router.push('/profile')
}

const validateForm = () => {
  if (!currentPassword.value || !newPassword.value || !confirmPassword.value) {
    error.value = 'Vui lòng nhập đầy đủ thông tin'
    return false
  }

  if (newPassword.value.length < 5 || newPassword.value.length > 20) {
    error.value = 'Mật khẩu mới phải từ 5-20 ký tự'
    return false
  }

  if (newPassword.value !== confirmPassword.value) {
    error.value = 'Mật khẩu xác nhận không khớp'
    return false
  }

  if (currentPassword.value === newPassword.value) {
    error.value = 'Mật khẩu mới không được trùng mật khẩu cũ'
    return false
  }

  return true
}

const handleSubmit = async () => {
  error.value = ''
  
  if (!validateForm()) return

  try {
    loading.value = true

    // GỌI API ĐỔI MẬT KHẨU (cần thêm endpoint này trong backend)
    await api.post('/users/change-password', {
      currentPassword: currentPassword.value,
      newPassword: newPassword.value
    })

    await Swal.fire({
      icon: 'success',
      title: 'Đổi mật khẩu thành công!',
      text: 'Vui lòng đăng nhập lại với mật khẩu mới',
      confirmButtonText: 'OK'
    })

    // Logout và redirect về trang login
    router.push('/login')

  } catch (err) {
    console.error('Lỗi đổi mật khẩu:', err)
    
    if (err.response?.data) {
      error.value = err.response.data
    } else {
      error.value = 'Đổi mật khẩu thất bại. Vui lòng kiểm tra mật khẩu hiện tại!'
    }
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="change-password-container">
    <div class="change-password-box">
      <h2>🔐 Đổi mật khẩu</h2>

      <form @submit.prevent="handleSubmit">
        <div class="form-group">
          <label>Mật khẩu hiện tại:</label>
          <input
            v-model="currentPassword"
            type="password"
            placeholder="Nhập mật khẩu hiện tại"
            required
          />
        </div>

        <div class="form-group">
          <label>Mật khẩu mới:</label>
          <input
            v-model="newPassword"
            type="password"
            placeholder="Nhập mật khẩu mới (5-20 ký tự)"
            required
          />
        </div>

        <div class="form-group">
          <label>Xác nhận mật khẩu mới:</label>
          <input
            v-model="confirmPassword"
            type="password"
            placeholder="Nhập lại mật khẩu mới"
            required
          />
        </div>

        <p v-if="error" class="error-message">{{ error }}</p>

        <div class="form-actions">
          <button type="button" @click="goBack" class="btn-cancel">
            <i class="fas fa-arrow-left"></i> Quay lại
          </button>
          <button type="submit" class="btn-submit" :disabled="loading">
            <i class="fas fa-save"></i> 
            {{ loading ? 'Đang xử lý...' : 'Đổi mật khẩu' }}
          </button>
        </div>
      </form>
    </div>
  </div>
</template>

<style scoped>
.change-password-container {
  max-width: 600px;
  margin: 40px auto;
  padding: 20px;
}

.change-password-box {
  background: white;
  padding: 40px;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.1);
}

.change-password-box h2 {
  text-align: center;
  color: #2c3e50;
  margin-bottom: 30px;
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

.form-group input {
  width: 100%;
  padding: 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
}

.form-group input:focus {
  outline: none;
  border-color: #3498db;
  box-shadow: 0 0 0 3px rgba(52, 152, 219, 0.1);
}

.error-message {
  background: #ffe6e6;
  color: #e74c3c;
  padding: 12px;
  border-radius: 4px;
  margin-bottom: 20px;
  text-align: center;
}

.form-actions {
  display: flex;
  gap: 15px;
  margin-top: 30px;
}

.btn-cancel,
.btn-submit {
  flex: 1;
  padding: 12px 24px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-weight: 600;
  font-size: 15px;
  transition: 0.3s;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.btn-cancel {
  background: #95a5a6;
  color: white;
}

.btn-cancel:hover {
  background: #7f8c8d;
}

.btn-submit {
  background: #e67e22;
  color: white;
}

.btn-submit:hover:not(:disabled) {
  background: #d35400;
}

.btn-submit:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
</style>