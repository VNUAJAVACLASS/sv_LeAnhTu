<script setup>
import { ref, onMounted, computed, watch } from 'vue'
import { useRouter } from 'vue-router'
import api from '@/api/axios'
import { useAuthStore } from '@/stores/auth.store'
import { useCartStore } from '@/stores/cart.store'

const auth = useAuthStore()
const router = useRouter()
const cartStore = useCartStore()

// State
const books = ref([])
const allBooks = ref([]) 
const sortType = ref('all')
const loading = ref(false)

// ===== TÌM KIẾM AUTOCOMPLETE =====
const searchQuery = ref('')
const showSuggestions = ref(false)
const selectedSuggestionIndex = ref(-1)
const searchInputRef = ref(null)

// ===== PHÂN TRANG =====
const currentPage = ref(0)
const pageSize = ref(10)
const totalPages = ref(0)
const totalItems = ref(0)
const hasNext = ref(false)
const hasPrevious = ref(false)

// ===== DEBOUNCE TIMER =====
let debounceTimer = null

// Computed properties
const isLoggedIn = computed(() => auth.isLoggedIn)
const isAdmin = computed(() => auth.isAdmin)
const username = computed(() => auth.user?.username || '')

// ===== GỢI Ý TÌM KIẾM (AUTOCOMPLETE) =====
const suggestions = computed(() => {
  if (!searchQuery.value || searchQuery.value.trim().length < 1) {
    return { books: [], authors: [] }
  }

  const query = searchQuery.value.toLowerCase().trim()

  // Lọc sách theo tên
  const bookSuggestions = allBooks.value
    .filter(book => book.tenSach?.toLowerCase().includes(query))
    .slice(0, 5)
    .map(book => ({
      type: 'book',
      id: book.id,
      text: book.tenSach,
      price: book.gia,
      stock: book.soLuong
    }))

  // Lọc tác giả (loại bỏ trùng lặp)
  const authorSet = new Set()
  const authorSuggestions = allBooks.value
    .filter(book => {
      const tacGia = book.tacGia?.toLowerCase() || ''
      if (tacGia.includes(query) && !authorSet.has(book.tacGia)) {
        authorSet.add(book.tacGia)
        return true
      }
      return false
    })
    .slice(0, 5)
    .map(book => ({
      type: 'author',
      text: book.tacGia
    }))

  return {
    books: bookSuggestions,
    authors: authorSuggestions
  }
})

// Tổng số gợi ý
const totalSuggestions = computed(() => {
  return suggestions.value.books.length + suggestions.value.authors.length
})

// ===== WATCH TÌM KIẾM =====
watch(searchQuery, (newVal) => {
  if (newVal && newVal.trim().length > 0) {
    showSuggestions.value = true
    selectedSuggestionIndex.value = -1
  } else {
    showSuggestions.value = false
  }
})

// ===== TÌM KIẾM LOCAL =====
const filteredBooks = computed(() => {
  if (!searchQuery.value || searchQuery.value.trim() === '') {
    return books.value
  }

  const query = searchQuery.value.toLowerCase().trim()
  return books.value.filter(book => {
    const tenSach = book.tenSach?.toLowerCase() || ''
    const tacGia = book.tacGia?.toLowerCase() || ''
    return tenSach.includes(query) || tacGia.includes(query)
  })
})

// Tính số trang hiển thị trong pagination
const pageNumbers = computed(() => {
  const pages = []
  const maxVisible = 5

  let start = Math.max(0, currentPage.value - 2)
  let end = Math.min(totalPages.value - 1, start + maxVisible - 1)

  if (end - start < maxVisible - 1) {
    start = Math.max(0, end - maxVisible + 1)
  }

  for (let i = start; i <= end; i++) {
    pages.push(i)
  }

  return pages
})

// ===== LOAD TẤT CẢ SÁCH ĐỂ TÌM KIẾM (1 LẦN DUY NHẤT) =====
const loadAllBooks = async () => {
  try {
    const response = await api.get('/books', {
      params: { page: 0, size: 1000 }
    })
    allBooks.value = response.data.content || response.data || []
  } catch (error) {
    console.error('Lỗi tải sách:', error)
  }
}

// ===== ✅ LOAD SÁCH VỚI DEBOUNCE =====
const loadBooks = async (page = 0, immediate = false) => {
  // Hủy timer cũ nếu có
  if (debounceTimer) {
    clearTimeout(debounceTimer)
  }

  // Nếu immediate = true, gọi API ngay lập tức (không debounce)
  if (immediate) {
    await fetchBooks(page)
    return
  }

  // Debounce: Chờ 300ms sau lần gọi cuối cùng mới thực sự fetch
  debounceTimer = setTimeout(async () => {
    await fetchBooks(page)
  }, 300)
}

// ===== FETCH BOOKS FROM API =====
const fetchBooks = async (page = 0) => {
  try {
    loading.value = true

    const response = await api.get('/books', {
      params: {
        page: page,
        size: pageSize.value,
        sort: sortType.value
      }
    })

    const data = response.data

    books.value = data.content || []
    currentPage.value = data.currentPage
    totalPages.value = data.totalPages
    totalItems.value = data.totalItems
    hasNext.value = data.hasNext
    hasPrevious.value = data.hasPrevious

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
    loadBooks(page, true) // ✅ Chuyển trang KHÔNG debounce
  }
}

const previousPage = () => {
  if (hasPrevious.value) {
    loadBooks(currentPage.value - 1, true) // ✅ KHÔNG debounce
  }
}

const nextPage = () => {
  if (hasNext.value) {
    loadBooks(currentPage.value + 1, true) // ✅ KHÔNG debounce
  }
}

// ===== ✅ XỬ LÝ THAY ĐỔI SẮP XẾP (CÓ DEBOUNCE) =====
const changeSort = () => {
  console.log("Sort type:", sortType.value)
  // Reset về trang đầu khi thay đổi cách sắp xếp
  // ✅ SỬ DỤNG DEBOUNCE để tránh gọi API liên tục khi user thay đổi nhanh
  loadBooks(0, false)
}

// ===== ✅ WATCH SORT TYPE (TỰ ĐỘNG GỌI API KHI THAY ĐỔI) =====
watch(sortType, () => {
  loadBooks(0, false) // ✅ Có debounce
})

// Load sách khi component mount
onMounted(() => {
  loadBooks(0, true) // ✅ Lần đầu load KHÔNG debounce
  loadAllBooks()
})

// ===== XỬ LÝ CHỌN GỢI Ý =====
const selectSuggestion = (suggestion) => {
  if (suggestion.type === 'book') {
    searchQuery.value = suggestion.text
  } else {
    searchQuery.value = suggestion.text
  }
  showSuggestions.value = false
  selectedSuggestionIndex.value = -1
}

// ===== XỬ LÝ BÀN PHÍM =====
const handleKeyDown = (event) => {
  if (!showSuggestions.value || totalSuggestions.value === 0) return

  const allSuggestions = [
    ...suggestions.value.books,
    ...suggestions.value.authors
  ]

  if (event.key === 'ArrowDown') {
    event.preventDefault()
    selectedSuggestionIndex.value = Math.min(
      selectedSuggestionIndex.value + 1,
      totalSuggestions.value - 1
    )
  } else if (event.key === 'ArrowUp') {
    event.preventDefault()
    selectedSuggestionIndex.value = Math.max(
      selectedSuggestionIndex.value - 1,
      -1
    )
  } else if (event.key === 'Enter') {
    event.preventDefault()
    if (selectedSuggestionIndex.value >= 0) {
      selectSuggestion(allSuggestions[selectedSuggestionIndex.value])
    }
  } else if (event.key === 'Escape') {
    showSuggestions.value = false
    selectedSuggestionIndex.value = -1
  }
}

// ===== ĐÓNG GỢI Ý KHI CLICK BÊNN NGOÀI =====
const handleClickOutside = (event) => {
  if (searchInputRef.value && !searchInputRef.value.contains(event.target)) {
    showSuggestions.value = false
  }
}

onMounted(() => {
  document.addEventListener('click', handleClickOutside)
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

// ===== XÓA TÌM KIẾM =====
const clearSearch = () => {
  searchQuery.value = ''
  showSuggestions.value = false
  selectedSuggestionIndex.value = -1
}

// ===== HIGHLIGHT TEXT =====
const highlightText = (text, query) => {
  if (!query) return text
  const regex = new RegExp(`(${query})`, 'gi')
  return text.replace(regex, '<strong class="highlight">$1</strong>')
}
</script>

<template>
  <!-- TEMPLATE GIỮ NGUYÊN NHƯ TRƯỚC -->
  <div class="home-page">
    <div class="greeting">
      <h2>📚 Chào mừng bạn đến BookStore</h2>
    </div>

    <div class="home-container">
      <div class="filter-panel">
        <h4>Danh mục</h4>
        <p>Chức năng lọc sẽ được thêm sau...</p>
      </div>

      <div class="main-content">
        <!-- THANH TÌM KIẾM -->
        <div class="search-bar" ref="searchInputRef">
          <div class="search-input-wrapper">
            <i class="fas fa-search search-icon"></i>
            <input
              v-model="searchQuery"
              type="text"
              placeholder="🔍 Tìm kiếm sách, tác giả..."
              class="search-input"
              @keydown="handleKeyDown"
              @focus="searchQuery && (showSuggestions = true)"
            />
            <button
              v-if="searchQuery"
              @click="clearSearch"
              class="clear-btn"
              title="Xóa tìm kiếm"
            >
              <i class="fas fa-times"></i>
            </button>
          </div>

          <!-- DROPDOWN GỢI Ý -->
          <div v-if="showSuggestions && totalSuggestions > 0" class="suggestions-dropdown">
            <div v-if="suggestions.books.length > 0" class="suggestion-group">
              <div class="suggestion-header">
                <i class="fas fa-book"></i>
                <span>Sách</span>
              </div>
              <div
                v-for="(book, index) in suggestions.books"
                :key="'book-' + book.id"
                class="suggestion-item"
                :class="{
                  active: index === selectedSuggestionIndex,
                  'out-of-stock': book.stock === 0
                }"
                @click="selectSuggestion(book)"
              >
                <div class="suggestion-content">
                  <i class="fas fa-book-open suggestion-icon"></i>
                  <div class="suggestion-text">
                    <div class="suggestion-title" v-html="highlightText(book.text, searchQuery)"></div>
                    <div class="suggestion-meta">
                      <span class="price">{{ book.price?.toLocaleString('vi-VN') }} đ</span>
                      <span class="stock" :class="{ 'no-stock': book.stock === 0 }">
                        {{ book.stock > 0 ? `Còn ${book.stock} cuốn` : 'Hết hàng' }}
                      </span>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <div v-if="suggestions.books.length > 0 && suggestions.authors.length > 0" class="suggestion-divider"></div>

            <div v-if="suggestions.authors.length > 0" class="suggestion-group">
              <div class="suggestion-header">
                <i class="fas fa-user-edit"></i>
                <span>Tác giả</span>
              </div>
              <div
                v-for="(author, index) in suggestions.authors"
                :key="'author-' + index"
                class="suggestion-item"
                :class="{ active: (suggestions.books.length + index) === selectedSuggestionIndex }"
                @click="selectSuggestion(author)"
              >
                <div class="suggestion-content">
                  <i class="fas fa-user suggestion-icon"></i>
                  <div class="suggestion-text">
                    <div class="suggestion-title" v-html="highlightText(author.text, searchQuery)"></div>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <div v-else-if="showSuggestions && searchQuery && totalSuggestions === 0" class="suggestions-dropdown">
            <div class="no-suggestions">
              <i class="fas fa-search"></i>
              <p>Không tìm thấy kết quả cho "<strong>{{ searchQuery }}</strong>"</p>
            </div>
          </div>

          <div v-if="searchQuery && !showSuggestions" class="search-result-info">
            <span class="result-count">
              Tìm thấy <strong>{{ filteredBooks.length }}</strong> kết quả
              cho "<strong>{{ searchQuery }}</strong>"
            </span>
          </div>
        </div>

        <!-- SORT BAR -->
        <div class="sort-bar">
          <span>Sắp xếp theo:</span>
          <select v-model="sortType">
            <option value="all">Mặc định</option>
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
          <div v-if="filteredBooks.length > 0" class="grid">
            <div class="book-card" v-for="b in filteredBooks" :key="b.id">
              <div class="book-image">📖</div>
              <h3>{{ b.tenSach }}</h3>
              <p class="author">Tác giả: {{ b.tacGia }}</p>
              <p class="price">{{ b.gia?.toLocaleString('vi-VN') }} đ</p>
              <p class="stock">Còn: {{ b.soLuong }} cuốn</p>
              <div class="actions">
                <button @click="goDetail(b.id)" class="btn-detail">
                  <i class="fas fa-eye"></i> Chi tiết
                </button>
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

          <div v-else-if="searchQuery" class="empty-message">
            <i class="fas fa-search" style="font-size: 48px; color: #95a5a6; margin-bottom: 15px;"></i>
            <p>Không tìm thấy sách nào phù hợp với "<strong>{{ searchQuery }}</strong>"</p>
            <button @click="clearSearch" class="btn-clear-search">
              <i class="fas fa-times"></i> Xóa tìm kiếm
            </button>
          </div>

          <div v-else class="empty-message">
            <p>Chưa có sách nào trong hệ thống</p>
          </div>

          <!-- PHÂN TRANG -->
          <div v-if="totalPages > 1 && !searchQuery" class="pagination">
            <button
              @click="previousPage"
              :disabled="!hasPrevious"
              class="page-btn"
              :class="{ disabled: !hasPrevious }"
            >
              <i class="fas fa-chevron-left"></i> Trước
            </button>

            <button
              v-if="pageNumbers[0] > 0"
              @click="goToPage(0)"
              class="page-number"
            >
              1
            </button>

            <span v-if="pageNumbers[0] > 1" class="page-dots">...</span>

            <button
              v-for="page in pageNumbers"
              :key="page"
              @click="goToPage(page)"
              class="page-number"
              :class="{ active: page === currentPage }"
            >
              {{ page + 1 }}
            </button>

            <span v-if="pageNumbers[pageNumbers.length - 1] < totalPages - 2" class="page-dots">...</span>

            <button
              v-if="pageNumbers[pageNumbers.length - 1] < totalPages - 1"
              @click="goToPage(totalPages - 1)"
              class="page-number"
            >
              {{ totalPages }}
            </button>

            <button
              @click="nextPage"
              :disabled="!hasNext"
              class="page-btn"
              :class="{ disabled: !hasNext }"
            >
              Sau <i class="fas fa-chevron-right"></i>
            </button>
          </div>

          <div v-if="totalPages > 1 && !searchQuery" class="pagination-info">
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

/* ===== THANH TÌM KIẾM AUTOCOMPLETE ===== */
.search-bar {
  position: relative;
  margin-bottom: 20px;
  background: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.search-input-wrapper {
  position: relative;
  display: flex;
  align-items: center;
}

.search-icon {
  position: absolute;
  left: 15px;
  color: #7f8c8d;
  font-size: 16px;
  pointer-events: none;
  z-index: 1;
}

.search-input {
  width: 100%;
  padding: 12px 45px 12px 45px;
  border: 2px solid #ddd;
  border-radius: 8px;
  font-size: 15px;
  transition: all 0.3s;
}

.search-input:focus {
  outline: none;
  border-color: #3498db;
  box-shadow: 0 0 0 3px rgba(52, 152, 219, 0.1);
}

.clear-btn {
  position: absolute;
  right: 10px;
  background: #e74c3c;
  color: white;
  border: none;
  border-radius: 50%;
  width: 28px;
  height: 28px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: 0.3s;
  z-index: 1;
}

.clear-btn:hover {
  background: #c0392b;
  transform: scale(1.1);
}

/* ===== DROPDOWN GỢI Ý ===== */
.suggestions-dropdown {
  position: absolute;
  top: calc(100% + 5px);
  left: 20px;
  right: 20px;
  background: white;
  border: 1px solid #ddd;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.15);
  max-height: 400px;
  overflow-y: auto;
  z-index: 1000;
}

.suggestion-group {
  padding: 8px 0;
}

.suggestion-header {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  color: #7f8c8d;
  font-size: 13px;
  font-weight: 600;
  text-transform: uppercase;
  background: #f8f9fa;
  border-bottom: 1px solid #ecf0f1;
}

.suggestion-header i {
  font-size: 12px;
}

.suggestion-item {
  padding: 12px 16px;
  cursor: pointer;
  transition: background 0.2s;
  border-bottom: 1px solid #f8f9fa;
}

.suggestion-item:hover,
.suggestion-item.active {
  background: #e8f4f8;
}

.suggestion-item.out-of-stock {
  opacity: 0.6;
}

.suggestion-content {
  display: flex;
  align-items: center;
  gap: 12px;
}

.suggestion-icon {
  color: #3498db;
  font-size: 16px;
  min-width: 20px;
}

.suggestion-text {
  flex: 1;
}

.suggestion-title {
  color: #2c3e50;
  font-size: 14px;
  margin-bottom: 4px;
}

.suggestion-title :deep(.highlight) {
  background: #fff3cd;
  font-weight: 700;
  color: #856404;
  padding: 2px 4px;
  border-radius: 3px;
}

.suggestion-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 12px;
}

.suggestion-meta .price {
  color: #e74c3c;
  font-weight: 600;
}

.suggestion-meta .stock {
  color: #27ae60;
}

.suggestion-meta .stock.no-stock {
  color: #e74c3c;
}

.suggestion-divider {
  height: 1px;
  background: #ecf0f1;
  margin: 4px 0;
}

.no-suggestions {
  padding: 40px 20px;
  text-align: center;
  color: #7f8c8d;
}

.no-suggestions i {
  font-size: 32px;
  margin-bottom: 12px;
  opacity: 0.5;
}

.no-suggestions p {
  margin: 0;
  font-size: 14px;
}

.search-result-info {
  margin-top: 12px;
  padding: 8px 12px;
  background: #e8f4f8;
  border-left: 3px solid #3498db;
  border-radius: 4px;
}

.result-count {
  color: #2c3e50;
  font-size: 14px;
}

.result-count strong {
  color: #3498db;
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

.sort-bar span {
  font-weight: 600;
  color: #2c3e50;
}

.sort-bar select {
  padding: 8px 12px;
  border: 2px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
  cursor: pointer;
  transition: 0.3s;
}

.sort-bar select:focus {
  outline: none;
  border-color: #3498db;
}

.sort-bar select:hover {
  border-color: #3498db;
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
  padding: 60px 20px;
  background: white;
  border-radius: 8px;
}

.empty-message p {
  font-size: 18px;
  color: #7f8c8d;
  margin-bottom: 20px;
}

.btn-clear-search {
  padding: 10px 20px;
  background: #e74c3c;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-weight: 600;
  transition: 0.3s;
}

.btn-clear-search:hover {
  background: #c0392b;
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

  .search-input {
    font-size: 14px;
  }

  .suggestions-dropdown {
    left: 10px;
    right: 10px;
  }
}

/* Scrollbar cho dropdown */
.suggestions-dropdown::-webkit-scrollbar {
  width: 6px;
}

.suggestions-dropdown::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 4px;
}

.suggestions-dropdown::-webkit-scrollbar-thumb {
  background: #bdc3c7;
  border-radius: 4px;
}

.suggestions-dropdown::-webkit-scrollbar-thumb:hover {
  background: #95a5a6;
}
</style>