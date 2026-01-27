import { createRouter, createWebHistory } from 'vue-router'

// Views
import Home from '@/views/home/Home.vue'
import Register from '@/views/auth/Register.vue'
import Login from '@/views/auth/Login.vue'
import Dashboard from '@/views/admin/Dashboard.vue'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: Home
  },
  {
    path: '/register',
    name: 'Register',
    component: Register
  },
  {
    path: '/login',
    name: 'Login',
    component: Login
  },
  {
    path: '/books/:id',
    name: 'BookDetail',
    component: () => import('@/views/home/BookDetail.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/cart',
    name: 'Cart',
    component: () => import('@/views/cart/Cart.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/profile',
    name: 'Profile',
    component: () => import('@/views/profile/Profile.vue'),
    meta: { requiresAuth: true }
  },
  // ===== PROFILE SUB-ROUTES =====
  {
    path: '/profile/change-password',
    name: 'ChangePassword',
    component: () => import('@/views/profile/ChangePassword.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/profile/update-info',
    name: 'UpdateInfo',
    component: () => import('@/views/profile/UpdateInfo.vue'),
    meta: { requiresAuth: true }
  },
  {
    path: '/profile/cancelled-orders',
    name: 'CancelledOrders',
    component: () => import('@/views/profile/CancelledOrders.vue'),
    meta: { requiresAuth: true }
  },
  // ===== ADMIN ROUTES =====
  {
    path: '/admin',
    name: 'AdminDashboard',
    component: Dashboard,
    meta: { requiresAuth: true, requiresAdmin: true }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// Kiểm tra token còn hợp lệ
function isTokenValid() {
  const token = localStorage.getItem('token')
  if (!token) return false

  try {
    // Decode JWT để kiểm tra expiration
    const payload = JSON.parse(atob(token.split('.')[1]))
    const exp = payload.exp * 1000 // Convert to milliseconds
    
    // Nếu token đã hết hạn
    if (Date.now() >= exp) {
      // Xóa toàn bộ localStorage
      localStorage.clear()
      return false
    }
    
    return true
  } catch (error) {
    // Token không hợp lệ
    localStorage.clear()
    return false
  }
}

// Navigation Guard - Kiểm tra quyền truy cập
router.beforeEach((to, from, next) => {
  // Kiểm tra token hợp lệ
  const hasValidToken = isTokenValid()
  const roles = JSON.parse(localStorage.getItem('roles') || '[]')
  const isAdmin = roles.includes('ROLE_ADMIN') || roles.includes('ROLE_SUPER_ADMIN')

  // Nếu đã login mà vào trang login/register -> redirect về home
  if (hasValidToken && (to.path === '/login' || to.path === '/register')) {
    next('/')
    return
  }

  // Kiểm tra yêu cầu đăng nhập
  if (to.meta.requiresAuth && !hasValidToken) {
    next('/login')
    return
  }

  // Kiểm tra quyền admin
  if (to.meta.requiresAdmin && !isAdmin) {
    alert('Bạn không có quyền truy cập trang này!')
    next('/')
    return
  }

  next()
})

export default router