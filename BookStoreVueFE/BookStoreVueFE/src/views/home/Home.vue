<script setup>
import { ref, onMounted } from 'vue'
import api from '@/api/axios'
import { useAuthStore } from '@/stores/auth.store'
import { useRouter } from 'vue-router'
import { useCartStore } from '@/stores/cart.store'

const auth = useAuthStore()
const router = useRouter()
const cartStore = useCartStore()

const books = ref([])
const sortType = ref('all')

onMounted(async () => {
  const res = await api.get('/books')
  books.value = res.data
})

const goDetail = (id) => {
  if (!auth.isLoggedIn) {
    router.push('/login')
    return
  }
  router.push(`/books/${id}`)
}

const addToCart = (book) => {
  if (!auth.isLoggedIn) {
    router.push('/login')
    return
  }

  cartStore.addToCart(book)

  alert(`Đã thêm "${book.tenSach}" vào giỏ`)
}

// Tạm thời chỉ demo – sau này xử lý thật
const changeSort = () => {
  console.log("Sort type:", sortType.value)
}
</script>

<template>
  <div>

    <!-- GREETING -->
    <h2 v-if="auth.isLoggedIn">
      👋 Xin chào, <b>{{ auth.user.username }}</b>
    </h2>

    <h2 v-else>
      📚 Chào mừng bạn đến BookStore
    </h2>

    <div class="home-container">

      <!-- LEFT: FILTER PANEL (TRỐNG THEO YÊU CẦU) -->
      <div class="filter-panel">
        <!-- Sau này thêm danh mục vào đây -->
      </div>

      <!-- RIGHT: MAIN CONTENT -->
      <div class="main-content">

        <!-- SORT BAR -->
        <div class="sort-bar">
          <span>Sắp xếp theo:</span>

          <select v-model="sortType" @change="changeSort">
            <option value="all">Tất cả</option>
            <option value="priceAsc">Giá thấp → cao</option>
            <option value="priceDesc">Giá cao → thấp</option>
            <option value="bestSeller">Bán chạy</option>
            <option value="newest">Mới nhất</option>
          </select>
        </div>

        <!-- BOOK GRID -->
        <div class="grid">
          <div class="book" v-for="b in books" :key="b.id">
            <h3>{{ b.tenSach }}</h3>
            <p>Tác giả: {{ b.tacGia }}</p>
            <p>Giá: {{ b.gia?.toLocaleString('vi-VN') }} đ</p>

            <button @click="goDetail(b.id)">Xem chi tiết</button>
            <button @click="addToCart(b)">Thêm vào giỏ</button>
          </div>
        </div>

      </div>
    </div>

  </div>
</template>

<style scoped>
.home-container {
  display: flex;
  margin-top: 20px;
}

/* LEFT PANEL */
.filter-panel {
  width: 22%;
  border-right: 1px solid #ddd;
  padding: 15px;
}

/* RIGHT CONTENT */
.main-content {
  width: 78%;
  padding: 15px;
}

/* SORT BAR */
.sort-bar {
  margin-bottom: 15px;
  display: flex;
  align-items: center;
  gap: 10px;
}

/* BOOK GRID */
.grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.book {
  padding: 12px;
  border: 1px solid #ccc;
  background: white;
}

button {
  margin-right: 8px;
  margin-top: 8px;
}
</style>
