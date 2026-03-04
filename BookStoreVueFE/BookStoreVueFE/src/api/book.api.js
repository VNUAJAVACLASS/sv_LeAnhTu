import api from './axios'

export const BookApi = {
  getAll(page = 0, size = 8, sort = 'all', danhMucId = null) {
    const params = { page, size, sort }
    if (danhMucId) params.danhMucId = danhMucId
    return api.get('/books', { params })
  },
  getById(id) {
    return api.get(`/books/${id}`)
  }
}

export const DanhMucApi = {
  getAll() {
    return api.get('/danh-muc')
  },
  create(data) {
    return api.post('/danh-muc', data)
  },
  update(id, data) {
    return api.patch(`/danh-muc/${id}`, data)
  },
  delete(id) {
    return api.delete(`/danh-muc/${id}`)
  }
}