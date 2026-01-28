<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import api from '@/api/axios'
import { useAuthStore } from '@/stores/auth.store'
import { useCartStore } from '@/stores/cart.store'

const auth = useAuthStore()
const router = useRouter()
const cartStore = useCartStore()

// State
const books = ref([])
const sortType = ref('all')
const loading = ref(false)

// ===== PHÂN TRANG =====
const currentPage = ref(0)      // Trang hiện tại (bắt đầu từ 0)
const pageSize = ref(10)        // Số sách mỗi trang
const totalPages = ref(0)       // Tổng số trang
const totalItems = ref(0)       // Tổng số sách
const hasNext = ref(false)      // Có trang tiếp theo?
const hasPrevious = ref(false)  // Có trang trước?

// Computed properties
const isLoggedIn = computed(() => auth.isLoggedIn)
const isAdmin = computed(() => auth.isAdmin)
const username = computed(() => auth.user?.username || '')

// Tính số trang hiển thị trong pagination
const pageNumbers = computed(() => {
  const pages = []
  const maxVisible = 5 // Số trang hiển thị tối đa
  
  let start = Math.max(0, currentPage.value - 2)
  let end = Math.min(totalPages.value - 1, start + maxVisible - 1)
  
  // Điều chỉnh start nếu end gần cuối
  if (end - start < maxVisible - 1) {
    start = Math.max(0, end - maxVisible + 1)
  }
  
  for (let i = start; i <= end; i++) {
    pages.push(i)
  }
  
  return pages
})

// ===== LOAD SÁCH VỚI PHÂN TRANG =====
const loadBooks = async (page = 0) => {
  try {
    loading.value = true
    
    // Gọi API với query params
    const response = await api.get('/books', {
      params: {
        page: page,
        size: pageSize.value
      }
    })
    
    const data = response.data
    
    // Cập nhật dữ liệu
    books.value = data.content || []
    currentPage.value = data.currentPage
    totalPages.value = data.totalPages
    totalItems.value = data.totalItems
    hasNext.value = data.hasNext
    hasPrevious.value = data.hasPrevious
    
    // Scroll to top khi chuyển trang
    window.scrollTo({ top: 0, behavior: 'smooth' })
    
  } catch (error) {
    console.error('Lỗi tải sách:', error)
    alert('Không thể tải danh sách sách')
  } finally {
    loading.value = false
  }
}

// ===== ĐIỀU HƯỚNG TRANG =====
const goToPage = (page) => {
  if (page >= 0 && page < totalPages.value) {
    loadBooks(page)
  }
}

const previousPage = () => {
  if (hasPrevious.value) {
    loadBooks(currentPage.value - 1)
  }
}

const nextPage = () => {
  if (hasNext.value) {
    loadBooks(currentPage.value + 1)
  }
}

// Load sách khi component mount
onMounted(() => {
  loadBooks()
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

        <!-- LOADING -->
        <div v-if="loading" class="loading">
          <div class="spinner"></div>
          <p>Đang tải sách...</p>
        </div>

        <!-- BOOK GRID -->
        <div v-else>
          <div v-if="books.length > 0" class="grid">
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
                  @click="addToCart(b)"
                  class="btn-cart"
                  :disabled="b.soLuong === 0"
                >
                  <i class="fas fa-cart-plus"></i> Thêm giỏ
                </button>
              </div>
            </div>
          </div>

          <!-- Empty state -->
          <div v-else class="empty-message">
            <p>Chưa có sách nào trong hệ thống</p>
          </div>

          <!-- ===== PHÂN TRANG ===== -->
          <div v-if="totalPages > 1" class="pagination">
            
            <!-- Nút Previous -->
            <button 
              @click="previousPage" 
              :disabled="!hasPrevious"
              class="page-btn"
              :class="{ disabled: !hasPrevious }"
            >
              <i class="fas fa-chevron-left"></i> Trước
            </button>

            <!-- Trang đầu -->
            <button 
              v-if="pageNumbers[0] > 0"
              @click="goToPage(0)"
              class="page-number"
            >
              1
            </button>

            <!-- Dấu ... -->
            <span v-if="pageNumbers[0] > 1" class="page-dots">...</span>

            <!-- Các số trang -->
            <button
              v-for="page in pageNumbers"
              :key="page"
              @click="goToPage(page)"
              class="page-number"
              :class="{ active: page === currentPage }"
            >
              {{ page + 1 }}
            </button>

            <!-- Dấu ... -->
            <span v-if="pageNumbers[pageNumbers.length - 1] < totalPages - 2" class="page-dots">...</span>

            <!-- Trang cuối -->
            <button 
              v-if="pageNumbers[pageNumbers.length - 1] < totalPages - 1"
              @click="goToPage(totalPages - 1)"
              class="page-number"
            >
              {{ totalPages }}
            </button>

            <!-- Nút Next -->
            <button 
              @click="nextPage" 
              :disabled="!hasNext"
              class="page-btn"
              :class="{ disabled: !hasNext }"
            >
              Sau <i class="fas fa-chevron-right"></i>
            </button>

          </div>

          <!-- Thông tin phân trang -->
          <div v-if="totalPages > 1" class="pagination-info">
            Trang <strong>{{ currentPage + 1 }}</strong> / <strong>{{ totalPages }}</strong> 
            (Tổng: <strong>{{ totalItems }}</strong> sách)
          </div>
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
  margin-bottom: 10px;
}

.total-books {
  color: #7f8c8d;
  font-size: 16px;
}

.total-books strong {
  color: #e74c3c;
  font-size: 18px;
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

/* LOADING */
.loading {
  text-align: center;
  padding: 60px 20px;
  background: white;
  border-radius: 8px;
}

.spinner {
  border: 4px solid #f3f3f3;
  border-top: 4px solid #3498db;
  border-radius: 50%;
  width: 50px;
  height: 50px;
  animation: spin 1s linear infinite;
  margin: 0 auto 20px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

/* BOOK GRID */
.grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
  gap: 20px;
  margin-bottom: 30px;
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

/* ===== PHÂN TRANG ===== */
.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 8px;
  margin: 30px 0;
  flex-wrap: wrap;
}

.page-btn,
.page-number {
  padding: 10px 16px;
  border: 1px solid #ddd;
  background: white;
  color: #2c3e50;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s;
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 5px;
}

.page-btn:hover:not(.disabled),
.page-number:hover:not(.active) {
  background: #3498db;
  color: white;
  border-color: #3498db;
}

.page-number.active {
  background: #3498db;
  color: white;
  border-color: #3498db;
  font-weight: bold;
  cursor: default;
}

.page-btn.disabled {
  background: #ecf0f1;
  color: #95a5a6;
  cursor: not-allowed;
  border-color: #ecf0f1;
}

.page-dots {
  padding: 0 8px;
  color: #7f8c8d;
  font-weight: bold;
}

.pagination-info {
  text-align: center;
  color: #7f8c8d;
  font-size: 14px;
  padding: 15px;
  background: #f8f9fa;
  border-radius: 4px;
}

.pagination-info strong {
  color: #2c3e50;
  font-size: 16px;
}

/* Responsive */
@media (max-width: 768px) {
  .home-container {
    flex-direction: column;
  }
  
  .filter-panel,
  .main-content {
    width: 100%;
  }
  
  .grid {
    grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  }
  
  .pagination {
    gap: 5px;
  }
  
  .page-btn,
  .page-number {
    padding: 8px 12px;
    font-size: 14px;
  }
}
</style>