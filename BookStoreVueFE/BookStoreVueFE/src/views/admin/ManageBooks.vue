<script setup>
import { ref, onMounted } from 'vue'
import api from '@/api/axios'
import Swal from 'sweetalert2'

const books = ref([])
const form = ref({
  id: null,
  tenSach: '',
  tacGia: '',
  gia: 0,
  soLuong: 0,
  moTa: ''
})

const isEditing = ref(false)
const showForm = ref(false)

const loadBooks = async () => {
  try {
    const res = await api.get('/books')
    books.value = res.data
  } catch (error) {
    console.error('Lỗi tải sách:', error)
  }
}

onMounted(loadBooks)

const openAddForm = () => {
  form.value = {
    id: null,
    tenSach: '',
    tacGia: '',
    gia: 0,
    soLuong: 0,
    moTa: ''
  }
  isEditing.value = false
  showForm.value = true
}

const editBook = (book) => {
  form.value = { ...book }
  isEditing.value = true
  showForm.value = true
}

const deleteBook = async (id) => {
  const result = await Swal.fire({
    title: 'Bạn có chắc chắn?',
    text: "Sách sẽ bị xóa vĩnh viễn!",
    icon: 'warning',
    showCancelButton: true,
    confirmButtonColor: '#e74c3c',
    cancelButtonColor: '#95a5a6',
    confirmButtonText: 'Xóa',
    cancelButtonText: 'Hủy'
  })

  if (result.isConfirmed) {
    try {
      await api.delete(`/books/${id}`)
      Swal.fire('Đã xóa!', 'Sách đã được xóa thành công', 'success')
      loadBooks()
    } catch (error) {
      Swal.fire('Lỗi!', 'Không thể xóa sách', 'error')
    }
  }
}

const saveBook = async () => {
  try {
    if (isEditing.value) {
      await api.patch(`/books/${form.value.id}`, form.value)
      Swal.fire('Thành công!', 'Cập nhật sách thành công', 'success')
    } else {
      await api.post('/books', form.value)
      Swal.fire('Thành công!', 'Thêm sách mới thành công', 'success')
    }

    showForm.value = false
    loadBooks()
  } catch (error) {
    Swal.fire('Lỗi!', 'Không thể lưu sách', 'error')
  }
}

const cancelForm = () => {
  showForm.value = false
}

const formatPrice = (price) => {
  return new Intl.NumberFormat('vi-VN', { 
    style: 'currency', 
    currency: 'VND' 
  }).format(price)
}
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
          <label>Tên sách:</label>
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
          <textarea v-model="form.moTa" rows="4" placeholder="Nhập mô tả sách"></textarea>
        </div>

        <div class="form-actions">
          <button @click="cancelForm" class="btn-cancel">Hủy</button>
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
            <th>Số lượng</th>
            <th>Mô tả</th>
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
            <td class="description">{{ book.moTa }}</td>
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

      <div v-if="books.length === 0" class="empty-message">
        <p>Chưa có sách nào trong hệ thống</p>
      </div>
    </div>
  </div>
</template>

<style scoped>
.manage-books {
  padding: 20px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
}

.header h3 {
  margin: 0;
  color: #2c3e50;
}

.btn-add {
  padding: 10px 20px;
  background: #27ae60;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-weight: 600;
}

.btn-add:hover {
  background: #229954;
}

/* FORM OVERLAY */
.form-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0,0,0,0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.form-container {
  background: white;
  padding: 30px;
  border-radius: 8px;
  width: 600px;
  max-height: 80vh;
  overflow-y: auto;
}

.form-container h4 {
  margin-bottom: 20px;
  color: #2c3e50;
}

.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  font-weight: 600;
  color: #2c3e50;
}

.form-group input,
.form-group textarea {
  width: 100%;
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 15px;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 20px;
}

.btn-cancel, .btn-save {
  padding: 10px 24px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-weight: 600;
}

.btn-cancel {
  background: #95a5a6;
  color: white;
}

.btn-cancel:hover {
  background: #7f8c8d;
}

.btn-save {
  background: #3498db;
  color: white;
}

.btn-save:hover {
  background: #2980b9;
}

/* TABLE */
.table-container {
  background: white;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 5px rgba(0,0,0,0.1);
}

.books-table {
  width: 100%;
  border-collapse: collapse;
}

.books-table th,
.books-table td {
  padding: 15px;
  text-align: left;
  border-bottom: 1px solid #ddd;
}

.books-table th {
  background: #34495e;
  color: white;
  font-weight: 600;
}

.books-table tbody tr:hover {
  background: #f8f9fa;
}

.book-name {
  font-weight: 600;
  color: #2c3e50;
}

.price {
  color: #e74c3c;
  font-weight: 600;
}

.stock {
  color: #27ae60;
  font-weight: 600;
}

.description {
  max-width: 200px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.btn-edit, .btn-delete {
  padding: 8px 12px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  margin-right: 5px;
}

.btn-edit {
  background: #3498db;
  color: white;
}

.btn-edit:hover {
  background: #2980b9;
}

.btn-delete {
  background: #e74c3c;
  color: white;
}

.btn-delete:hover {
  background: #c0392b;
}

.empty-message {
  padding: 50px;
  text-align: center;
  color: #7f8c8d;
}
</style>