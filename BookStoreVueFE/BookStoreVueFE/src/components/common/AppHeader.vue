<template>
  <header class="header">
    <!-- Logo / Home -->
    <router-link to="/" class="logo">
      <i class="fas fa-home"></i> BookStore
    </router-link>

    <nav class="nav-menu">
      <!-- Nếu chưa đăng nhập -->
      <div v-if="!isLoggedIn" class="auth-links">
        <router-link to="/login" class="btn-login">Đăng nhập</router-link>
        <router-link to="/register" class="btn-register">Đăng ký</router-link>
      </div>

      <!-- Nếu đã đăng nhập -->
      <div v-else class="user-menu">
        <!-- Icon giỏ hàng (chỉ User) -->
        <router-link v-if="!isAdmin" to="/cart" class="cart-icon">
          🛒
          <span v-if="cartStore.items.length > 0" class="badge">
            {{ cartStore.items.length }}
          </span>
        </router-link>

        <!-- Nút Quản lý Website (chỉ Admin) -->
        <router-link v-if="isAdmin" to="/admin" class="btn-admin">
          <i class="fas fa-cog"></i> Quản lý Website
        </router-link>

        <!-- Nút Trang cá nhân (User) -->
        <router-link v-if="!isAdmin" to="/profile" class="btn-profile">
          <i class="fas fa-user"></i> Trang cá nhân
        </router-link>

        <!-- Thông tin user -->
        <span class="username">
          Xin chào, <strong>{{ username }}</strong>
        </span>

        <!-- Nút Logout -->
        <a href="#" @click.prevent="logout" class="btn-logout">
          <i class="fas fa-sign-out-alt"></i> Đăng xuất
        </a>
      </div>
    </nav>
  </header>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth.store'
import { useCartStore } from '@/stores/cart.store'

const router = useRouter()
const authStore = useAuthStore()
const cartStore = useCartStore()

const isLoggedIn = computed(() => authStore.isLoggedIn)
const isAdmin = computed(() => authStore.isAdmin)
const username = computed(() => authStore.user?.username || '')

const logout = () => {
  authStore.logout()
  cartStore.clearCart() //Xóa giỏ hàng khi logout
  router.push('/')
}
</script>

<style scoped>
.header {
  position: sticky;
  top: 0;
  z-index: 100;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 30px;
  background: #2c3e50;
  color: #fff;
  box-shadow: 0 2px 5px rgba(0,0,0,0.1);
}

.logo {
  color: #fff;
  text-decoration: none;
  font-weight: bold;
  font-size: 20px;
}

.logo:hover {
  color: #3498db;
}

.nav-menu {
  display: flex;
  align-items: center;
  gap: 15px;
}

.auth-links {
  display: flex;
  gap: 10px;
}

.btn-login, .btn-register {
  padding: 8px 16px;
  border-radius: 4px;
  transition: 0.3s;
  text-decoration: none;
}

.btn-login {
  background: #3498db;
  color: white;
}

.btn-login:hover {
  background: #2980b9;
}

.btn-register {
  background: #27ae60;
  color: white;
}

.btn-register:hover {
  background: #229954;
}

.user-menu {
  display: flex;
  align-items: center;
  gap: 20px;
}

.cart-icon {
  position: relative;
  font-size: 24px;
  cursor: pointer;
  text-decoration: none;
  transition: 0.3s;
}

.cart-icon:hover {
  transform: scale(1.1);
}

.badge {
  position: absolute;
  top: -8px;
  right: -10px;
  background: red;
  color: white;
  border-radius: 50%;
  padding: 2px 7px;
  font-size: 12px;
  font-weight: bold;
}

.btn-admin, .btn-profile {
  background: #e74c3c;
  padding: 8px 16px;
  border-radius: 4px;
  text-decoration: none;
  color: white;
  transition: 0.3s;
}

.btn-admin:hover {
  background: #c0392b;
}

.btn-profile {
  background: #9b59b6;
}

.btn-profile:hover {
  background: #8e44ad;
}

.username {
  font-size: 14px;
  color: white;
}

.btn-logout {
  background: #95a5a6;
  padding: 8px 16px;
  border-radius: 4px;
  cursor: pointer;
  border: none;
  color: white;
  font-size: 14px;
  transition: 0.3s;
  text-decoration: none;
}

.btn-logout:hover {
  background: #7f8c8d;
}
</style>