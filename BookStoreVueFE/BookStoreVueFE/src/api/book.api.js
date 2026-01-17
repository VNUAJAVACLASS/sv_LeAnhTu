import api from './axios'


export const BookApi = {
getAll(page = 0, size = 8) {
return api.get(`/books?page=${page}&size=${size}`)
},
getById(id) {
return api.get(`/books/${id}`)
}
}