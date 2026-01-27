<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth.store'
import api from '@/api/axios'

const router = useRouter()
const authStore = useAuthStore()

const allOrders = ref([])
const loading = ref(true)
const selectedOrder = ref(null)
const showDetail = ref(false)

const userId = computed(() => authStore.userId)

onMounted(async () => {
  await loadCancelledOrders()
})

const loadCancelledOrders = async () => {
  try {
    if (!userId.value) return
    
    // Lấy tất cả đơn hàng của user
    const res = await api.get(`/orders/user/${userId.value}`)
    
    // Lọc chỉ lấy đơn đã hủy (5) và đã trả hàng (6)
    allOrders.value = res.data.filter(order => {
      return order.trangThai === 5 || order.trangThai === 6
    })
  } catch (error) {
    console.error('Lỗi tải đơn hàng:', error)
  } finally {
    loading.value = false
  }
}

const viewDetail = async (orderId) => {
  try {
    const res = await api.get(`/orders/${orderId}/details`)
    selectedOrder.value = res.data
    showDetail.value = true
  } catch (error) {
    console.error('Lỗi tải chi tiết:', error)
  }
}

const goBack = () => {
  router.push('/profile')
}

const handleImageError = (e) => {
  e.target.style.display = 'none'
  e.target.parentElement.innerHTML = '<div class="book-icon">📖</div>'
}

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

const getStatusText = (status) => {
  const statusMap = {
    5: 'Đã hủy',
    6: 'Đã trả hàng'
  }
  return statusMap[status] || 'Không rõ'
}

const getStatusClass = (status) => {
  const classMap = {
    5: 'status-cancelled',
    6: 'status-returned'
  }
  return classMap[status] || ''
}
</script>

<template>
  <div class="cancelled-orders-container">
    <div class="header">
      <h2>📋 Đơn hàng đã hủy/trả hàng</h2>
      <button @click="goBack" class="btn-back">
        <i class="fas fa-arrow-left"></i> Quay lại
      </button>
    </div>

    <div v-if="loading" class="loading">
      <p>Đang tải dữ liệu...</p>
    </div>

    <div v-else class="orders-section">
      <p class="section-note">* Danh sách các đơn hàng đã bị hủy hoặc đã trả hàng</p>

      <table v-if="allOrders.length > 0" class="orders-table">
        <thead>
          <tr>
            <th>Mã đơn</th>
            <th>Ngày đặt</th>
            <th>Tổng tiền</th>
            <th>Trạng thái</th>
            <th>Lý do</th>
            <th>Chi tiết</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="order in allOrders" :key="order.id">
            <td>#{{ order.id }}</td>
            <td>{{ formatDate(order.ngayDat) }}</td>
            <td class="price">{{ formatPrice(order.tongGiaTien) }}</td>
            <td>
              <span :class="['status-badge', getStatusClass(order.trangThai)]">
                {{ getStatusText(order.trangThai) }}
              </span>
            </td>
            <td class="reason">
              <span v-if="order.trangThai === 5">
                Đơn hàng bị hủy bởi quản trị viên
              </span>
              <span v-else-if="order.trangThai === 6">
                Sản phẩm bị trả lại
              </span>
            </td>
            <td>
              <button @click="viewDetail(order.id)" class="btn-view">
                <i class="fas fa-eye"></i> Xem
              </button>
            </td>
          </tr>
        </tbody>
      </table>

      <div v-else class="empty-message">
        <i class="fas fa-inbox" style="font-size: 60px; color: #95a5a6; margin-bottom: 20px;"></i>
        <p>Không có đơn hàng nào bị hủy hoặc trả hàng</p>
        <button @click="goBack" class="btn-go-back">Quay về trang cá nhân</button>
      </div>
    </div>

    <!-- MODAL CHI TIẾT -->
    <div v-if="showDetail && selectedOrder" class="modal-overlay" @click.self="showDetail = false">
      <div class="modal-content">
        <div class="modal-header">
          <h3>Chi tiết đơn hàng #{{ selectedOrder.orderId }}</h3>
          <button @click="showDetail = false" class="btn-close">✕</button>
        </div>

        <div class="modal-body">
          <div class="order-info">
            <p><strong>Ngày đặt:</strong> {{ formatDate(selectedOrder.ngayDat) }}</p>
            <p><strong>Trạng thái:</strong>
              <span :class="['status-badge', getStatusClass(selectedOrder.trangThai)]">
                {{ selectedOrder.tenTrangThai }}
              </span>
            </p>
            <p><strong>Số điện thoại:</strong> {{ selectedOrder.soDienThoai || 'N/A' }}</p>
          </div>

          <h4>Danh sách sách</h4>
          <table class="detail-table">
            <thead>
              <tr>
                <th>Ảnh</th>
                <th>Tên sách</th>
                <th>Số lượng</th>
                <th>Đơn giá</th>
                <th>Thành tiền</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="book in selectedOrder.books" :key="book.bookId">
                <td>
                  <div class="book-image">
                    <img
                      v-if="book.imagePath"
                      :src="`http://localhost:8080${book.imagePath}`"
                      :alt="book.tenSach"
                      @error="handleImageError"
                    />
                    <div v-else class="book-icon">📖</div>
                  </div>
                </td>
                <td class="book-name">{{ book.tenSach }}</td>
                <td>{{ book.soLuong }}</td>
                <td class="price">{{ formatPrice(book.giaTaiThoiDiem) }}</td>
                <td class="price">{{ formatPrice(book.thanhTien) }}</td>
              </tr>
            </tbody>
            <tfoot>
              <tr class="total-row">
                <td colspan="4"><strong>Tổng cộng:</strong></td>
                <td class="price"><strong>{{ formatPrice(selectedOrder.tongTien) }}</strong></td>
              </tr>
            </tfoot>
          </table>
        </div>

        <div class="modal-footer">
          <button @click="showDetail = false" class="btn-ok">Đóng</button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.cancelled-orders-container {
  max-width: 1200px;
  margin: 20px auto;
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

.header h2 {
  color: #2c3e50;
  margin: 0;
}

.btn-back {
  padding: 10px 20px;
  background: #95a5a6;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-weight: 600;
  transition: 0.3s;
  display: flex;
  align-items: center;
  gap: 8px;
}

.btn-back:hover {
  background: #7f8c8d;
}

.loading {
  text-align: center;
  padding: 50px;
  font-size: 18px;
  color: #7f8c8d;
}

.orders-section {
  background: white;
  padding: 25px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.section-note {
  font-size: 13px;
  color: #7f8c8d;
  font-style: italic;
  margin-bottom: 20px;
}

.orders-table {
  width: 100%;
  border-collapse: collapse;
}

.orders-table th,
.orders-table td {
  padding: 12px;
  text-align: left;
  border-bottom: 1px solid #ecf0f1;
}

.orders-table th {
  background: #34495e;
  color: white;
  font-weight: 600;
}

.orders-table tbody tr:hover {
  background: #f8f9fa;
}

.price {
  color: #e74c3c;
  font-weight: 600;
}

.reason {
  color: #7f8c8d;
  font-size: 14px;
  font-style: italic;
}

.status-badge {
  padding: 5px 12px;
  border-radius: 20px;
  font-size: 13px;
  font-weight: 600;
}

.status-cancelled {
  background: #f8d7da;
  color: #721c24;
}

.status-returned {
  background: #e2e3e5;
  color: #383d41;
}

.btn-view {
  background: #3498db;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 4px;
  cursor: pointer;
  transition: 0.3s;
  display: flex;
  align-items: center;
  gap: 5px;
}

.btn-view:hover {
  background: #2980b9;
}

.empty-message {
  text-align: center;
  padding: 60px 20px;
  color: #7f8c8d;
}

.empty-message p {
  font-size: 18px;
  margin-bottom: 20px;
}

.btn-go-back {
  padding: 12px 24px;
  background: #3498db;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-weight: 600;
  transition: 0.3s;
}

.btn-go-back:hover {
  background: #2980b9;
}

/* MODAL */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0,0,0,0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  background: white;
  border-radius: 8px;
  max-width: 900px;
  width: 90%;
  max-height: 90vh;
  overflow-y: auto;
}

.modal-header {
  padding: 20px;
  border-bottom: 1px solid #ecf0f1;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.modal-header h3 {
  margin: 0;
  color: #2c3e50;
}

.btn-close {
  background: none;
  border: none;
  font-size: 24px;
  cursor: pointer;
  color: #95a5a6;
}

.btn-close:hover {
  color: #e74c3c;
}

.modal-body {
  padding: 20px;
}

.order-info {
  background: #f8f9fa;
  padding: 15px;
  border-radius: 4px;
  margin-bottom: 20px;
}

.order-info p {
  margin: 8px 0;
}

.modal-body h4 {
  margin: 20px 0 10px 0;
  color: #2c3e50;
}

.detail-table {
  width: 100%;
  border-collapse: collapse;
  margin-top: 10px;
}

.detail-table th,
.detail-table td {
  padding: 12px;
  text-align: left;
  border: 1px solid #ecf0f1;
  vertical-align: middle;
}

.detail-table th {
  background: #34495e;
  color: white;
}

.detail-table tbody tr:hover {
  background: #f8f9fa;
}

.book-image {
  width: 60px;
  height: 80px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto;
}

.book-image img {
  max-width: 100%;
  max-height: 100%;
  object-fit: cover;
  border-radius: 4px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.book-icon {
  font-size: 40px;
}

.book-name {
  font-weight: 500;
  color: #2c3e50;
}

.total-row {
  background: #f8f9fa;
}

.total-row td {
  border-top: 2px solid #2c3e50;
  padding: 15px 12px;
  font-size: 16px;
}

.modal-footer {
  padding: 15px 20px;
  border-top: 1px solid #ecf0f1;
  text-align: right;
}

.btn-ok {
  background: #3498db;
  color: white;
  border: none;
  padding: 10px 24px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 15px;
}

.btn-ok:hover {
  background: #2980b9;
}
</style>