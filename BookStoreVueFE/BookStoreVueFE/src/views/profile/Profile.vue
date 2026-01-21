<script setup>
import { ref, onMounted, computed } from 'vue'
import { useAuthStore } from '@/stores/auth.store'
import api from '@/api/axios'

const authStore = useAuthStore()

const userInfo = ref(null)
const processingOrders = ref([])
const orderHistory = ref([])
const selectedOrder = ref(null)
const showDetail = ref(false)
const loading = ref(true)

const userId = computed(() => authStore.userId)

onMounted(async () => {
  await loadUserInfo()
  await loadProcessingOrders()
  await loadOrderHistory()
})

const loadUserInfo = async () => {
  try {
    const res = await api.get('/users/me')
    userInfo.value = res.data
  } catch (error) {
    console.error('Lỗi tải thông tin user:', error)
  }
}

const loadProcessingOrders = async () => {
  try {
    if (!userId.value) return
    const res = await api.get(`/orders/processing/${userId.value}`)
    processingOrders.value = res.data
  } catch (error) {
    console.error('Lỗi tải đơn hàng đang xử lý:', error)
  }
}

const loadOrderHistory = async () => {
  try {
    if (!userId.value) return
    const res = await api.get(`/orders/history/${userId.value}`)
    orderHistory.value = res.data
  } catch (error) {
    console.error('Lỗi tải lịch sử đơn hàng:', error)
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
    console.error('Lỗi tải chi tiết đơn hàng:', error)
  }
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
    1: 'Chờ xác nhận',
    2: 'Đã xác nhận',
    3: 'Đang giao',
    4: 'Đã giao',
    5: 'Đã hủy',
    6: 'Đã trả hàng'
  }
  return statusMap[status] || 'Không rõ'
}

const getStatusClass = (status) => {
  const classMap = {
    1: 'status-pending',
    2: 'status-confirmed',
    3: 'status-shipping',
    4: 'status-delivered',
    5: 'status-cancelled',
    6: 'status-returned'
  }
  return classMap[status] || ''
}
</script>

<template>
  <div class="profile-container">
    <h2>👤 Trang cá nhân</h2>

    <!-- LOADING -->
    <div v-if="loading" class="loading">
      <p>Đang tải dữ liệu...</p>
    </div>

    <div v-else>
      <!-- THÔNG TIN USER -->
      <div class="user-info-card" v-if="userInfo">
        <h3>📋 Thông tin tài khoản</h3>
        <div class="info-grid">
          <div class="info-item">
            <strong>ID:</strong>
            <span>{{ userInfo.id }}</span>
          </div>
          <div class="info-item">
            <strong>Tên đăng nhập:</strong>
            <span>{{ userInfo.username }}</span>
          </div>
          <div class="info-item">
            <strong>Email:</strong>
            <span>{{ userInfo.gmail || 'Chưa cập nhật' }}</span>
          </div>
          <div class="info-item">
            <strong>Số điện thoại:</strong>
            <span>{{ userInfo.soDienThoai || 'Chưa cập nhật' }}</span>
          </div>
          <div class="info-item">
            <strong>Địa chỉ:</strong>
            <span>{{ userInfo.diaChi || 'Chưa cập nhật' }}</span>
          </div>
        </div>
      </div>

      <!-- ĐƠN HÀNG ĐANG XỬ LÝ -->
      <div class="orders-section">
        <h3>📦 Đơn hàng đang xử lý</h3>

        <table v-if="processingOrders.length > 0" class="orders-table">
          <thead>
            <tr>
              <th>Mã đơn</th>
              <th>Ngày đặt</th>
              <th>Tổng tiền</th>
              <th>Trạng thái</th>
              <th>Chi tiết</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="order in processingOrders" :key="order.id">
              <td>#{{ order.id }}</td>
              <td>{{ formatDate(order.ngayDat) }}</td>
              <td class="price">{{ formatPrice(order.tongGiaTien) }}</td>
              <td>
                <span :class="['status-badge', getStatusClass(order.trangThai)]">
                  {{ getStatusText(order.trangThai) }}
                </span>
              </td>
              <td>
                <button @click="viewDetail(order.id)" class="btn-view">
                  Xem
                </button>
              </td>
            </tr>
          </tbody>
        </table>

        <p v-else class="empty-message">Không có đơn hàng nào đang xử lý</p>
      </div>

      <!-- LỊCH SỬ MUA HÀNG -->
      <div class="orders-section">
        <h3>📜 Lịch sử mua hàng</h3>

        <table v-if="orderHistory.length > 0" class="orders-table">
          <thead>
            <tr>
              <th>Mã đơn</th>
              <th>Sách</th>
              <th>Số lượng</th>
              <th>Giá</th>
              <th>Ngày đặt</th>
              <th>Ngày nhận</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="item in orderHistory" :key="item.id">
              <td>#{{ item.id }}</td>
              <td>{{ item.tenSach }}</td>
              <td>{{ item.soLuong }}</td>
              <td class="price">{{ formatPrice(item.priceAtOrder) }}</td>
              <td>{{ formatDate(item.ngayDat) }}</td>
              <td>{{ formatDate(item.ngayNhan) }}</td>
            </tr>
          </tbody>
        </table>

        <p v-else class="empty-message">Chưa có lịch sử mua hàng</p>
      </div>
    </div>

    <!-- MODAL CHI TIẾT ĐƠN HÀNG -->
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
.profile-container {
  max-width: 1200px;
  margin: 20px auto;
  padding: 20px;
}

.profile-container h2 {
  color: #2c3e50;
  margin-bottom: 30px;
}

.loading {
  text-align: center;
  padding: 50px;
  font-size: 18px;
  color: #7f8c8d;
}

/* USER INFO CARD */
.user-info-card {
  background: white;
  padding: 25px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  margin-bottom: 30px;
}

.user-info-card h3 {
  margin-bottom: 20px;
  color: #2c3e50;
  border-bottom: 2px solid #3498db;
  padding-bottom: 10px;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 15px;
}

.info-item {
  display: flex;
  gap: 10px;
}

.info-item strong {
  color: #34495e;
  min-width: 140px;
}

/* ORDERS SECTION */
.orders-section {
  background: white;
  padding: 25px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  margin-bottom: 30px;
}

.orders-section h3 {
  margin-bottom: 20px;
  color: #2c3e50;
  border-bottom: 2px solid #27ae60;
  padding-bottom: 10px;
}

.orders-table {
  width: 100%;
  border-collapse: collapse;
  margin-top: 15px;
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

.status-badge {
  padding: 5px 12px;
  border-radius: 20px;
  font-size: 13px;
  font-weight: 600;
}

.status-pending {
  background: #fff3cd;
  color: #856404;
}

.status-confirmed {
  background: #d1ecf1;
  color: #0c5460;
}

.status-shipping {
  background: #cce5ff;
  color: #004085;
}

.status-delivered {
  background: #d4edda;
  color: #155724;
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
  padding: 6px 16px;
  border-radius: 4px;
  cursor: pointer;
  transition: 0.3s;
}

.btn-view:hover {
  background: #2980b9;
}

.empty-message {
  text-align: center;
  padding: 30px;
  color: #7f8c8d;
  font-size: 16px;
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

/* BOOK IMAGE STYLING */
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