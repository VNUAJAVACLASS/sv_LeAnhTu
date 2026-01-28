<script setup>
import { ref, onMounted, computed, watch } from 'vue'
import api from '@/api/axios'
import { Chart, registerables } from 'chart.js'

Chart.register(...registerables)

// State
const currentYear = new Date().getFullYear()
const currentMonth = new Date().getMonth() + 1 // Tháng hiện tại (1-12)
const selectedYear = ref(currentYear)
const selectedMonth = ref(currentMonth) // MẶC ĐỊNH CHỌN THÁNG HIỆN TẠI(đang bị bug chưa sửa)
const monthlyRevenue = ref([])
const topBooks = ref([])
const summary = ref({})
const loading = ref(true)

// Chart refs
const monthlyChartRef = ref(null)
const topBooksChartRef = ref(null)
let monthlyChart = null
let topBooksChart = null

// Computed
const totalRevenue = computed(() => {
  return monthlyRevenue.value.reduce((sum, item) => sum + item.revenue, 0)
})

const totalOrders = computed(() => {
  return monthlyRevenue.value.reduce((sum, item) => sum + item.totalOrders, 0)
})

// Lifecycle
onMounted(async () => {
  await loadData()
})

watch(selectedYear, () => {
  loadData()
})

watch(selectedMonth, () => {
  loadTopBooks()
})

// Methods
const loadData = async () => {
  loading.value = true
  try {
    await Promise.all([
      loadMonthlyRevenue(),
      loadTopBooks(),
      loadSummary()
    ])
  } catch (error) {
    console.error('Lỗi tải dữ liệu:', error)
  } finally {
    loading.value = false
  }
}

const loadMonthlyRevenue = async () => {
  try {
    const res = await api.get('/revenue/monthly', {
      params: { year: selectedYear.value }
    })
    monthlyRevenue.value = res.data
    updateMonthlyChart()
  } catch (error) {
    console.error('Lỗi tải doanh thu tháng:', error)
  }
}

const loadTopBooks = async () => {
  try {
    const res = await api.get('/revenue/top-books', {
      params: {
        month: selectedMonth.value,
        year: selectedYear.value,
        limit: 10
      }
    })
    topBooks.value = res.data
    updateTopBooksChart()
  } catch (error) {
    console.error('Lỗi tải top sách:', error)
  }
}

const loadSummary = async () => {
  try {
    const res = await api.get('/revenue/summary')
    summary.value = res.data
  } catch (error) {
    console.error('Lỗi tải thống kê:', error)
  }
}

const updateMonthlyChart = () => {
  if (monthlyChart) {
    monthlyChart.destroy()
  }

  const ctx = monthlyChartRef.value?.getContext('2d')
  if (!ctx) return

  monthlyChart = new Chart(ctx, {
    type: 'bar',
    data: {
      labels: monthlyRevenue.value.map(item => `Tháng ${item.month}`),
      datasets: [{
        label: 'Doanh thu (VNĐ)',
        data: monthlyRevenue.value.map(item => item.revenue),
        backgroundColor: 'rgba(52, 152, 219, 0.7)',
        borderColor: 'rgba(52, 152, 219, 1)',
        borderWidth: 2
      }]
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      plugins: {
        legend: {
          display: true,
          position: 'top'
        },
        tooltip: {
          callbacks: {
            label: (context) => {
              return `Doanh thu: ${formatPrice(context.parsed.y)}`
            }
          }
        }
      },
      scales: {
        y: {
          beginAtZero: true,
          ticks: {
            callback: (value) => formatPrice(value)
          }
        }
      }
    }
  })
}

const updateTopBooksChart = () => {
  if (topBooksChart) {
    topBooksChart.destroy()
  }

  const ctx = topBooksChartRef.value?.getContext('2d')
  if (!ctx) return

  topBooksChart = new Chart(ctx, {
    type: 'bar',
    data: {
      labels: topBooks.value.map(book => book.tenSach),
      datasets: [{
        label: 'Số lượng bán',
        data: topBooks.value.map(book => book.totalQuantity),
        backgroundColor: 'rgba(46, 204, 113, 0.7)',
        borderColor: 'rgba(46, 204, 113, 1)',
        borderWidth: 2
      }]
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      indexAxis: 'y',
      plugins: {
        legend: {
          display: true,
          position: 'top'
        }
      },
      scales: {
        x: {
          beginAtZero: true
        }
      }
    }
  })
}

const formatPrice = (price) => {
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND'
  }).format(price)
}

const resetFilter = () => {
  selectedMonth.value = currentMonth 
}
</script>

<template>
  <div class="revenue-container">
    <div class="header">
      <h3>💰 Quản lý Doanh thu</h3>
    </div>

    <div v-if="loading" class="loading">
      <p>Đang tải dữ liệu...</p>
    </div>

    <div v-else>
      <!-- THỐNG KÊ TỔNG QUAN -->
      <div class="summary-cards">
        <div class="summary-card">
          <div class="card-icon">💵</div>
          <div class="card-content">
            <div class="card-label">Tổng doanh thu</div>
            <div class="card-value">{{ formatPrice(summary.totalRevenue) }}</div>
          </div>
        </div>

        <div class="summary-card">
          <div class="card-icon">📦</div>
          <div class="card-content">
            <div class="card-label">Tổng đơn hàng</div>
            <div class="card-value">{{ summary.totalOrders }}</div>
          </div>
        </div>

        <div class="summary-card">
          <div class="card-icon">📚</div>
          <div class="card-content">
            <div class="card-label">Tổng sách bán</div>
            <div class="card-value">{{ summary.totalBooks }}</div>
          </div>
        </div>
      </div>

      <!-- BIỂU ĐỒ DOANH THU THEO THÁNG -->
      <div class="chart-section">
        <div class="section-header">
          <h4>📊 Doanh thu theo tháng</h4>
          <div class="filters">
            <label>Năm:</label>
            <select v-model="selectedYear">
              <option v-for="year in [2023, 2024, 2025, 2026]" :key="year" :value="year">
                {{ year }}
              </option>
            </select>
          </div>
        </div>

        <div class="chart-stats">
          <p><strong>Tổng doanh thu năm {{ selectedYear }}:</strong> {{ formatPrice(totalRevenue) }}</p>
          <p><strong>Tổng đơn hàng:</strong> {{ totalOrders }}</p>
        </div>

        <div class="chart-container">
          <canvas ref="monthlyChartRef"></canvas>
        </div>
      </div>

      <!-- TOP SÁCH BÁN CHẠY -->
      <div class="chart-section">
        <div class="section-header">
          <h4>🏆 Top 10 Sách Bán Chạy</h4>
          <div class="filters">
            <label>Tháng:</label>
            <select v-model="selectedMonth">
              <option :value="null">Cả năm</option>
              <option v-for="month in 12" :key="month" :value="month">
                Tháng {{ month }}
              </option>
            </select>
            <button @click="resetFilter" class="btn-reset">Reset</button>
          </div>
        </div>

        <div class="top-books-section">
          <!-- BIỂU ĐỒ -->
          <div class="chart-container-horizontal">
            <canvas ref="topBooksChartRef"></canvas>
          </div>

          <!-- BẢNG XẾP HẠNG -->
          <div class="ranking-table">
            <h5>📋 Bảng xếp hạng chi tiết</h5>
            <table class="top-books-table">
              <thead>
                <tr>
                  <th>Hạng</th>
                  <th>Tên sách</th>
                  <th>Số lượng</th>
                  <th>Doanh thu</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="(book, index) in topBooks" :key="book.bookId">
                  <td class="rank">
                    <span v-if="index === 0" class="medal gold">🥇</span>
                    <span v-else-if="index === 1" class="medal silver">🥈</span>
                    <span v-else-if="index === 2" class="medal bronze">🥉</span>
                    <span v-else>{{ index + 1 }}</span>
                  </td>
                  <td class="book-name">{{ book.tenSach }}</td>
                  <td class="quantity">{{ book.totalQuantity }}</td>
                  <td class="revenue">{{ formatPrice(book.totalRevenue) }}</td>
                </tr>
              </tbody>
            </table>

            <div v-if="topBooks.length === 0" class="empty-message">
              <p>Không có dữ liệu trong khoảng thời gian này</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.revenue-container {
  padding: 20px;
}

.header {
  margin-bottom: 30px;
}

.header h3 {
  margin: 0;
  color: #2c3e50;
}

.loading {
  text-align: center;
  padding: 60px 20px;
  font-size: 18px;
  color: #7f8c8d;
}

/* SUMMARY CARDS */
.summary-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 20px;
  margin-bottom: 30px;
}

.summary-card {
  background: white;
  padding: 25px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  display: flex;
  align-items: center;
  gap: 20px;
  border-left: 4px solid #3498db;
}

.summary-card:nth-child(2) {
  border-left-color: #27ae60;
}

.summary-card:nth-child(3) {
  border-left-color: #e74c3c;
}

.card-icon {
  font-size: 48px;
}

.card-content {
  flex: 1;
}

.card-label {
  color: #7f8c8d;
  font-size: 14px;
  margin-bottom: 8px;
  text-transform: uppercase;
  letter-spacing: 1px;
}

.card-value {
  font-size: 28px;
  font-weight: bold;
  color: #2c3e50;
}

/* CHART SECTION */
.chart-section {
  background: white;
  padding: 25px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  margin-bottom: 30px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  flex-wrap: wrap;
  gap: 15px;
}

.section-header h4 {
  margin: 0;
  color: #2c3e50;
}

.filters {
  display: flex;
  align-items: center;
  gap: 10px;
}

.filters label {
  font-weight: 600;
  color: #2c3e50;
}

.filters select {
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
}

.btn-reset {
  padding: 8px 16px;
  background: #95a5a6;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-weight: 600;
}

.btn-reset:hover {
  background: #7f8c8d;
}

.chart-stats {
  background: #f8f9fa;
  padding: 15px;
  border-radius: 4px;
  margin-bottom: 20px;
}

.chart-stats p {
  margin: 5px 0;
  color: #2c3e50;
}

.chart-container {
  height: 400px;
  position: relative;
}

.chart-container-horizontal {
  height: 500px;
  position: relative;
  margin-bottom: 30px;
}

/* TOP BOOKS SECTION */
.top-books-section {
  margin-top: 20px;
}

.ranking-table h5 {
  margin-bottom: 15px;
  color: #2c3e50;
}

.top-books-table {
  width: 100%;
  border-collapse: collapse;
}

.top-books-table th,
.top-books-table td {
  padding: 12px;
  text-align: left;
  border-bottom: 1px solid #ecf0f1;
}

.top-books-table th {
  background: #34495e;
  color: white;
  font-weight: 600;
}

.top-books-table tbody tr:hover {
  background: #f8f9fa;
}

.rank {
  text-align: center;
  font-weight: bold;
  font-size: 18px;
}

.medal {
  font-size: 24px;
}

.book-name {
  font-weight: 500;
  color: #2c3e50;
}

.quantity {
  color: #27ae60;
  font-weight: 600;
  text-align: center;
}

.revenue {
  color: #e74c3c;
  font-weight: 600;
  text-align: right;
}

.empty-message {
  text-align: center;
  padding: 40px;
  color: #7f8c8d;
}

@media (max-width: 768px) {
  .summary-cards {
    grid-template-columns: 1fr;
  }

  .section-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .filters {
    width: 100%;
  }

  .filters select {
    flex: 1;
  }
}
</style>