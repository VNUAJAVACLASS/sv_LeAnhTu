<script setup>
import { useCartStore } from '@/stores/cart.store'
import { useRouter } from 'vue-router'

const cartStore = useCartStore()
const router = useRouter()

const removeItem = (index) => {
  cartStore.removeFromCart(index)
}

const clearCart = () => {
  cartStore.clearCart()
}

const goHome = () => {
  router.push('/')
}
</script>

<template>
  <div class="cart-container">
    <h2>Giỏ hàng của bạn</h2>

    <div v-if="cartStore.items.length === 0">
      <p>Giỏ hàng đang trống</p>
      <button @click="goHome">Tiếp tục mua sắm</button>
    </div>

    <div v-else>
      <table class="cart-table">
        <thead>
          <tr>
            <th>#</th>
            <th>Tên sản phẩm</th>
            <th>Thao tác</th>
          </tr>
        </thead>

        <tbody>
          <tr v-for="(item, index) in cartStore.items" :key="index">
            <td>{{ index + 1 }}</td>
<td>
  {{ item.tenSach }} - {{ item.gia?.toLocaleString('vi-VN') }} VNĐ
</td>

            <td>
              <button @click="removeItem(index)" class="delete-btn">
                Xóa
              </button>
            </td>
          </tr>
        </tbody>
      </table>

      <div class="actions">
        <button @click="clearCart" class="clear-btn">
          Xóa toàn bộ
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.cart-container {
  padding: 20px;
}

.cart-table {
  width: 100%;
  border-collapse: collapse;
  margin-top: 10px;
}

.cart-table th,
.cart-table td {
  border: 1px solid #ddd;
  padding: 10px;
  text-align: left;
}

.delete-btn {
  background: red;
  color: white;
  border: none;
  padding: 5px 10px;
  cursor: pointer;
}

.clear-btn {
  margin-top: 10px;
  background: orange;
  color: white;
  border: none;
  padding: 8px 12px;
}

.actions {
  margin-top: 15px;
}
</style>
