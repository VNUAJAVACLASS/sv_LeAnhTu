<script setup>
import { ref, onMounted, computed, watch } from 'vue'
import { useRouter } from 'vue-router'
import api from '@/api/axios'
import { DanhMucApi } from '@/api/book.api'
import { useAuthStore } from '@/stores/auth.store'
import { useCartStore } from '@/stores/cart.store'

const auth = useAuthStore()
const router = useRouter()
const cartStore = useCartStore()

const books = ref([])
const allBooks = ref([])
const danhMucList = ref([])
const selectedDanhMuc = ref(null)
const sortType = ref('all')
const loading = ref(false)

const searchQuery = ref('')
const showSuggestions = ref(false)
const selectedSuggestionIndex = ref(-1)
const searchInputRef = ref(null)

const currentPage = ref(0)
const pageSize = ref(10)
const totalPages = ref(0)
const totalItems = ref(0)
const hasNext = ref(false)
const hasPrevious = ref(false)

let debounceTimer = null

const isLoggedIn = computed(() => auth.isLoggedIn)
const isAdmin = computed(() => auth.isAdmin)

const suggestions = computed(() => {
  if (!searchQuery.value || searchQuery.value.trim().length < 1) return { books: [], authors: [] }
  const query = searchQuery.value.toLowerCase().trim()
  const bookSuggestions = allBooks.value
    .filter(b => b.tenSach?.toLowerCase().includes(query))
    .slice(0, 5)
    .map(b => ({ type: 'book', id: b.id, text: b.tenSach, price: b.gia, stock: b.soLuong }))
  const authorSet = new Set()
  const authorSuggestions = allBooks.value
    .filter(b => {
      const tg = b.tacGia?.toLowerCase() || ''
      if (tg.includes(query) && !authorSet.has(b.tacGia)) { authorSet.add(b.tacGia); return true }
      return false
    })
    .slice(0, 3)
    .map(b => ({ type: 'author', text: b.tacGia }))
  return { books: bookSuggestions, authors: authorSuggestions }
})

const totalSuggestions = computed(() => suggestions.value.books.length + suggestions.value.authors.length)

const filteredBooks = computed(() => {
  if (!searchQuery.value || searchQuery.value.trim() === '') return books.value
  const query = searchQuery.value.toLowerCase().trim()
  return books.value.filter(b =>
    b.tenSach?.toLowerCase().includes(query) || b.tacGia?.toLowerCase().includes(query)
  )
})

const pageNumbers = computed(() => {
  const pages = [], maxVisible = 5
  let start = Math.max(0, currentPage.value - 2)
  let end = Math.min(totalPages.value - 1, start + maxVisible - 1)
  if (end - start < maxVisible - 1) start = Math.max(0, end - maxVisible + 1)
  for (let i = start; i <= end; i++) pages.push(i)
  return pages
})

watch(searchQuery, (v) => {
  showSuggestions.value = !!(v && v.trim().length > 0)
  selectedSuggestionIndex.value = -1
})

watch(sortType, () => loadBooks(0, false))

const loadAllBooks = async () => {
  try {
    const res = await api.get('/books', { params: { page: 0, size: 1000 } })
    allBooks.value = res.data.content || res.data || []
  } catch (e) { console.error(e) }
}

const loadDanhMuc = async () => {
  try {
    const res = await DanhMucApi.getAll()
    danhMucList.value = res.data
  } catch (e) { console.error(e) }
}

const selectDanhMuc = (id) => {
  selectedDanhMuc.value = selectedDanhMuc.value === id ? null : id
  currentPage.value = 0
  loadBooks(0, true)
}

const loadBooks = async (page = 0, immediate = false) => {
  if (debounceTimer) clearTimeout(debounceTimer)
  if (immediate) { await fetchBooks(page); return }
  debounceTimer = setTimeout(() => fetchBooks(page), 300)
}

const fetchBooks = async (page = 0) => {
  try {
    loading.value = true
    const params = { page, size: pageSize.value, sort: sortType.value }
    if (selectedDanhMuc.value) params.danhMucId = selectedDanhMuc.value
    const res = await api.get('/books', { params })
    const data = res.data
    books.value = data.content || []
    currentPage.value = data.currentPage
    totalPages.value = data.totalPages
    totalItems.value = data.totalItems
    hasNext.value = data.hasNext
    hasPrevious.value = data.hasPrevious
    window.scrollTo({ top: 0, behavior: 'smooth' })
  } catch (e) {
    console.error(e)
    alert('Không thể tải danh sách sách')
  } finally {
    loading.value = false
  }
}

const goToPage = (page) => { if (page >= 0 && page < totalPages.value) loadBooks(page, true) }
const previousPage = () => { if (hasPrevious.value) loadBooks(currentPage.value - 1, true) }
const nextPage = () => { if (hasNext.value) loadBooks(currentPage.value + 1, true) }

const selectSuggestion = (s) => {
  searchQuery.value = s.text
  showSuggestions.value = false
  selectedSuggestionIndex.value = -1
}

const handleKeyDown = (e) => {
  if (!showSuggestions.value || totalSuggestions.value === 0) return
  const all = [...suggestions.value.books, ...suggestions.value.authors]
  if (e.key === 'ArrowDown') { e.preventDefault(); selectedSuggestionIndex.value = Math.min(selectedSuggestionIndex.value + 1, totalSuggestions.value - 1) }
  else if (e.key === 'ArrowUp') { e.preventDefault(); selectedSuggestionIndex.value = Math.max(selectedSuggestionIndex.value - 1, -1) }
  else if (e.key === 'Enter') { e.preventDefault(); if (selectedSuggestionIndex.value >= 0) selectSuggestion(all[selectedSuggestionIndex.value]) }
  else if (e.key === 'Escape') { showSuggestions.value = false; selectedSuggestionIndex.value = -1 }
}

const handleClickOutside = (e) => {
  if (searchInputRef.value && !searchInputRef.value.contains(e.target)) showSuggestions.value = false
}

const clearSearch = () => { searchQuery.value = ''; showSuggestions.value = false }

const highlightText = (text, query) => {
  if (!query) return text
  return text.replace(new RegExp(`(${query})`, 'gi'), '<strong class="highlight">$1</strong>')
}

const goDetail = (id) => {
  if (!auth.isLoggedIn) { alert('Vui lòng đăng nhập để xem chi tiết sách!'); router.push('/login'); return }
  router.push(`/books/${id}`)
}

const addToCart = (book) => {
  if (!auth.isLoggedIn) { alert('Vui lòng đăng nhập để thêm sách vào giỏ!'); router.push('/login'); return }
  cartStore.addToCart(book)
  alert(`✅ Đã thêm "${book.tenSach}" vào giỏ hàng`)
}

const selectedDanhMucName = computed(() => {
  if (!selectedDanhMuc.value) return null
  return danhMucList.value.find(d => d.id === selectedDanhMuc.value)?.tenDanhMuc || null
})

onMounted(() => {
  loadBooks(0, true)
  loadAllBooks()
  loadDanhMuc()
  document.addEventListener('click', handleClickOutside)
})
</script>

<template>
  <div class="home-page">
    <div class="greeting">
      <h2>📚 Chào mừng bạn đến BookStore</h2>
    </div>

    <div class="home-container">

      <!-- ===== SIDEBAR DANH MỤC ===== -->
      <div class="filter-panel">
        <h4>🗂️ Danh mục</h4>

        <button
          class="category-btn"
          :class="{ active: selectedDanhMuc === null }"
          @click="selectDanhMuc(null)"
        >
          <span class="cat-icon">📖</span>
          <span class="cat-name">Tất cả</span>
        </button>

        <button
          v-for="dm in danhMucList"
          :key="dm.id"
          class="category-btn"
          :class="{ active: selectedDanhMuc === dm.id }"
          :style="selectedDanhMuc === dm.id ? `background:${dm.mauSac};border-color:${dm.mauSac}` : `border-left: 3px solid ${dm.mauSac}`"
          @click="selectDanhMuc(dm.id)"
        >
          <span class="cat-icon">{{ dm.icon }}</span>
          <span class="cat-name">{{ dm.tenDanhMuc }}</span>
        </button>
      </div>

      <!-- ===== MAIN CONTENT ===== -->
      <div class="main-content">

        <!-- BADGE DANH MỤC ĐANG CHỌN -->
        <div v-if="selectedDanhMucName" class="active-filter-bar">
          <span>📂 Đang xem:</span>
          <strong>{{ selectedDanhMucName }}</strong>
          <button class="clear-filter-btn" @click="selectDanhMuc(null)">✕ Bỏ lọc</button>
        </div>

        <!-- SEARCH BAR -->
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
            <button v-if="searchQuery" @click="clearSearch" class="clear-btn">
              <i class="fas fa-times"></i>
            </button>
          </div>

          <div v-if="showSuggestions && totalSuggestions > 0" class="suggestions-dropdown">
            <div v-if="suggestions.books.length > 0" class="suggestion-group">
              <div class="suggestion-header"><i class="fas fa-book"></i><span>Sách</span></div>
              <div
                v-for="(book, index) in suggestions.books" :key="'book-'+book.id"
                class="suggestion-item"
                :class="{ active: index === selectedSuggestionIndex, 'out-of-stock': book.stock === 0 }"
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
              <div class="suggestion-header"><i class="fas fa-user-edit"></i><span>Tác giả</span></div>
              <div
                v-for="(author, index) in suggestions.authors" :key="'author-'+index"
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
        </div>

        <!-- SORT BAR -->
        <div class="sort-bar">
          <span>Sắp xếp:</span>
          <select v-model="sortType">
            <option value="all">Mặc định</option>
            <option value="priceAsc">Giá thấp → cao</option>
            <option value="priceDesc">Giá cao → thấp</option>
            <option value="bestSeller">Bán chạy</option>
            <option value="newest">Mới nhất</option>
          </select>
          <span class="total-count" v-if="!searchQuery">
            {{ totalItems }} sách
          </span>
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
              <!-- Badge danh mục trên card -->
              <div
                v-if="b.danhMuc"
                class="book-category-badge"
                :style="`background: ${b.danhMuc.mauSac}`"
              >
                {{ b.danhMuc.icon }} {{ b.danhMuc.tenDanhMuc }}
              </div>
              <div class="book-image">📖</div>
              <h3>{{ b.tenSach }}</h3>
              <p class="author">Tác giả: {{ b.tacGia }}</p>
              <p class="price">{{ b.gia?.toLocaleString('vi-VN') }} đ</p>
              <p class="stock">Còn: {{ b.soLuong }} cuốn</p>
              <div class="actions">
                <button @click="goDetail(b.id)" class="btn-detail">
                  <i class="fas fa-eye"></i> Chi tiết
                </button>
                <button @click="addToCart(b)" class="btn-cart" :disabled="b.soLuong === 0">
                  <i class="fas fa-cart-plus"></i> Thêm giỏ
                </button>
              </div>
            </div>
          </div>

          <div v-else-if="searchQuery" class="empty-message">
            <i class="fas fa-search" style="font-size:48px;color:#95a5a6;margin-bottom:15px;"></i>
            <p>Không tìm thấy sách nào phù hợp với "<strong>{{ searchQuery }}</strong>"</p>
            <button @click="clearSearch" class="btn-clear-search">✕ Xóa tìm kiếm</button>
          </div>

          <div v-else class="empty-message">
            <p>Chưa có sách nào trong danh mục này</p>
          </div>

          <!-- PHÂN TRANG -->
          <div v-if="totalPages > 1 && !searchQuery" class="pagination">
            <button @click="previousPage" :disabled="!hasPrevious" class="page-btn" :class="{ disabled: !hasPrevious }">
              <i class="fas fa-chevron-left"></i> Trước
            </button>
            <button v-if="pageNumbers[0] > 0" @click="goToPage(0)" class="page-number">1</button>
            <span v-if="pageNumbers[0] > 1" class="page-dots">...</span>
            <button v-for="page in pageNumbers" :key="page" @click="goToPage(page)"
              class="page-number" :class="{ active: page === currentPage }">
              {{ page + 1 }}
            </button>
            <span v-if="pageNumbers[pageNumbers.length-1] < totalPages-2" class="page-dots">...</span>
            <button v-if="pageNumbers[pageNumbers.length-1] < totalPages-1"
              @click="goToPage(totalPages-1)" class="page-number">{{ totalPages }}</button>
            <button @click="nextPage" :disabled="!hasNext" class="page-btn" :class="{ disabled: !hasNext }">
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
.home-page { padding: 20px; }
.greeting { text-align: center; margin-bottom: 30px; }
.greeting h2 { color: #2c3e50; }

.home-container { display: flex; gap: 20px; }

/* ===== SIDEBAR DANH MỤC ===== */
.filter-panel {
  width: 220px;
  min-width: 180px;
  background: white;
  border-radius: 12px;
  padding: 18px 12px;
  height: fit-content;
  box-shadow: 0 2px 12px rgba(0,0,0,0.08);
  position: sticky;
  top: 80px;
}

.filter-panel h4 {
  color: #2c3e50;
  font-size: 15px;
  margin-bottom: 14px;
  padding-bottom: 10px;
  border-bottom: 2px solid #ecf0f1;
}

.category-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  width: 100%;
  padding: 9px 12px;
  background: #f8f9fa;
  border: 1px solid transparent;
  border-radius: 8px;
  cursor: pointer;
  font-size: 13px;
  color: #2c3e50;
  margin-bottom: 6px;
  transition: all 0.2s;
  text-align: left;
  border-left: 3px solid #ecf0f1;
}

.category-btn:hover {
  background: #eaf4fb;
  transform: translateX(3px);
}

.category-btn.active {
  background: #3498db;
  color: white;
  border-color: #3498db;
  font-weight: 600;
  box-shadow: 0 2px 8px rgba(52,152,219,0.35);
}

.cat-icon { font-size: 16px; flex-shrink: 0; }
.cat-name { font-size: 13px; line-height: 1.3; }

/* ACTIVE FILTER BAR */
.active-filter-bar {
  display: flex;
  align-items: center;
  gap: 10px;
  background: linear-gradient(135deg, #eaf4fb, #d6eaf8);
  border: 1px solid #aed6f1;
  border-radius: 8px;
  padding: 10px 16px;
  margin-bottom: 16px;
  font-size: 14px;
  color: #1a5276;
}

.active-filter-bar strong { color: #2980b9; }

.clear-filter-btn {
  margin-left: auto;
  background: #e74c3c;
  color: white;
  border: none;
  border-radius: 4px;
  padding: 4px 10px;
  cursor: pointer;
  font-size: 12px;
  transition: 0.2s;
}

.clear-filter-btn:hover { background: #c0392b; }

/* MAIN CONTENT */
.main-content { flex: 1; min-width: 0; }

/* SEARCH BAR */
.search-bar {
  position: relative;
  margin-bottom: 16px;
  background: white;
  padding: 16px;
  border-radius: 10px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.08);
}

.search-input-wrapper { position: relative; display: flex; align-items: center; }
.search-icon { position: absolute; left: 14px; color: #7f8c8d; pointer-events: none; }
.search-input {
  width: 100%;
  padding: 11px 44px;
  border: 2px solid #ddd;
  border-radius: 8px;
  font-size: 14px;
  transition: 0.3s;
}
.search-input:focus { outline: none; border-color: #3498db; box-shadow: 0 0 0 3px rgba(52,152,219,0.1); }
.clear-btn {
  position: absolute;
  right: 10px;
  background: #e74c3c;
  color: white;
  border: none;
  border-radius: 50%;
  width: 26px; height: 26px;
  cursor: pointer;
  display: flex; align-items: center; justify-content: center;
  transition: 0.2s;
}
.clear-btn:hover { background: #c0392b; transform: scale(1.1); }

.suggestions-dropdown {
  position: absolute;
  top: calc(100% + 5px);
  left: 16px; right: 16px;
  background: white;
  border: 1px solid #ddd;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.15);
  max-height: 380px;
  overflow-y: auto;
  z-index: 1000;
}
.suggestion-group { padding: 6px 0; }
.suggestion-header {
  display: flex; align-items: center; gap: 8px;
  padding: 7px 14px;
  color: #7f8c8d; font-size: 12px; font-weight: 600;
  text-transform: uppercase;
  background: #f8f9fa; border-bottom: 1px solid #ecf0f1;
}
.suggestion-item {
  padding: 10px 14px; cursor: pointer;
  transition: background 0.15s; border-bottom: 1px solid #f8f9fa;
}
.suggestion-item:hover, .suggestion-item.active { background: #eaf4fb; }
.suggestion-content { display: flex; align-items: center; gap: 10px; }
.suggestion-icon { color: #3498db; font-size: 15px; }
.suggestion-title { color: #2c3e50; font-size: 13px; margin-bottom: 3px; }
.suggestion-title :deep(.highlight) {
  background: #fff3cd; font-weight: 700; color: #856404; padding: 1px 3px; border-radius: 3px;
}
.suggestion-meta { display: flex; gap: 10px; font-size: 12px; }
.suggestion-meta .price { color: #e74c3c; font-weight: 600; }
.suggestion-meta .stock { color: #27ae60; }
.suggestion-meta .stock.no-stock { color: #e74c3c; }
.suggestion-divider { height: 1px; background: #ecf0f1; }
.no-suggestions { padding: 30px; text-align: center; color: #7f8c8d; }

/* SORT BAR */
.sort-bar {
  display: flex; align-items: center; gap: 12px;
  padding: 12px 16px; background: white;
  border-radius: 8px; margin-bottom: 16px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.06);
}
.sort-bar span { font-weight: 600; color: #2c3e50; font-size: 14px; }
.sort-bar select {
  padding: 7px 12px; border: 2px solid #ddd; border-radius: 6px;
  font-size: 13px; cursor: pointer; transition: 0.2s;
}
.sort-bar select:focus { outline: none; border-color: #3498db; }
.total-count { margin-left: auto; color: #7f8c8d; font-size: 13px; }

/* LOADING */
.loading { text-align: center; padding: 60px 20px; background: white; border-radius: 10px; }
.spinner {
  border: 4px solid #f3f3f3; border-top: 4px solid #3498db;
  border-radius: 50%; width: 44px; height: 44px;
  animation: spin 1s linear infinite; margin: 0 auto 16px;
}
@keyframes spin { 100% { transform: rotate(360deg); } }

/* BOOK GRID */
.grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(230px, 1fr));
  gap: 18px;
  margin-bottom: 24px;
}

.book-card {
  position: relative;
  padding: 18px;
  background: white;
  border-radius: 10px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.08);
  transition: transform 0.25s, box-shadow 0.25s;
  border: 1px solid #f0f0f0;
}
.book-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 6px 18px rgba(0,0,0,0.13);
}

/* Badge danh mục trên card */
.book-category-badge {
  position: absolute;
  top: 10px; right: 10px;
  color: white;
  font-size: 10px;
  font-weight: 600;
  padding: 3px 8px;
  border-radius: 12px;
  white-space: nowrap;
  max-width: 120px;
  overflow: hidden;
  text-overflow: ellipsis;
  box-shadow: 0 2px 4px rgba(0,0,0,0.2);
}

.book-image { font-size: 52px; text-align: center; margin-bottom: 12px; }
.book-card h3 { font-size: 15px; margin-bottom: 8px; color: #2c3e50; min-height: 44px; line-height: 1.4; }
.author { color: #7f8c8d; font-size: 13px; margin-bottom: 6px; }
.price { font-size: 18px; font-weight: bold; color: #e74c3c; margin-bottom: 6px; }
.stock { font-size: 13px; color: #27ae60; margin-bottom: 14px; }

.actions { display: flex; gap: 8px; }
.btn-detail, .btn-cart {
  flex: 1; padding: 9px 6px; border: none; border-radius: 6px;
  cursor: pointer; font-weight: 500; font-size: 13px; transition: 0.2s;
}
.btn-detail { background: #3498db; color: white; }
.btn-detail:hover { background: #2980b9; }
.btn-cart { background: #27ae60; color: white; }
.btn-cart:hover { background: #229954; }
.btn-cart:disabled { background: #95a5a6; cursor: not-allowed; }

.empty-message {
  text-align: center; padding: 60px 20px;
  background: white; border-radius: 10px;
}
.empty-message p { font-size: 16px; color: #7f8c8d; margin-bottom: 18px; }
.btn-clear-search {
  padding: 10px 20px; background: #e74c3c; color: white;
  border: none; border-radius: 6px; cursor: pointer; font-weight: 600;
}
.btn-clear-search:hover { background: #c0392b; }

/* PHÂN TRANG */
.pagination {
  display: flex; justify-content: center;
  align-items: center; gap: 7px;
  margin: 24px 0; flex-wrap: wrap;
}
.page-btn, .page-number {
  padding: 9px 14px; border: 1px solid #ddd;
  background: white; color: #2c3e50;
  border-radius: 5px; cursor: pointer;
  transition: all 0.2s; font-weight: 500;
  display: flex; align-items: center; gap: 4px;
}
.page-btn:hover:not(.disabled), .page-number:hover:not(.active) {
  background: #3498db; color: white; border-color: #3498db;
}
.page-number.active {
  background: #3498db; color: white; border-color: #3498db;
  font-weight: bold; cursor: default;
}
.page-btn.disabled { background: #ecf0f1; color: #bdc3c7; cursor: not-allowed; }
.page-dots { padding: 0 6px; color: #7f8c8d; font-weight: bold; }
.pagination-info {
  text-align: center; color: #7f8c8d; font-size: 13px;
  padding: 12px; background: #f8f9fa; border-radius: 6px;
}
.pagination-info strong { color: #2c3e50; font-size: 15px; }

@media (max-width: 768px) {
  .home-container { flex-direction: column; }
  .filter-panel { width: 100%; display: flex; flex-wrap: wrap; gap: 6px; position: static; }
  .filter-panel h4 { width: 100%; }
  .category-btn { width: auto; flex: none; padding: 7px 12px; }
  .grid { grid-template-columns: repeat(auto-fill, minmax(165px, 1fr)); gap: 12px; }
}
</style>