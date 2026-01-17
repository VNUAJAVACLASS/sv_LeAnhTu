import { defineStore } from 'pinia'
import api from '@/api/axios'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: localStorage.getItem('token'),
    user: localStorage.getItem('username')
      ? {
          username: localStorage.getItem('username'),
          roles: JSON.parse(localStorage.getItem('roles') || '[]')
        }
      : null
  }),

  getters: {
    isLoggedIn: state => !!state.token,

    isAdmin: state =>
      state.user?.roles?.includes('ROLE_ADMIN') ||
      state.user?.roles?.includes('ROLE_SUPER_ADMIN')
  },

  actions: {
   async login(username, password) {
  const res = await api.post('/auth/login', {
    username,
    password
  })

  const { accessToken, roles } = res.data

  localStorage.setItem('token', accessToken)
  localStorage.setItem('username', username)
  localStorage.setItem('roles', JSON.stringify(roles))

  this.token = accessToken
  this.user = { username, roles }
},

    logout() {
      localStorage.clear()
      this.token = null
      this.user = null
    }
  }
})
