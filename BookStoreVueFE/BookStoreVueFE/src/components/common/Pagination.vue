<template>
  <div v-if="totalPages > 1" class="pagination-container">
    <div class="pagination">
      <!-- Nút Previous -->
      <button
        @click="$emit('page-change', currentPage - 1)"
        :disabled="!hasPrevious"
        class="page-btn"
        :class="{ disabled: !hasPrevious }"
      >
        <i class="fas fa-chevron-left"></i> Trước
      </button>

      <!-- Trang đầu -->
      <button
        v-if="pageNumbers[0] > 0"
        @click="$emit('page-change', 0)"
        class="page-number"
      >
        1
      </button>

      <!-- Dấu ... -->
      <span v-if="pageNumbers[0] > 1" class="page-dots">...</span>

      <!-- Các số trang -->
      <button
        v-for="page in pageNumbers"
        :key="page"
        @click="$emit('page-change', page)"
        class="page-number"
        :class="{ active: page === currentPage }"
      >
        {{ page + 1 }}
      </button>

      <!-- Dấu ... -->
      <span v-if="pageNumbers[pageNumbers.length - 1] < totalPages - 2" class="page-dots">...</span>

      <!-- Trang cuối -->
      <button
        v-if="pageNumbers[pageNumbers.length - 1] < totalPages - 1"
        @click="$emit('page-change', totalPages - 1)"
        class="page-number"
      >
        {{ totalPages }}
      </button>

      <!-- Nút Next -->
      <button
        @click="$emit('page-change', currentPage + 1)"
        :disabled="!hasNext"
        class="page-btn"
        :class="{ disabled: !hasNext }"
      >
        Sau <i class="fas fa-chevron-right"></i>
      </button>
    </div>

    <!-- Thông tin phân trang -->
    <div class="pagination-info">
      Trang <strong>{{ currentPage + 1 }}</strong> / <strong>{{ totalPages }}</strong>
      (Tổng: <strong>{{ totalItems }}</strong> {{ itemName }})
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  currentPage: { type: Number, required: true },
  totalPages: { type: Number, required: true },
  totalItems: { type: Number, required: true },
  hasNext: { type: Boolean, required: true },
  hasPrevious: { type: Boolean, required: true },
  itemName: { type: String, default: 'mục' }
})

defineEmits(['page-change'])

const pageNumbers = computed(() => {
  const pages = []
  const maxVisible = 5

  let start = Math.max(0, props.currentPage - 2)
  let end = Math.min(props.totalPages - 1, start + maxVisible - 1)

  if (end - start < maxVisible - 1) {
    start = Math.max(0, end - maxVisible + 1)
  }

  for (let i = start; i <= end; i++) {
    pages.push(i)
  }

  return pages
})
</script>

<style scoped>
.pagination-container {
  margin: 30px 0;
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.page-btn,
.page-number {
  padding: 10px 16px;
  border: 1px solid #ddd;
  background: white;
  color: #2c3e50;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s;
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 5px;
}

.page-btn:hover:not(.disabled),
.page-number:hover:not(.active) {
  background: #3498db;
  color: white;
  border-color: #3498db;
}

.page-number.active {
  background: #3498db;
  color: white;
  border-color: #3498db;
  font-weight: bold;
  cursor: default;
}

.page-btn.disabled {
  background: #ecf0f1;
  color: #95a5a6;
  cursor: not-allowed;
  border-color: #ecf0f1;
}

.page-dots {
  padding: 0 8px;
  color: #7f8c8d;
  font-weight: bold;
}

.pagination-info {
  text-align: center;
  color: #7f8c8d;
  font-size: 14px;
  padding: 15px;
  background: #f8f9fa;
  border-radius: 4px;
  margin-top: 15px;
}

.pagination-info strong {
  color: #2c3e50;
  font-size: 16px;
}

@media (max-width: 768px) {
  .pagination {
    gap: 5px;
  }

  .page-btn,
  .page-number {
    padding: 8px 12px;
    font-size: 14px;
  }
}
</style>