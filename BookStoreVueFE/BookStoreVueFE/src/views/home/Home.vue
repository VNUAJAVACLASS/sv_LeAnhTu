<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import api from '@/api/axios'
import { useAuthStore } from '@/stores/auth.store'
import { useCartStore } from '@/stores/cart.store'

const auth = useAuthStore()
const router = useRouter()
const cartStore = useCartStore()

const books = ref([])
const sortType = ref('all')

// ✅ FIX: Thêm computed properties
const isLoggedIn = computed(() => auth.isLoggedIn)
const isAdmin = computed(() => auth.isAdmin)
const username = computed(() => auth.user?.username || '')

onMounted(async () => {
  try {
    const res = await api.get('/books')
    books.value = res.data
  } catch (error) {
    console.error('Lỗi tải sách:', error)
  }
})

// Yêu cầu đăng nhập trước khi xem chi tiết
const goDetail = (id) => {
  if (!auth.isLoggedIn) {
    alert('Vui lòng đăng nhập để xem chi tiết sách!')
    router.push('/login')
    return
  }
  router.push(`/books/${id}`)
}

// Yêu cầu đăng nhập trước khi thêm giỏ hàng
const addToCart = (book) => {
  if (!auth.isLoggedIn) {
    alert('Vui lòng đăng nhập để thêm sách vào giỏ hàng!')
    router.push('/login')
    return
  }

  // Kiểm tra admin không được mua hàng
  if (auth.isAdmin) {
    alert('Tài khoản Admin không thể mua hàng!')
    return
  }

  cartStore.addToCart(book)
  alert(`✅ Đã thêm "${book.tenSach}" vào giỏ hàng`)
}

const changeSort = () => {
  // TODO: Xử lý sắp xếp sau
  console.log("Sort type:", sortType.value)
}
</script>

<template>
  <div class="home-page">

    <!-- GREETING -->
    <div class="greeting">
      <h2>
        📚 Chào mừng bạn đến BookStore
      </h2>
    </div>

    <div class="home-container">

      <!-- LEFT: FILTER PANEL -->
      <div class="filter-panel">
        <h4>Danh mục</h4>
        <p>Chức năng lọc sẽ được thêm sau...</p>
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
          <div class="book-card" v-for="b in books" :key="b.id">
            <div class="book-image">📖</div>

            <h3>{{ b.tenSach }}</h3>
            <p class="author">Tác giả: {{ b.tacGia }}</p>
            <p class="price">{{ b.gia?.toLocaleString('vi-VN') }} đ</p>
            <p class="stock">Còn: {{ b.soLuong }} cuốn</p>

            <div class="actions">
              <button @click="goDetail(b.id)" class="btn-detail">
                <i class="fas fa-eye"></i> Chi tiết
              </button>

              <!-- CHỈ hiện nút "Thêm giỏ" nếu KHÔNG phải Admin -->
              <button
                v-if="!isAdmin"
                @click="addToCart(b)"
                class="btn-cart"
                :disabled="b.soLuong === 0"
              >
                <i class="fas fa-cart-plus"></i> Thêm giỏ
              </button>
            </div>
          </div>
        </div>

        <!-- Thông báo nếu không có sách -->
        <div v-if="books.length === 0" class="empty-message">
          <p>Chưa có sách nào trong hệ thống</p>
        </div>

      </div>
    </div>

  </div>
</template>

<style scoped>
.home-page {
  padding: 20px;
}

.greeting {
  text-align: center;
  margin-bottom: 30px;
}

.greeting h2 {
  color: #2c3e50;
}

.home-container {
  display: flex;
  gap: 20px;
}

/* LEFT PANEL */
.filter-panel {
  width: 20%;
  border-right: 1px solid #ddd;
  padding: 15px;
  background: white;
  border-radius: 8px;
  height: fit-content;
}

.filter-panel h4 {
  margin-bottom: 15px;
  color: #2c3e50;
}

/* RIGHT CONTENT */
.main-content {
  width: 80%;
}

/* SORT BAR */
.sort-bar {
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 15px;
  background: white;
  border-radius: 8px;
}

.sort-bar select {
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
}

/* BOOK GRID */
.grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
  gap: 20px;
}

.book-card {
  padding: 20px;
  border: 1px solid #ddd;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 5px rgba(0,0,0,0.1);
  transition: transform 0.3s;
}

.book-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 4px 10px rgba(0,0,0,0.15);
}

.book-image {
  font-size: 60px;
  text-align: center;
  margin-bottom: 15px;
}

.book-card h3 {
  font-size: 18px;
  margin-bottom: 10px;
  color: #2c3e50;
  min-height: 50px;
}

.author {
  color: #7f8c8d;
  font-size: 14px;
  margin-bottom: 8px;
}

.price {
  font-size: 20px;
  font-weight: bold;
  color: #e74c3c;
  margin-bottom: 8px;
}

.stock {
  font-size: 14px;
  color: #27ae60;
  margin-bottom: 15px;
}

.actions {
  display: flex;
  gap: 10px;
}

.btn-detail, .btn-cart {
  flex: 1;
  padding: 10px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-weight: 500;
  transition: 0.3s;
}

.btn-detail {
  background: #3498db;
  color: white;
}

.btn-detail:hover {
  background: #2980b9;
}

.btn-cart {
  background: #27ae60;
  color: white;
}

.btn-cart:hover {
  background: #229954;
}

.btn-cart:disabled {
  background: #95a5a6;
  cursor: not-allowed;
}

.empty-message {
  text-align: center;
  padding: 50px;
  background: white;
  border-radius: 8px;
}

.empty-message p {
  font-size: 18px;
  color: #7f8c8d;
}
</style>