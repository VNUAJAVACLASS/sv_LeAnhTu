<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { BookApi } from '@/api/book.api'
import { useCartStore } from '@/stores/cart.store'

const route = useRoute()
const router = useRouter()
const cartStore = useCartStore()

const book = ref(null)
const loading = ref(true)

// Lấy thông tin sách khi mở trang
onMounted(async () => {
  try {
    const res = await BookApi.getById(route.params.id)
    book.value = res.data
  } catch (e) {
    alert('Không tìm thấy sách')
    router.push('/')
  } finally {
    loading.value = false
  }
})

// Thêm vào giỏ
const addToCart = () => {
  cartStore.addToCart(book.value)
  alert(`Đã thêm "${book.value.tenSach}" vào giỏ`)
}

// Quay lại trang chủ
const goBack = () => {
  router.push('/')
}
</script>

<template>
  <div class="detail-container">
    <button class="back-btn" @click="goBack">⬅ Quay lại</button>

    <div v-if="loading">
      <p>Đang tải thông tin sách...</p>
    </div>

    <div v-else-if="book" class="detail-box">
      <h2>{{ book.tenSach }}</h2>

      <p><b>Tác giả:</b> {{ book.tacGia }}</p>
      <p><b>Giá:</b> {{ book.gia?.toLocaleString('vi-VN') }} đ</p>

      <p v-if="book.moTa">
        <b>Mô tả:</b> {{ book.moTa }}
      </p>

      <div class="actions">
        <button @click="addToCart" class="add-btn">
          🛒 Thêm vào giỏ hàng
        </button>

        <button @click="goBack" class="back-btn">
          ⬅ Quay lại
        </button>
      </div>
    </div>

    <div v-else>
      <p>Không tìm thấy thông tin sách</p>
      <button @click="goBack">Quay lại</button>
    </div>
  </div>
</template>

<style scoped>
.detail-container {
  max-width: 600px;
  margin: 30px auto;
  padding: 20px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.detail-box h2 {
  margin-bottom: 10px;
}

.actions {
  margin-top: 20px;
  display: flex;
  gap: 10px;
}

.add-btn {
  background: green;
  color: white;
  border: none;
  padding: 10px 16px;
  cursor: pointer;
}

.back-btn {
  background: #007bff;
  color: white;
  border: none;
  padding: 10px 16px;
  cursor: pointer;
}
</style>
