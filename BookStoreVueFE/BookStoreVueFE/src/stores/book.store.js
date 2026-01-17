import { defineStore } from 'pinia'
import { BookApi } from '@/api/book.api'
import { Book } from '@/models/Book'


export const useBookStore = defineStore('book', {
state: () => ({
books: [],
totalPages: 0,
}),
actions: {
async fetchBooks(page = 0) {
  const res = await BookApi.getAll(page)

  const list = res.data.content ?? res.data

  this.books = list.map(b => new Book(b))
  this.totalPages = res.data.totalPages ?? 1
}
}
})