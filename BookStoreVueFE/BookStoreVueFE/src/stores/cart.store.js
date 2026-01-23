import { defineStore } from 'pinia'
import { useAuthStore } from './auth.store'

export const useCartStore = defineStore('cart', {
  state: () => ({
    items: []
  }),

  getters: {
    // Tính tổng tiền giỏ hàng
    totalPrice: (state) => {
      return state.items.reduce((sum, item) => sum + (item.gia * item.quantity), 0)
    },

    // Tổng số lượng sản phẩm
    totalItems: (state) => {
      return state.items.reduce((sum, item) => sum + item.quantity, 0)
    }
  },

  actions: {
    // Khởi tạo giỏ hàng từ localStorage theo userId
    initCart() {
      const authStore = useAuthStore()
      const userId = authStore.userId

      if (!userId) {
        this.items = []
        return
      }

      const cartKey = `cart_user_${userId}`
      const savedCart = localStorage.getItem(cartKey)

      if (savedCart) {
        try {
          this.items = JSON.parse(savedCart)
        } catch (error) {
          console.error('Lỗi parse giỏ hàng:', error)
          this.items = []
        }
      } else {
        this.items = []
      }
    },

    // Lưu giỏ hàng vào localStorage theo userId
    saveCart() {
      const authStore = useAuthStore()
      const userId = authStore.userId

      if (!userId) return

      const cartKey = `cart_user_${userId}`
      localStorage.setItem(cartKey, JSON.stringify(this.items))
    },

    // Thêm sách vào giỏ
    addToCart(product) {
      const existingIndex = this.items.findIndex(item => item.id === product.id)

      if (existingIndex !== -1) {
        this.items[existingIndex].quantity += 1
      } else {
        this.items.push({
          ...product,
          quantity: 1
        })
      }

      this.saveCart()
    },

    // Xóa sản phẩm khỏi giỏ
    removeFromCart(index) {
      this.items.splice(index, 1)
      this.saveCart()
    },

    // Xóa toàn bộ giỏ hàng
    clearCart() {
      this.items = []
      this.saveCart()
    },

    // Tăng số lượng
    increaseQuantity(index) {
      if (this.items[index]) {
        this.items[index].quantity += 1
        this.saveCart()
      }
    },

    // Giảm số lượng
    decreaseQuantity(index) {
      if (this.items[index] && this.items[index].quantity > 1) {
        this.items[index].quantity -= 1
        this.saveCart()
      }
    },

    //Cập nhật số lượng thủ công
    updateQuantity(index, quantity) {
      if (this.items[index] && quantity > 0) {
        this.items[index].quantity = quantity
        this.saveCart()
      }
    }
  }
})