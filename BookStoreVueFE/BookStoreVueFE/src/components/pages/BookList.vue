<template>
  <div class="container">
    <h2 class="text-center mt-5 mb-3">Book Manager</h2>
    <div class="card">
      <div class="card-header">
        <router-link to="/books/create" class="btn btn-outline-primary">
          Create New Book
        </router-link>
      </div>
      <div class="card-body">
        <table class="table table-bordered">
          <thead>
            <tr>
              <th>Tên Sách</th>
              <th>Tác Giả</th>
              <th>Giá</th>
              <th>Số Lượng</th>
              <th width="240px">Action</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="book in books" :key="book.id">
              <td>{{ book.tenSach }}</td>
              <td>{{ book.tacGia }}</td>
              <td>{{ formatPrice(book.gia) }}</td>
              <td>{{ book.soLuong }}</td>
              <td>
                <router-link :to="`/books/${book.id}`" class="btn btn-outline-info mx-1">
                  Show
                </router-link>
                <router-link v-if="isAdmin" :to="`/books/${book.id}/edit`" class="btn btn-outline-success mx-1">
                  Edit
                </router-link>
                <button v-if="isAdmin" @click="handleDelete(book.id)" class="btn btn-outline-danger mx-1">
                  Delete
                </button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios'
import Swal from 'sweetalert2'

export default {
  name: 'BookList',
  data() {
    return {
      books: []
    }
  },
  created() {
    this.fetchBookList()
  },
  computed: {
    isAdmin() {
      const roles = JSON.parse(localStorage.getItem('roles') || '[]')
      return roles.includes('ROLE_ADMIN') || roles.includes('ROLE_SUPER_ADMIN')
    }
  },
  methods: {
    fetchBookList() {
      axios.get('/api/books')
        .then(response => {
          this.books = response.data
        })
        .catch(error => {
          console.error(error)
        })
    },
    formatPrice(price) {
      return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(price)
    },
    handleDelete(id) {
      Swal.fire({
        title: 'Are you sure?',
        text: "You won't be able to revert this!",
        icon: 'warning',
        showCancelButton: true,
        confirmButtonColor: '#3085d6',
        cancelButtonColor: '#d33',
        confirmButtonText: 'Yes, delete it!'
      }).then((result) => {
        if (result.isConfirmed) {
          axios.delete(`/api/books/${id}`, {
            headers: {
              Authorization: 'Bearer ' + localStorage.getItem('token')
            }
          })
          .then(() => {
            Swal.fire({
              icon: 'success',
              title: 'Book deleted successfully!',
              showConfirmButton: false,
              timer: 1500
            })
            this.fetchBookList()
          })
          .catch(error => {
            Swal.fire({
              icon: 'error',
              title: 'An Error Occurred!',
              showConfirmButton: false,
              timer: 1500
            })
          })
        }
      })
    }
  }
}
</script>