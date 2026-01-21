import { defineStore } from 'pinia'
import api from '@/api/axios'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: localStorage.getItem('token') || null,
    user: null
  }),

  getters: {
    isLoggedIn: (state) => !!state.token,

    isAdmin: (state) => {
      if (!state.user || !state.user.roles) return false
      return state.user.roles.includes('ROLE_ADMIN') ||
             state.user.roles.includes('ROLE_SUPER_ADMIN')
    },

    userId: (state) => state.user?.id
  },

  actions: {
    // ✅ Khởi tạo user từ localStorage khi app load
    initAuth() {
      const token = localStorage.getItem('token')
      const username = localStorage.getItem('username')
      const userId = localStorage.getItem('userId')
      const roles = localStorage.getItem('roles')

      if (token && username) {
        this.token = token
        this.user = {
          id: userId ? parseInt(userId) : null,
          username: username,
          roles: roles ? JSON.parse(roles) : []
        }
      }
    },

    async login(username, password) {
      try {
        const res = await api.post('/auth/login', {
          username,
          password
        })

        const { accessToken, roles } = res.data

        // Lấy thông tin user đầy đủ
        const userRes = await api.get('/users/me', {
          headers: { Authorization: `Bearer ${accessToken}` }
        })

        const userId = userRes.data.id

        // Lưu vào localStorage
        localStorage.setItem('token', accessToken)
        localStorage.setItem('username', username)
        localStorage.setItem('userId', userId.toString())
        localStorage.setItem('roles', JSON.stringify(roles))

        // ✅ Cập nhật state ngay lập tức
        this.token = accessToken
        this.user = { 
          id: userId, 
          username: username,  // ✅ Đảm bảo username được set
          roles: roles 
        }

        console.log('✅ Login success, user:', this.user) // Debug log

        return res.data
      } catch (error) {
        this.logout()
        throw error
      }
    },

    logout() {
      // Xóa localStorage
      localStorage.removeItem('token')
      localStorage.removeItem('username')
      localStorage.removeItem('userId')
      localStorage.removeItem('roles')

      // Reset state
      this.token = null
      this.user = null
    }
  }
})