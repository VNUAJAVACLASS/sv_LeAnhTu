<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import api from '@/api/axios'
import Swal from 'sweetalert2'

const router = useRouter()

const form = ref({
  gmail: '',
  soDienThoai: '',
  diaChi: ''
})

const loading = ref(false)
const loadingData = ref(true)
const error = ref('')

onMounted(async () => {
  await loadUserInfo()
})

const loadUserInfo = async () => {
  try {
    const res = await api.get('/users/me')
    form.value = {
      gmail: res.data.gmail || '',
      soDienThoai: res.data.soDienThoai || '',
      diaChi: res.data.diaChi || ''
    }
  } catch (error) {
    console.error('Lỗi tải thông tin:', error)
  } finally {
    loadingData.value = false
  }
}

const goBack = () => {
  router.push('/profile')
}

const validateForm = () => {
  // Validate email
  if (form.value.gmail && !isValidEmail(form.value.gmail)) {
    error.value = 'Email không hợp lệ'
    return false
  }

  // Validate số điện thoại
  if (form.value.soDienThoai && !isValidPhone(form.value.soDienThoai)) {
    error.value = 'Số điện thoại không hợp lệ (10-11 số)'
    return false
  }

  return true
}

const isValidEmail = (email) => {
  const regex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  return regex.test(email)
}

const isValidPhone = (phone) => {
  const regex = /^[0-9]{10,11}$/
  return regex.test(phone)
}

const handleSubmit = async () => {
  error.value = ''
  
  if (!validateForm()) return

  try {
    loading.value = true

    // GỌI API CẬP NHẬT THÔNG TIN (cần thêm endpoint này trong backend)
    await api.patch('/users/update-info', form.value)

    await Swal.fire({
      icon: 'success',
      title: 'Cập nhật thành công!',
      text: 'Thông tin của bạn đã được cập nhật',
      confirmButtonText: 'OK'
    })

    router.push('/profile')

  } catch (err) {
    console.error('Lỗi cập nhật:', err)
    error.value = err.response?.data || 'Cập nhật thất bại. Vui lòng thử lại!'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="update-info-container">
    <div class="update-info-box">
      <h2>✏️ Cập nhật thông tin</h2>

      <div v-if="loadingData" class="loading">
        <p>Đang tải thông tin...</p>
      </div>

      <form v-else @submit.prevent="handleSubmit">
        <div class="form-group">
          <label>Email:</label>
          <input
            v-model="form.gmail"
            type="email"
            placeholder="Nhập email của bạn"
          />
          <small>Ví dụ: example@gmail.com</small>
        </div>

        <div class="form-group">
          <label>Số điện thoại:</label>
          <input
            v-model="form.soDienThoai"
            type="text"
            placeholder="Nhập số điện thoại (10-11 số)"
            maxlength="11"
          />
          <small>Ví dụ: 0123456789</small>
        </div>

        <div class="form-group">
          <label>Địa chỉ:</label>
          <textarea
            v-model="form.diaChi"
            rows="4"
            placeholder="Nhập địa chỉ của bạn"
          ></textarea>
          <small>Ví dụ: 123 Đường ABC, Phường XYZ, Quận 1, TP.HCM</small>
        </div>

        <p v-if="error" class="error-message">{{ error }}</p>

        <div class="form-actions">
          <button type="button" @click="goBack" class="btn-cancel">
            <i class="fas fa-arrow-left"></i> Quay lại
          </button>
          <button type="submit" class="btn-submit" :disabled="loading">
            <i class="fas fa-save"></i> 
            {{ loading ? 'Đang lưu...' : 'Lưu thay đổi' }}
          </button>
        </div>
      </form>
    </div>
  </div>
</template>

<style scoped>
.update-info-container {
  max-width: 700px;
  margin: 40px auto;
  padding: 20px;
}

.update-info-box {
  background: white;
  padding: 40px;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.1);
}

.update-info-box h2 {
  text-align: center;
  color: #2c3e50;
  margin-bottom: 30px;
}

.loading {
  text-align: center;
  padding: 50px;
  color: #7f8c8d;
}

.form-group {
  margin-bottom: 25px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  font-weight: 600;
  color: #2c3e50;
}

.form-group input,
.form-group textarea {
  width: 100%;
  padding: 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
  font-family: inherit;
}

.form-group input:focus,
.form-group textarea:focus {
  outline: none;
  border-color: #3498db;
  box-shadow: 0 0 0 3px rgba(52, 152, 219, 0.1);
}

.form-group small {
  display: block;
  margin-top: 5px;
  color: #7f8c8d;
  font-size: 12px;
  font-style: italic;
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
  background: #3498db;
  color: white;
}

.btn-submit:hover:not(:disabled) {
  background: #2980b9;
}

.btn-submit:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
</style>