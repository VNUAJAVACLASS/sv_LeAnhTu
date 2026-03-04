<script setup>
import { ref, onMounted } from 'vue'
import api from '@/api/axios'
import { DanhMucApi } from '@/api/book.api'
import Swal from 'sweetalert2'
import Pagination from '@/components/common/Pagination.vue'

const books = ref([])
const danhMucList = ref([])

const form = ref({
  id: null,
  tenSach: '',
  tacGia: '',
  gia: 0,
  soLuong: 0,
  moTa: '',
  danhMucId: null,
  tenDanhMucMoi: ''
})

// 'existing' = chọn từ list, 'new' = nhập mới, null = không chọn
const danhMucMode = ref(null)

const isEditing = ref(false)
const showForm = ref(false)

const currentPage = ref(0)
const pageSize = ref(20)
const totalPages = ref(0)
const totalItems = ref(0)
const hasNext = ref(false)
const hasPrevious = ref(false)

const loadBooks = async (page = 0) => {
  try {
    const res = await api.get('/books', { params: { page, size: pageSize.value } })
    const data = res.data
    books.value = data.content || []
    currentPage.value = data.currentPage
    totalPages.value = data.totalPages
    totalItems.value = data.totalItems
    hasNext.value = data.hasNext
    hasPrevious.value = data.hasPrevious
  } catch (e) { console.error(e) }
}

const loadDanhMuc = async () => {
  try {
    const res = await DanhMucApi.getAll()
    danhMucList.value = res.data
  } catch (e) { console.error(e) }
}

onMounted(() => { loadBooks(); loadDanhMuc() })

const resetForm = () => ({
  id: null, tenSach: '', tacGia: '', gia: 0,
  soLuong: 0, moTa: '', danhMucId: null, tenDanhMucMoi: ''
})

const openAddForm = () => {
  form.value = resetForm()
  danhMucMode.value = null
  isEditing.value = false
  showForm.value = true
}

const editBook = (book) => {
  form.value = {
    ...resetForm(),
    id: book.id,
    tenSach: book.tenSach,
    tacGia: book.tacGia,
    gia: book.gia,
    soLuong: book.soLuong,
    moTa: book.moTa,
    danhMucId: book.danhMuc?.id || null
  }
  danhMucMode.value = book.danhMuc ? 'existing' : null
  isEditing.value = true
  showForm.value = true
}

// Validate: chỉ được chọn 1 trong 2
const validateDanhMuc = () => {
  if (danhMucMode.value === 'existing' && !form.value.danhMucId) {
    Swal.fire('Lỗi!', 'Vui lòng chọn danh mục từ danh sách', 'error')
    return false
  }
  if (danhMucMode.value === 'new' && !form.value.tenDanhMucMoi.trim()) {
    Swal.fire('Lỗi!', 'Vui lòng nhập tên danh mục mới', 'error')
    return false
  }
  return true
}

const buildPayload = () => {
  const payload = {
    tenSach: form.value.tenSach,
    tacGia: form.value.tacGia,
    gia: form.value.gia,
    soLuong: form.value.soLuong,
    moTa: form.value.moTa
  }
  if (danhMucMode.value === 'existing' && form.value.danhMucId) {
    payload.danhMucId = form.value.danhMucId
  } else if (danhMucMode.value === 'new' && form.value.tenDanhMucMoi.trim()) {
    payload.tenDanhMucMoi = form.value.tenDanhMucMoi.trim()
  }
  return payload
}

const saveBook = async () => {
  if (!form.value.tenSach.trim()) { Swal.fire('Lỗi!', 'Tên sách không được để trống', 'error'); return }
  if (!validateDanhMuc()) return

  try {
    const payload = buildPayload()
    if (isEditing.value) {
      await api.patch(`/books/${form.value.id}`, payload)
      Swal.fire('Thành công!', 'Cập nhật sách thành công', 'success')
    } else {
      await api.post('/books', payload)
      Swal.fire('Thành công!', 'Thêm sách mới thành công', 'success')
    }
    showForm.value = false
    loadBooks(currentPage.value)
    loadDanhMuc() // reload danh mục phòng có mới
 } catch (e) {
    const msg = e.response?.data || ''
    if (
      typeof msg === 'string' && (
        msg.toLowerCase().includes('duplicate') ||
        msg.toLowerCase().includes('ten_sach') ||
        msg.toLowerCase().includes('already exists')
      )
    ) {
      Swal.fire('Trùng tên sách!', `Sách "${form.value.tenSach}" đã tồn tại trong hệ thống. Vui lòng đặt tên khác.`, 'warning')
    } else {
      Swal.fire('Lỗi!', typeof msg === 'string' && msg ? msg : 'Không thể lưu sách', 'error')
    }
  }
}

const deleteBook = async (id) => {
  const result = await Swal.fire({
    title: 'Bạn có chắc chắn?', text: 'Sách sẽ bị xóa vĩnh viễn!', icon: 'warning',
    showCancelButton: true, confirmButtonColor: '#e74c3c', cancelButtonColor: '#95a5a6',
    confirmButtonText: 'Xóa', cancelButtonText: 'Hủy'
  })
  if (result.isConfirmed) {
    try {
      await api.delete(`/books/${id}`)
      Swal.fire('Đã xóa!', 'Sách đã được xóa thành công', 'success')
      loadBooks(currentPage.value)
    } catch (e) { Swal.fire('Lỗi!', 'Không thể xóa sách', 'error') }
  }
}

const formatPrice = (price) =>
  new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(price)
</script>

<template>
  <div class="manage-books">
    <div class="header">
      <h3>📚 Quản lý Sách</h3>
      <button @click="openAddForm" class="btn-add">
        <i class="fas fa-plus"></i> Thêm sách mới
      </button>
    </div>

    <!-- FORM THÊM/SỬA -->
    <div v-if="showForm" class="form-overlay">
      <div class="form-container">
        <h4>{{ isEditing ? '✏️ Sửa sách' : '➕ Thêm sách mới' }}</h4>

        <div class="form-group">
          <label>Tên sách: <span class="required">*</span></label>
          <input v-model="form.tenSach" type="text" placeholder="Nhập tên sách" />
        </div>

        <div class="form-group">
          <label>Tác giả:</label>
          <input v-model="form.tacGia" type="text" placeholder="Nhập tên tác giả" />
        </div>

        <div class="form-row">
          <div class="form-group">
            <label>Giá (VNĐ):</label>
            <input v-model="form.gia" type="number" placeholder="0" />
          </div>
          <div class="form-group">
            <label>Số lượng:</label>
            <input v-model="form.soLuong" type="number" placeholder="0" />
          </div>
        </div>

        <div class="form-group">
          <label>Mô tả:</label>
          <textarea v-model="form.moTa" rows="6" maxlength="1000" placeholder="Nhập mô tả sách"></textarea>
        </div>

        <!-- ===== CHỌN DANH MỤC ===== -->
        <div class="form-group danh-muc-section">
          <label>📂 Danh mục sách:</label>

          <!-- Lựa chọn phương thức -->
          <div class="mode-selector">
            <label class="mode-option" :class="{ selected: danhMucMode === 'existing' }">
              <input
                type="radio"
                value="existing"
                v-model="danhMucMode"
                @change="form.tenDanhMucMoi = ''"
              />
              Chọn danh mục có sẵn
            </label>
            <label class="mode-option" :class="{ selected: danhMucMode === 'new' }">
              <input
                type="radio"
                value="new"
                v-model="danhMucMode"
                @change="form.danhMucId = null"
              />
              Tạo danh mục mới
            </label>
          </div>

          <!-- CHOICEBOX: chọn danh mục có sẵn -->
          <div v-if="danhMucMode === 'existing'" class="danh-muc-grid">
            <label
              v-for="dm in danhMucList"
              :key="dm.id"
              class="danh-muc-chip"
              :class="{ selected: form.danhMucId === dm.id }"
              :style="form.danhMucId === dm.id ? `background:${dm.mauSac}; color: white; border-color: ${dm.mauSac}` : `border-color:${dm.mauSac}`"
            >
              <input type="radio" :value="dm.id" v-model="form.danhMucId" style="display:none" />
              {{ dm.icon }} {{ dm.tenDanhMuc }}
            </label>
          </div>

          <!-- TEXT FIELD: nhập danh mục mới -->
          <div v-if="danhMucMode === 'new'" class="new-category-input">
            <input
              v-model="form.tenDanhMucMoi"
              type="text"
              placeholder="Ví dụ: Văn học nước ngoài"
              class="new-cat-field"
            />
            <small>Nếu danh mục này đã tồn tại, hệ thống sẽ tự động sử dụng lại.</small>
          </div>
        </div>
        <!-- ===== KẾT THÚC CHỌN DANH MỤC ===== -->

        <div class="form-actions">
          <button @click="showForm = false" class="btn-cancel">Hủy</button>
          <button @click="saveBook" class="btn-save">
            {{ isEditing ? 'Cập nhật' : 'Thêm mới' }}
          </button>
        </div>
      </div>
    </div>

    <!-- BẢNG DANH SÁCH -->
    <div class="table-container">
      <table class="books-table">
        <thead>
          <tr>
            <th>ID</th>
            <th>Tên sách</th>
            <th>Tác giả</th>
            <th>Giá</th>
            <th>SL</th>
            <th>Danh mục</th>
            <th>Thao tác</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="book in books" :key="book.id">
            <td>{{ book.id }}</td>
            <td class="book-name">{{ book.tenSach }}</td>
            <td>{{ book.tacGia }}</td>
            <td class="price">{{ formatPrice(book.gia) }}</td>
            <td class="stock">{{ book.soLuong }}</td>
            <td>
              <span
                v-if="book.danhMuc"
                class="cat-tag"
                :style="`background:${book.danhMuc.mauSac}`"
              >
                {{ book.danhMuc.icon }} {{ book.danhMuc.tenDanhMuc }}
              </span>
              <span v-else class="cat-tag cat-none">—</span>
            </td>
            <td>
              <button @click="editBook(book)" class="btn-edit">
                <i class="fas fa-edit"></i>
              </button>
              <button @click="deleteBook(book.id)" class="btn-delete">
                <i class="fas fa-trash"></i>
              </button>
            </td>
          </tr>
        </tbody>
      </table>
      <div v-if="books.length === 0" class="empty-message"><p>Chưa có sách nào</p></div>
    </div>

    <Pagination
      :current-page="currentPage" :total-pages="totalPages"
      :total-items="totalItems" :has-next="hasNext" :has-previous="hasPrevious"
      item-name="sách" @page-change="loadBooks"
    />
  </div>
</template>

<style scoped>
.manage-books { padding: 20px; }
.header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 28px; }
.header h3 { margin: 0; color: #2c3e50; }
.btn-add { padding: 10px 20px; background: #27ae60; color: white; border: none; border-radius: 6px; cursor: pointer; font-weight: 600; }
.btn-add:hover { background: #229954; }

.form-overlay {
  position: fixed; top: 0; left: 0; width: 100%; height: 100%;
  background: rgba(0,0,0,0.5); display: flex; justify-content: center;
  align-items: center; z-index: 1000;
}
.form-container {
  background: white; padding: 30px; border-radius: 10px;
  width: 640px; max-height: 85vh; overflow-y: auto;
}
.form-container h4 { margin-bottom: 22px; color: #2c3e50; font-size: 18px; }

.form-group { margin-bottom: 18px; }
.form-group label { display: block; margin-bottom: 7px; font-weight: 600; color: #2c3e50; }
.required { color: #e74c3c; }
.form-group input, .form-group textarea {
  width: 100%; padding: 10px; border: 1px solid #ddd; border-radius: 6px; font-size: 14px;
}
.form-group input:focus, .form-group textarea:focus {
  outline: none; border-color: #3498db; box-shadow: 0 0 0 3px rgba(52,152,219,0.1);
}
.form-row { display: grid; grid-template-columns: 1fr 1fr; gap: 14px; }

/* DANH MỤC SECTION */
.danh-muc-section { background: #f8f9fa; padding: 16px; border-radius: 8px; border: 1px solid #e0e0e0; }

.mode-selector {
  display: flex; gap: 10px; margin-bottom: 14px; flex-wrap: wrap;
}
.mode-option {
  display: flex; align-items: center; gap: 6px;
  padding: 7px 14px; border: 2px solid #ddd; border-radius: 20px;
  cursor: pointer; font-size: 13px; font-weight: 500;
  background: white; transition: all 0.2s; user-select: none;
}
.mode-option:hover { border-color: #3498db; color: #3498db; }
.mode-option.selected { background: #3498db; color: white; border-color: #3498db; }

/* CHOICEBOX dạng chip */
.danh-muc-grid {
  display: flex; flex-wrap: wrap; gap: 8px; margin-top: 4px;
}
.danh-muc-chip {
  padding: 6px 14px; border: 2px solid #ddd;
  border-radius: 20px; cursor: pointer; font-size: 13px;
  font-weight: 500; background: white; transition: all 0.2s;
  user-select: none;
}
.danh-muc-chip:hover { opacity: 0.85; transform: scale(1.03); }
.danh-muc-chip.selected { box-shadow: 0 2px 8px rgba(0,0,0,0.2); font-weight: 700; }

/* New category input */
.new-category-input { margin-top: 6px; }
.new-cat-field {
  width: 100%; padding: 10px 14px; border: 2px solid #3498db;
  border-radius: 6px; font-size: 14px;
}
.new-cat-field:focus { outline: none; box-shadow: 0 0 0 3px rgba(52,152,219,0.15); }
.new-category-input small { display: block; margin-top: 5px; color: #7f8c8d; font-style: italic; }

.form-actions { display: flex; justify-content: flex-end; gap: 10px; margin-top: 22px; }
.btn-cancel, .btn-save {
  padding: 10px 24px; border: none; border-radius: 6px; cursor: pointer; font-weight: 600;
}
.btn-cancel { background: #95a5a6; color: white; }
.btn-cancel:hover { background: #7f8c8d; }
.btn-save { background: #3498db; color: white; }
.btn-save:hover { background: #2980b9; }

/* TABLE */
.table-container { background: white; border-radius: 8px; overflow: hidden; box-shadow: 0 2px 5px rgba(0,0,0,0.08); }
.books-table { width: 100%; border-collapse: collapse; }
.books-table th, .books-table td { padding: 13px 14px; text-align: left; border-bottom: 1px solid #f0f0f0; }
.books-table th { background: #34495e; color: white; font-weight: 600; }
.books-table tbody tr:hover { background: #f8f9fa; }
.book-name { font-weight: 600; color: #2c3e50; }
.price { color: #e74c3c; font-weight: 600; }
.stock { color: #27ae60; font-weight: 600; text-align: center; }

.cat-tag {
  color: white; padding: 3px 10px; border-radius: 12px;
  font-size: 12px; font-weight: 600; white-space: nowrap;
}
.cat-none { background: #bdc3c7; color: white; }

.btn-edit, .btn-delete {
  padding: 7px 11px; border: none; border-radius: 5px;
  cursor: pointer; margin-right: 4px; font-size: 13px;
}
.btn-edit { background: #3498db; color: white; }
.btn-edit:hover { background: #2980b9; }
.btn-delete { background: #e74c3c; color: white; }
.btn-delete:hover { background: #c0392b; }
.empty-message { padding: 40px; text-align: center; color: #7f8c8d; }

.char-count {
  font-size: 12px; font-weight: 400; color: #7f8c8d; float: right;
}
</style>