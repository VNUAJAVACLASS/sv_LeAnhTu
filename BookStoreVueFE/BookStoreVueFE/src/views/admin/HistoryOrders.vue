<script setup>
import { ref, onMounted, computed } from 'vue'
import api from '@/api/axios'

const histories = ref([])
const searchQuery = ref('')

// Tìm kiếm
const filteredHistories = computed(() => {
  if (!searchQuery.value) return histories.value
  const query = searchQuery.value.toLowerCase()
  return histories.value.filter(item => 
    item.id?.toString().includes(query) ||
    item.username?.toLowerCase().includes(query) ||
    item.tenSach?.toLowerCase().includes(query) ||
    item.phone?.includes(query)
  )
})

const loadHistories = async () => {
  try {
    const res = await api.get('/orders/history')
    histories.value = res.data
  } catch (error) {
    console.error('Lỗi tải lịch sử:', error)
  }
}

onMounted(loadHistories)

const formatDate = (date) => {
  if (!date) return 'N/A'
  return new Date(date).toLocaleDateString('vi-VN')
}

const formatPrice = (price) => {
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND'
  }).format(price)
}

const calculateTotal = (item) => {
  return item.soLuong * item.priceAtOrder
}
</script>

<template>
  <div class="history-orders">
    <div class="header">
      <h3>📜 Lịch sử giao dịch</h3>
      <input 
        v-model="searchQuery" 
        type="text" 
        placeholder="🔍 Tìm kiếm lịch sử..."
        class="search-input"
      />
    </div>

    <!-- BẢNG LỊCH SỬ -->
    <div class="table-container">
      <table class="history-table">
        <thead>
          <tr>
            <th>ID</th>
            <th>Khách hàng</th>
            <th>Gmail</th>
            <th>SĐT</th>
            <th>Tên sách</th>
            <th>Số lượng</th>
            <th>Đơn giá</th>
            <th>Thành tiền</th>
            <th>Ngày đặt</th>
            <th>Ngày nhận</th>
          </tr>
        </thead>

        <tbody>
          <tr v-for="item in filteredHistories" :key="item.id">
            <td class="history-id">{{ item.id }}</td>
            <td class="username">{{ item.username || 'N/A' }}</td>
            <td>{{ item.gmail || 'N/A' }}</td>
            <td>{{ item.phone || 'N/A' }}</td>
            <td class="book-name">{{ item.tenSach }}</td>
            <td class="quantity">{{ item.soLuong }}</td>
            <td class="price">{{ formatPrice(item.priceAtOrder) }}</td>
            <td class="total-price">{{ formatPrice(calculateTotal(item)) }}</td>
            <td>{{ formatDate(item.ngayDat) }}</td>
            <td>{{ formatDate(item.ngayNhan) }}</td>
          </tr>
        </tbody>
      </table>

      <div v-if="filteredHistories.length === 0" class="empty-message">
        <p>{{ searchQuery ? 'Không tìm thấy lịch sử phù hợp' : 'Chưa có lịch sử giao dịch nào' }}</p>
      </div>
    </div>

    <!-- THỐNG KÊ -->
    <div v-if="histories.length > 0" class="statistics">
      <div class="stat-card">
        <div class="stat-label">Tổng giao dịch</div>
        <div class="stat-value">{{ histories.length }}</div>
      </div>
      <div class="stat-card">
        <div class="stat-label">Tổng sách đã bán</div>
        <div class="stat-value">
          {{ histories.reduce((sum, item) => sum + item.soLuong, 0) }}
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-label">Tổng doanh thu</div>
        <div class="stat-value price">
          {{ formatPrice(histories.reduce((sum, item) => sum + calculateTotal(item), 0)) }}
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.history-orders {
  padding: 20px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
  flex-wrap: wrap;
  gap: 15px;
}

.header h3 {
  margin: 0;
  color: #2c3e50;
}

.search-input {
  padding: 10px 15px;
  border: 1px solid #ddd;
  border-radius: 4px;
  width: 300px;
  font-size: 14px;
}

.search-input:focus {
  outline: none;
  border-color: #3498db;
}

.table-container {
  background: white;
  border-radius: 8px;
  overflow-x: auto;
  box-shadow: 0 2px 5px rgba(0,0,0,0.1);
  margin-bottom: 30px;
}

.history-table {
  width: 100%;
  border-collapse: collapse;
  min-width: 1000px;
}

.history-table th,
.history-table td {
  padding: 15px;
  text-align: left;
  border-bottom: 1px solid #ddd;
}

.history-table th {
  background: #34495e;
  color: white;
  font-weight: 600;
  position: sticky;
  top: 0;
  z-index: 10;
}

.history-table tbody tr:hover {
  background: #f8f9fa;
}

.history-id {
  font-weight: 600;
  color: #7f8c8d;
}

.username {
  font-weight: 600;
  color: #2c3e50;
}

.book-name {
  font-weight: 500;
  color: #34495e;
  max-width: 200px;
}

.quantity {
  text-align: center;
  font-weight: 600;
  color: #27ae60;
}

.price {
  color: #e74c3c;
  font-weight: 600;
}

.total-price {
  color: #c0392b;
  font-weight: 700;
  font-size: 15px;
}

.empty-message {
  padding: 50px;
  text-align: center;
  color: #7f8c8d;
}

/* THỐNG KÊ */
.statistics {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
}

.stat-card {
  background: white;
  padding: 25px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  text-align: center;
  border-left: 4px solid #3498db;
}

.stat-card:nth-child(2) {
  border-left-color: #27ae60;
}

.stat-card:nth-child(3) {
  border-left-color: #e74c3c;
}

.stat-label {
  color: #7f8c8d;
  font-size: 14px;
  margin-bottom: 10px;
  text-transform: uppercase;
  letter-spacing: 1px;
}

.stat-value {
  font-size: 32px;
  font-weight: bold;
  color: #2c3e50;
}

.stat-value.price {
  color: #e74c3c;
  font-size: 24px;
}

@media (max-width: 768px) {
  .search-input {
    width: 100%;
  }
  
  .statistics {
    grid-template-columns: 1fr;
  }
}
</style>