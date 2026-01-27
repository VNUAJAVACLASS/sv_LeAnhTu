<script setup>
import { computed } from 'vue'
import { useCartStore } from '@/stores/cart.store'
import { useAuthStore } from '@/stores/auth.store'
import { useRouter } from 'vue-router'
import api from '@/api/axios'
import Swal from 'sweetalert2'

const cartStore = useCartStore()
const authStore = useAuthStore()
const router = useRouter()

const removeItem = (index) => {
  if (confirm('Bạn có chắc muốn xóa sản phẩm này?')) {
    cartStore.removeFromCart(index)
  }
}

const clearCart = () => {
  if (confirm('Bạn có chắc muốn xóa toàn bộ giỏ hàng?')) {
    cartStore.clearCart()
  }
}

const goHome = () => {
  router.push('/')
}

// THANH TOÁN TRỰC TIẾP
const checkout = async () => {
  if (cartStore.items.length === 0) {
    Swal.fire({
      icon: 'warning',
      title: 'Giỏ hàng trống!',
      text: 'Vui lòng thêm sách vào giỏ hàng trước khi thanh toán',
      confirmButtonText: 'OK'
    })
    return
  }

  // Chuẩn bị dữ liệu gửi lên API
  const orderData = {
    userId: authStore.userId,
    items: cartStore.items.map(item => ({
      bookId: item.id,
      soLuong: item.quantity
    }))
  }

  try {
    // Hiển thị loading
    Swal.fire({
      title: 'Đang xử lý...',
      text: 'Vui lòng đợi trong giây lát',
      allowOutsideClick: false,
      didOpen: () => {
        Swal.showLoading()
      }
    })

    // Gọi API tạo đơn hàng
    const response = await api.post('/orders', orderData)

    // Xóa giỏ hàng sau khi đặt hàng thành công
    cartStore.clearCart()

    // Thông báo thành công
    Swal.fire({
      icon: 'success',
      title: 'Đặt hàng thành công!',
      html: `
        <p>Mã đơn hàng: <strong>#${response.data.id}</strong></p>
        <p>Tổng tiền: <strong>${totalPrice.value.toLocaleString('vi-VN')} đ</strong></p>
        <p>Đơn hàng của bạn đang được xử lý</p>
      `,
      confirmButtonText: 'Xem đơn hàng',
      showCancelButton: true,
      cancelButtonText: 'Tiếp tục mua sắm'
    }).then((result) => {
      if (result.isConfirmed) {
        router.push('/profile')
      } else {
        router.push('/')
      }
    })

  } catch (error) {
    console.error('Lỗi đặt hàng:', error)
    
    let errorMessage = 'Có lỗi xảy ra khi đặt hàng'
    
    if (error.response?.data) {
      errorMessage = error.response.data
    }

    Swal.fire({
      icon: 'error',
      title: 'Đặt hàng thất bại!',
      text: errorMessage,
      confirmButtonText: 'OK'
    })
  }
}

// Tính tổng tiền
const totalPrice = computed(() => {
  return cartStore.items.reduce((sum, item) => {
    return sum + (item.gia * item.quantity)
  }, 0)
})

// Tăng số lượng
const increaseQuantity = (index) => {
  cartStore.increaseQuantity(index)
}

// Giảm số lượng
const decreaseQuantity = (index) => {
  cartStore.decreaseQuantity(index)
}

// Cập nhật số lượng thủ công
const updateQuantity = (index, value) => {
  const qty = parseInt(value)
  if (qty > 0) {
    cartStore.updateQuantity(index, qty)
  } else {
    Swal.fire({
      icon: 'warning',
      title: 'Số lượng không hợp lệ',
      text: 'Số lượng phải lớn hơn 0',
      confirmButtonText: 'OK'
    })
  }
}
</script>

<template>
  <div class="cart-container">
    <h2>🛒 Giỏ hàng của bạn</h2>

    <div v-if="cartStore.items.length === 0" class="empty-cart">
      <p>Giỏ hàng đang trống</p>
      <button @click="goHome" class="btn-continue">Tiếp tục mua sắm</button>
    </div>

    <div v-else class="cart-content">
      <table class="cart-table">
        <thead>
          <tr>
            <th>STT</th>
            <th>Tên sách</th>
            <th>Đơn giá</th>
            <th>Số lượng</th>
            <th>Thành tiền</th>
            <th>Thao tác</th>
          </tr>
        </thead>

        <tbody>
          <tr v-for="(item, index) in cartStore.items" :key="index">
            <td>{{ index + 1 }}</td>
            <td class="book-name">{{ item.tenSach }}</td>
            <td class="price">{{ item.gia?.toLocaleString('vi-VN') }} đ</td>

            <td>
              <div class="quantity-control">
                <button
                  @click="decreaseQuantity(index)"
                  class="btn-minus"
                  :disabled="item.quantity <= 1"
                >
                  −
                </button>

                <input
                  type="number"
                  :value="item.quantity"
                  @change="updateQuantity(index, $event.target.value)"
                  min="1"
                  class="quantity-input"
                />

                <button
                  @click="increaseQuantity(index)"
                  class="btn-plus"
                >
                  +
                </button>
              </div>
            </td>

            <td class="total-price">
              {{ (item.gia * item.quantity).toLocaleString('vi-VN') }} đ
            </td>

            <td>
              <button @click="removeItem(index)" class="btn-delete">
                <i class="fas fa-trash"></i> Xóa
              </button>
            </td>
          </tr>
        </tbody>
      </table>

      <!-- Tổng kết -->
      <div class="cart-summary">
        <div class="summary-row">
          <span>Tổng số lượng:</span>
          <strong>{{ cartStore.items.length }} sản phẩm</strong>
        </div>

        <div class="summary-row total">
          <span>Tổng tiền:</span>
          <strong class="total-amount">{{ totalPrice.toLocaleString('vi-VN') }} đ</strong>
        </div>
      </div>

      <!-- Actions -->
      <div class="actions">
        <button @click="goHome" class="btn-continue">
          <i class="fas fa-arrow-left"></i> Tiếp tục mua sắm
        </button>

        <button @click="clearCart" class="btn-clear">
          <i class="fas fa-trash-alt"></i> Xóa toàn bộ
        </button>

        <button @click="checkout" class="btn-checkout">
          <i class="fas fa-credit-card"></i> Thanh toán trực tiếp
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.cart-container {
  max-width: 1200px;
  margin: 30px auto;
  padding: 20px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.1);
}

h2 {
  text-align: center;
  color: #2c3e50;
  margin-bottom: 30px;
}

.empty-cart {
  text-align: center;
  padding: 50px;
}

.empty-cart p {
  font-size: 18px;
  color: #7f8c8d;
  margin-bottom: 20px;
}

.cart-table {
  width: 100%;
  border-collapse: collapse;
  margin-bottom: 30px;
}

.cart-table th,
.cart-table td {
  border: 1px solid #ddd;
  padding: 15px;
  text-align: center;
}

.cart-table th {
  background: #34495e;
  color: white;
  font-weight: 600;
}

.cart-table tbody tr:hover {
  background: #f8f9fa;
}

.book-name {
  text-align: left;
  font-weight: 500;
}

.price, .total-price {
  color: #e74c3c;
  font-weight: 600;
}

.quantity-control {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 5px;
}

.btn-minus, .btn-plus {
  width: 30px;
  height: 30px;
  border: 1px solid #ddd;
  background: #ecf0f1;
  cursor: pointer;
  font-weight: bold;
  border-radius: 4px;
}

.btn-minus:hover, .btn-plus:hover {
  background: #bdc3c7;
}

.btn-minus:disabled {
  background: #e0e0e0;
  cursor: not-allowed;
}

.quantity-input {
  width: 60px;
  text-align: center;
  border: 1px solid #ddd;
  padding: 5px;
  border-radius: 4px;
}

.btn-delete {
  background: #e74c3c;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 4px;
  cursor: pointer;
}

.btn-delete:hover {
  background: #c0392b;
}

.cart-summary {
  border-top: 2px solid #ddd;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 8px;
}

.summary-row {
  display: flex;
  justify-content: space-between;
  padding: 10px 0;
  font-size: 16px;
}

.summary-row.total {
  border-top: 1px solid #ddd;
  margin-top: 10px;
  padding-top: 15px;
  font-size: 20px;
}

.total-amount {
  color: #e74c3c;
  font-size: 24px;
}

.actions {
  margin-top: 30px;
  display: flex;
  justify-content: space-between;
  gap: 15px;
}

.btn-continue, .btn-clear, .btn-checkout {
  padding: 12px 24px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-weight: 600;
  font-size: 16px;
  transition: 0.3s;
}

.btn-continue {
  background: #3498db;
  color: white;
}

.btn-continue:hover {
  background: #2980b9;
}

.btn-clear {
  background: #95a5a6;
  color: white;
}

.btn-clear:hover {
  background: #7f8c8d;
}

.btn-checkout {
  background: #27ae60;
  color: white;
  flex: 1;
}

.btn-checkout:hover {
  background: #229954;
}
</style>