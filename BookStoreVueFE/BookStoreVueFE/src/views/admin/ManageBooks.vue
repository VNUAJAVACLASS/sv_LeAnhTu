<script setup>
import { ref, onMounted } from 'vue'
import api from '@/api/axios'

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

const loadBooks = async () => {
  const res = await api.get('/books')
  books.value = res.data
}

onMounted(loadBooks)

const editBook = (book) => {
  form.value = { ...book }
  isEditing.value = true
}

const deleteBook = async (id) => {
  if (!confirm('Bạn có chắc muốn xóa?')) return

  await api.delete(`/books/${id}`)
  loadBooks()
}

const saveBook = async () => {
  if (isEditing.value) {
    await api.put(`/books/${form.value.id}`, form.value)
  } else {
    await api.post('/books', form.value)
  }

  form.value = {
    id: null,
    tenSach: '',
    tacGia: '',
    gia: 0,
    soLuong: 0,
    moTa: ''
  }

  isEditing.value = false
  loadBooks()
}
</script>

<template>
  <div>
    <h3>📚 Quản lý sách</h3>

    <div>
      <input v-model="form.tenSach" placeholder="Tên sách" />
      <input v-model="form.tacGia" placeholder="Tác giả" />
      <input v-model="form.gia" type="number" placeholder="Giá" />
      <input v-model="form.soLuong" type="number" placeholder="Số lượng" />
      <input v-model="form.moTa" placeholder="Mô tả" />

      <button @click="saveBook">
        {{ isEditing ? 'Cập nhật' : 'Thêm mới' }}
      </button>
    </div>

    <table>
      <tr>
        <th>ID</th>
        <th>Tên sách</th>
        <th>Tác giả</th>
        <th>Giá</th>
        <th>Số lượng</th>
        <th>Mô tả</th>
        <th></th>
      </tr>

      <tr v-for="b in books" :key="b.id">
        <td>{{ b.id }}</td>
        <td>{{ b.tenSach }}</td>
        <td>{{ b.tacGia }}</td>
        <td>{{ b.gia }}</td>
        <td>{{ b.soLuong }}</td>
        <td>{{ b.moTa }}</td>

        <td>
          <button @click="editBook(b)">Sửa</button>
          <button @click="deleteBook(b.id)">Xóa</button>
        </td>
      </tr>
    </table>
  </div>
</template>
