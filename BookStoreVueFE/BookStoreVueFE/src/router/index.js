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

// Navigation Guard - Kiểm tra quyền truy cập
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  const roles = JSON.parse(localStorage.getItem('roles') || '[]')
  
  const isAdmin = roles.includes('ROLE_ADMIN') || roles.includes('ROLE_SUPER_ADMIN')

  // Nếu đã login mà vào trang login/register -> redirect về home
  if (token && (to.path === '/login' || to.path === '/register')) {
    next('/')
    return
  }

  // Kiểm tra yêu cầu đăng nhập
  if (to.meta.requiresAuth && !token) {
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