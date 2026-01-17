import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth.store'

const routes = [
  { path: '/', component: () => import('@/views/home/Home.vue') },
  { path: '/login', component: () => import('@/views/auth/Login.vue') },
  { path: '/register', component: () => import('@/views/auth/Register.vue') },
  {
    path: '/books/:id',
    component: () => import('@/views/home/BookDetail.vue'),
    meta: { requiresAuth: true }
  },
 {
  path: '/admin',
  component: () => import('@/views/admin/Admin.vue'),
  meta: { requiresAuth: true, role: ['ADMIN', 'SUPER_ADMIN'] }
},
{
  path: '/cart',
  name: 'cart',
  component: () => import('@/views/cart/Cart.vue')
},
{
  path: '/profile',
  component: () => import('@/views/profile/Profile.vue'),
  meta: { requiresAuth: true }
}


]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const auth = useAuthStore()

  // Chưa login mà vào trang cần auth
  if (to.meta.requiresAuth && !auth.isLoggedIn) {
    next('/login')
    return
  }

  // Có yêu cầu role nhưng không phải admin
  if (to.meta.role && !auth.isAdmin) {
    next('/')
    return
  }

  next()
})


export default router
