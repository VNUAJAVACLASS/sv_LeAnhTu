<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import api from '@/api/axios'
import Swal from 'sweetalert2'

const router = useRouter()

const form = ref({
  username: '',
  email: '',
  soDienThoai: '',
  password: '',
  confirmPassword: ''
})

const errors = ref({})
const isSubmitting = ref(false)

// Validation real-time
const validateUsername = () => {
  if (!form.value.username) {
    errors.value.username = 'Tên đăng nhập không được để trống'
    return false
  }
  if (form.value.username.length < 3 || form.value.username.length > 20) {
    errors.value.username = 'Tên đăng nhập phải từ 3-20 ký tự'
    return false
  }
  if (!/^[a-zA-Z0-9_]+$/.test(form.value.username)) {
    errors.value.username = 'Tên đăng nhập chỉ chứa chữ, số và dấu gạch dưới'
    return false
  }
  errors.value.username = ''
  return true
}

const validateEmail = () => {
  if (!form.value.email) {
    errors.value.email = 'Email không được để trống'
    return false
  }
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  if (!emailRegex.test(form.value.email)) {
    errors.value.email = 'Email không hợp lệ'
    return false
  }
  errors.value.email = ''
  return true
}

const validatePhone = () => {
  if (!form.value.soDienThoai) {
    errors.value.soDienThoai = 'Số điện thoại không được để trống'
    return false
  }
  if (!/^[0-9]{10,11}$/.test(form.value.soDienThoai)) {
    errors.value.soDienThoai = 'Số điện thoại phải có 10-11 chữ số'
    return false
  }
  errors.value.soDienThoai = ''
  return true
}

const validatePassword = () => {
  if (!form.value.password) {
    errors.value.password = 'Mật khẩu không được để trống'
    return false
  }
  if (form.value.password.length < 5 || form.value.password.length > 20) {
    errors.value.password = 'Mật khẩu phải từ 5-20 ký tự'
    return false
  }
  errors.value.password = ''
  return true
}

const validateConfirmPassword = () => {
  if (!form.value.confirmPassword) {
    errors.value.confirmPassword = 'Vui lòng xác nhận mật khẩu'
    return false
  }
  if (form.value.password !== form.value.confirmPassword) {
    errors.value.confirmPassword = 'Mật khẩu xác nhận không khớp'
    return false
  }
  errors.value.confirmPassword = ''
  return true
}

// Computed để check form hợp lệ
const isFormValid = computed(() => {
  return (
    form.value.username &&
    form.value.email &&
    form.value.soDienThoai &&
    form.value.password &&
    form.value.confirmPassword &&
    !errors.value.username &&
    !errors.value.email &&
    !errors.value.soDienThoai &&
    !errors.value.password &&
    !errors.value.confirmPassword
  )
})

const handleSubmit = async () => {
  // Validate tất cả trước khi submit
  const isUsernameValid = validateUsername()
  const isEmailValid = validateEmail()
  const isPhoneValid = validatePhone()
  const isPasswordValid = validatePassword()
  const isConfirmPasswordValid = validateConfirmPassword()

  if (!isUsernameValid || !isEmailValid || !isPhoneValid || !isPasswordValid || !isConfirmPasswordValid) {
    return
  }

  try {
    isSubmitting.value = true

    await api.post('/auth/register', {
      username: form.value.username,
      password: form.value.password,
      gmail: form.value.email,
      soDienThoai: form.value.soDienThoai
    })

    await Swal.fire({
      icon: 'success',
      title: 'Đăng ký thành công!',
      html: `
        <p>Chào mừng <strong>${form.value.username}</strong>!</p>
        <p>Hãy đăng nhập để vào hệ thống</p>
      `,
      confirmButtonText: 'Đăng nhập ngay',
      confirmButtonColor: '#3498db'
    })

    router.push('/login')

  } catch (err) {
    console.error('Lỗi đăng ký:', err)
    
    let errorMessage = 'Đăng ký thất bại. Vui lòng thử lại!'
    
    if (err.response?.data) {
      errorMessage = err.response.data
    } else if (err.response?.status === 409) {
      errorMessage = 'Tên đăng nhập đã tồn tại'
    }

    Swal.fire({
      icon: 'error',
      title: 'Đăng ký thất bại!',
      text: errorMessage,
      confirmButtonText: 'OK'
    })
  } finally {
    isSubmitting.value = false
  }
}

// Format số điện thoại khi nhập (chỉ cho phép số)
const formatPhone = (e) => {
  const value = e.target.value.replace(/\D/g, '')
  form.value.soDienThoai = value.substring(0, 11)
  validatePhone()
}
</script>

<template>
  <div class="register-page">
    <div class="auth-box">
      <h2>📝 Đăng ký tài khoản</h2>
      <p class="subtitle">Tạo tài khoản để trải nghiệm BookStore</p>

      <form @submit.prevent="handleSubmit">
        <!-- USERNAME -->
        <div class="form-group">
          <label>
            <i class="fas fa-user"></i> Tên đăng nhập
            <span class="required">*</span>
          </label>
          <input
            v-model="form.username"
            type="text"
            placeholder="Nhập tên đăng nhập (3-20 ký tự)"
            @blur="validateUsername"
            @input="validateUsername"
            maxlength="20"
            :class="{ 'error-input': errors.username }"
          />
          <small v-if="errors.username" class="error-text">
            <i class="fas fa-exclamation-circle"></i> {{ errors.username }}
          </small>
          <small v-else class="hint-text">
            Chỉ chứa chữ, số và dấu gạch dưới (_)
          </small>
        </div>

        <!-- EMAIL -->
        <div class="form-group">
          <label>
            <i class="fas fa-envelope"></i> Email
            <span class="required">*</span>
          </label>
          <input
            v-model="form.email"
            type="email"
            placeholder="example@gmail.com"
            @blur="validateEmail"
            @input="validateEmail"
            :class="{ 'error-input': errors.email }"
          />
          <small v-if="errors.email" class="error-text">
            <i class="fas fa-exclamation-circle"></i> {{ errors.email }}
          </small>
          <small v-else class="hint-text">
            Email hợp lệ để nhận thông báo
          </small>
        </div>

        <!-- SỐ ĐIỆN THOẠI -->
        <div class="form-group">
          <label>
            <i class="fas fa-phone"></i> Số điện thoại
            <span class="required">*</span>
          </label>
          <input
            v-model="form.soDienThoai"
            type="tel"
            placeholder="Nhập 10-11 chữ số"
            @input="formatPhone"
            @blur="validatePhone"
            maxlength="11"
            :class="{ 'error-input': errors.soDienThoai }"
          />
          <small v-if="errors.soDienThoai" class="error-text">
            <i class="fas fa-exclamation-circle"></i> {{ errors.soDienThoai }}
          </small>
          <small v-else class="hint-text">
            Số điện thoại: {{ form.soDienThoai.length }}/11 số
          </small>
        </div>

        <!-- MẬT KHẨU -->
        <div class="form-group">
          <label>
            <i class="fas fa-lock"></i> Mật khẩu
            <span class="required">*</span>
          </label>
          <input
            v-model="form.password"
            type="password"
            placeholder="Nhập mật khẩu (5-20 ký tự)"
            @blur="validatePassword"
            @input="validatePassword"
            maxlength="20"
            :class="{ 'error-input': errors.password }"
          />
          <small v-if="errors.password" class="error-text">
            <i class="fas fa-exclamation-circle"></i> {{ errors.password }}
          </small>
          <small v-else class="hint-text">
            Độ dài: {{ form.password.length }}/20 ký tự
          </small>
        </div>

        <!-- XÁC NHẬN MẬT KHẨU -->
        <div class="form-group">
          <label>
            <i class="fas fa-lock"></i> Xác nhận mật khẩu
            <span class="required">*</span>
          </label>
          <input
            v-model="form.confirmPassword"
            type="password"
            placeholder="Nhập lại mật khẩu"
            @blur="validateConfirmPassword"
            @input="validateConfirmPassword"
            maxlength="20"
            :class="{ 'error-input': errors.confirmPassword }"
          />
          <small v-if="errors.confirmPassword" class="error-text">
            <i class="fas fa-exclamation-circle"></i> {{ errors.confirmPassword }}
          </small>
          <small v-else-if="form.confirmPassword && !errors.confirmPassword" class="success-text">
            <i class="fas fa-check-circle"></i> Mật khẩu khớp
          </small>
        </div>

        <!-- SUBMIT BUTTON -->
        <button 
          type="submit" 
          class="btn-register"
          :disabled="!isFormValid || isSubmitting"
          :class="{ 'btn-disabled': !isFormValid }"
        >
          <i class="fas fa-user-plus"></i>
          {{ isSubmitting ? 'Đang đăng ký...' : 'Đăng ký' }}
        </button>

        <!-- LOGIN LINK -->
        <div class="login-link">
          <p>
            Đã có tài khoản?
            <router-link to="/login">Đăng nhập ngay</router-link>
          </p>
        </div>
      </form>
    </div>
  </div>
</template>

<style scoped>
.register-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 40px 20px;
}

.auth-box {
  max-width: 500px;
  width: 100%;
  padding: 40px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 10px 30px rgba(0,0,0,0.2);
}

.auth-box h2 {
  text-align: center;
  color: #2c3e50;
  margin-bottom: 10px;
}

.subtitle {
  text-align: center;
  color: #7f8c8d;
  margin-bottom: 30px;
  font-size: 14px;
}

.form-group {
  margin-bottom: 25px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  font-weight: 600;
  color: #2c3e50;
  font-size: 14px;
}

.form-group label i {
  margin-right: 5px;
  color: #667eea;
}

.required {
  color: #e74c3c;
  font-weight: bold;
}

.auth-box input {
  width: 100%;
  padding: 12px 15px;
  border: 2px solid #ddd;
  border-radius: 6px;
  font-size: 14px;
  transition: all 0.3s;
}

.auth-box input:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.error-input {
  border-color: #e74c3c !important;
  background: #fff5f5;
}

.error-input:focus {
  border-color: #e74c3c !important;
  box-shadow: 0 0 0 3px rgba(231, 76, 60, 0.1) !important;
}

.hint-text {
  display: block;
  margin-top: 5px;
  color: #7f8c8d;
  font-size: 12px;
  font-style: italic;
}

.error-text {
  display: block;
  margin-top: 5px;
  color: #e74c3c;
  font-size: 12px;
  font-weight: 600;
}

.error-text i {
  margin-right: 3px;
}

.success-text {
  display: block;
  margin-top: 5px;
  color: #27ae60;
  font-size: 12px;
  font-weight: 600;
}

.success-text i {
  margin-right: 3px;
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
  transition: all 0.3s;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  margin-top: 10px;
}

.btn-register:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 5px 15px rgba(102, 126, 234, 0.3);
}

.btn-register:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
}

.btn-disabled {
  background: #95a5a6 !important;
}

.login-link {
  text-align: center;
  margin-top: 25px;
  padding-top: 20px;
  border-top: 1px solid #ecf0f1;
}

.login-link p {
  color: #7f8c8d;
  font-size: 14px;
}

.login-link a {
  color: #667eea;
  text-decoration: none;
  font-weight: 600;
  margin-left: 5px;
}

.login-link a:hover {
  text-decoration: underline;
}

/* Animation for errors */
.error-text,
.success-text {
  animation: slideIn 0.3s ease-out;
}

@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateY(-5px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* Responsive */
@media (max-width: 576px) {
  .auth-box {
    padding: 30px 20px;
  }

  .auth-box h2 {
    font-size: 24px;
  }

  .form-group {
    margin-bottom: 20px;
  }
}
</style>