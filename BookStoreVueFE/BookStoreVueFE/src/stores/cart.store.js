import { defineStore } from 'pinia'

export const useCartStore = defineStore('cart', {
  state: () => ({
    items: []
  }),

  actions: {
    // Thêm sách vào giỏ
    addToCart(product) {
      // Kiểm tra sách đã có trong giỏ chưa
      const existingIndex = this.items.findIndex(
        item => item.id === product.id
      )

      if (existingIndex !== -1) {
        // Nếu đã có -> tăng số lượng
        this.items[existingIndex].quantity += 1
      } else {
        // Nếu chưa có -> thêm mới với quantity = 1
        this.items.push({
          ...product,
          quantity: 1
        })
      }
    },

    // Xóa sản phẩm khỏi giỏ
    removeFromCart(index) {
      this.items.splice(index, 1)
    },

    // Xóa toàn bộ giỏ hàng
    clearCart() {
      this.items = []
    },

    // Tăng số lượng
    increaseQuantity(index) {
      if (this.items[index]) {
        this.items[index].quantity += 1
      }
    },

    // Giảm số lượng
    decreaseQuantity(index) {
      if (this.items[index] && this.items[index].quantity > 1) {
        this.items[index].quantity -= 1
      }
    },

    // Cập nhật số lượng thủ công
    updateQuantity(index, quantity) {
      if (this.items[index]) {
        this.items[index].quantity = quantity
      }
    }
  }
})