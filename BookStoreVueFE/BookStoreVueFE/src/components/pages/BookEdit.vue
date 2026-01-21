<template>
  <h2 class="text-center mt-5 mb-3">Edit Book</h2>
  <div class="card">
    <div class="card-header">
      <router-link class="btn btn-outline-info float-right" to="/dashboard">
        View All Books
      </router-link>
    </div>
    <div class="card-body">
      <form>
        <div class="form-group">
          <label htmlFor="tenSach">Tên Sách</label>
          <input
            v-model="book.tenSach"
            type="text"
            class="form-control"
            id="tenSach"
            name="tenSach"
          />
        </div>
        <div class="form-group">
          <label htmlFor="tacGia">Tác Giả</label>
          <input
            v-model="book.tacGia"
            type="text"
            class="form-control"
            id="tacGia"
            name="tacGia"
          />
        </div>
        <div class="form-group">
          <label htmlFor="gia">Giá</label>
          <input
            v-model="book.gia"
            type="number"
            class="form-control"
            id="gia"
            name="gia"
          />
        </div>
        <div class="form-group">
          <label htmlFor="soLuong">Số Lượng</label>
          <input
            v-model="book.soLuong"
            type="number"
            class="form-control"
            id="soLuong"
            name="soLuong"
          />
        </div>
        <div class="form-group">
          <label htmlFor="moTa">Mô Tả</label>
          <textarea
            v-model="book.moTa"
            class="form-control"
            id="moTa"
            rows="3"
            name="moTa"
          ></textarea>
        </div>
        <button
          @click="handleSave()"
          :disabled="isSaving"
          type="button"
          class="btn btn-outline-primary mt-3"
        >
          Update Book
        </button>
      </form>
    </div>
  </div>
</template>

<script>
import axios from 'axios'
import Swal from 'sweetalert2'

export default {
  name: 'BookEdit',
  data() {
    return {
      book: {
        tenSach: '',
        tacGia: '',
        gia: 0,
        soLuong: 0,
        moTa: ''
      },
      isSaving: false
    }
  },
  created() {
    const id = this.$route.params.id
    axios
      .get(`/api/books/${id}`)
      .then(response => {
        let bookInfo = response.data
        this.book = {
          tenSach: bookInfo.tenSach,
          tacGia: bookInfo.tacGia,
          gia: bookInfo.gia,
          soLuong: bookInfo.soLuong,
          moTa: bookInfo.moTa
        }
        return response
      })
      .catch(error => {
        Swal.fire({
          icon: 'error',
          title: 'An Error Occured!',
          showConfirmButton: false,
          timer: 1500
        })
        return error
      })
  },
  methods: {
    handleSave() {
      this.isSaving = true
      const id = this.$route.params.id
      axios
        .patch(`/api/books/${id}`, this.book, {
          headers: {
            Authorization: 'Bearer ' + localStorage.getItem('token')
          }
        })
        .then(response => {
          Swal.fire({
            icon: 'success',
            title: 'Book updated successfully!',
            showConfirmButton: false,
            timer: 1500
          })
          this.isSaving = false
          this.$router.push('/dashboard')
          return response
        })
        .catch(error => {
          this.isSaving = false
          Swal.fire({
            icon: 'error',
            title: 'An Error Occured!',
            showConfirmButton: false,
            timer: 1500
          })
          return error
        })
    }
  }
}
</script>