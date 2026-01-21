<template>
  <div class="container">
    <h2 class="text-center mt-5 mb-3">BookStore - Client View</h2>
    <div class="card">
      <div class="card-header">
        <h4>Book List</h4>
      </div>
      <div class="card-body">
        <table class="table table-bordered">
          <thead>
            <tr>
              <th>Tên Sách</th>
              <th>Tác Giả</th>
              <th>Giá</th>
              <th width="240px">Action</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="book in books" :key="book.id">
              <td>{{ book.tenSach }}</td>
              <td>{{ book.tacGia }}</td>
              <td>{{ formatPrice(book.gia) }}</td>
              <td>
                <router-link
                  :to="`/books/${book.id}`"
                  class="btn btn-outline-info mx-1"
                >
                  Show
                </router-link>
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

export default {
  name: 'ClientPage',
  data() {
    return {
      books: []
    }
  },
  created() {
    this.fetchBookList()
  },
  methods: {
    fetchBookList() {
      axios
        .get('/api/books')
        .then(response => {
          this.books = response.data
          return response
        })
        .catch(error => {
          return error
        })
    },
    formatPrice(price) {
      return new Intl.NumberFormat('vi-VN', {
        style: 'currency',
        currency: 'VND'
      }).format(price)
    }
  }
}
</script>