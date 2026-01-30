<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import api from '@/api/axios'
import Swal from 'sweetalert2'
import Pagination from '@/components/common/Pagination.vue'

const orders = ref([])
const searchQuery = ref('')
const selectedOrder = ref(null)
const showDetail = ref(false)

// ===== PHÂN TRANG =====
const currentPage = ref(0)
const pageSize = ref(20)
const totalPages = ref(0)
const totalItems = ref(0)
const hasNext = ref(false)
const hasPrevious = ref(false)

// Tìm kiếm local
const filteredOrders = computed(() => {
  if (!searchQuery.value) return orders.value
  const query = searchQuery.value.toLowerCase()
  return orders.value.filter(order =>
    order.id?.toString().includes(query) ||
    order.user?.username?.toLowerCase().includes(query) ||
    order.soDienThoaiUser?.includes(query)
  )
})

const loadOrders = async (page = 0) => {
  try {
    const res = await api.get('/orders', {
      params: { page, size: pageSize.value }
    })

    const data = res.data
    orders.value = data.content || data
    currentPage.value = data.currentPage || 0
    totalPages.value = data.totalPages || 1
    totalItems.value = data.totalItems || data.length
    hasNext.value = data.hasNext || false
    hasPrevious.value = data.hasPrevious || false
  } catch (error) {
    console.error('Lỗi tải đơn hàng:', error)
  }
}

onMounted(() => loadOrders())

const viewDetail = async (orderId) => {
  try {
    const res = await api.get(`/orders/${orderId}/details`)
    selectedOrder.value = res.data
    showDetail.value = true
  } catch (error) {
    Swal.fire('Lỗi!', 'Không thể tải chi tiết đơn hàng', 'error')
  }
}

const updateStatus = async (order, newStatus) => {
  if (order.trangThai === 4 && newStatus !== 6) {
    Swal.fire({
      icon: 'error',
      title: 'Không được phép!',
      text: 'Đơn hàng đã giao chỉ có thể chuyển sang trạng thái Trả hàng',
      confirmButtonText: 'OK'
    })
    return
  }

  if (newStatus === 6) {
    const result = await Swal.fire({
      icon: 'warning',
      title: 'Xác nhận trả hàng',
      html: `
        <p>Bạn có chắc chắn muốn <strong>TRẢ HÀNG</strong> đơn #${order.id}?</p>
        <p style="color: #e74c3c; font-weight: bold;">
          ⚠️ Hành động này sẽ:
        </p>
        <ul style="text-align: left; color: #555;">
          <li>Xóa đơn hàng khỏi lịch sử giao dịch</li>
          <li>Giảm doanh thu</li>
          <li>Hoàn lại hàng vào kho</li>
        </ul>
      `,
      showCancelButton: true,
      confirmButtonColor: '#e74c3c',
      cancelButtonColor: '#95a5a6',
      confirmButtonText: 'Xác nhận trả hàng',
      cancelButtonText: 'Hủy'
    })

    if (!result.isConfirmed) {
      return
    }
  }

  try {
    await api.patch(`/orders/${order.id}/status`, {
      trangThai: newStatus
    })

    Swal.fire('Thành công!', 'Cập nhật trạng thái thành công', 'success')
    loadOrders(currentPage.value)
  } catch (error) {
    Swal.fire('Lỗi!', error.response?.data || 'Không thể cập nhật trạng thái', 'error')
  }
}

const cancelOrder = async (orderId) => {
  const result = await Swal.fire({
    title: 'Bạn có chắc chắn?',
    text: "Đơn hàng sẽ bị hủy!",
    icon: 'warning',
    showCancelButton: true,
    confirmButtonColor: '#e74c3c',
    cancelButtonColor: '#95a5a6',
    confirmButtonText: 'Hủy đơn',
    cancelButtonText: 'Quay lại'
  })

  if (result.isConfirmed) {
    try {
      await api.delete(`/orders/${orderId}`)
      Swal.fire('Đã hủy!', 'Đơn hàng đã được hủy', 'success')
      loadOrders(currentPage.value)
    } catch (error) {
      Swal.fire('Lỗi!', error.response?.data || 'Không thể hủy đơn hàng', 'error')
    }
  }
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
    1: 'Đang chuẩn bị hàng',
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
  <div class="manage-orders">
    <div class="header">
      <h3>📦 Quản lý Đơn hàng</h3>
      <input
        v-model="searchQuery"
        type="text"
        placeholder="🔍 Tìm kiếm đơn hàng..."
        class="search-input"
      />
    </div>

    <!-- Bảng DANH SÁCH -->
    <div class="table-container">
      <table class="orders-table">
        <thead>
          <tr>
            <th>Mã đơn</th>
            <th>Khách hàng</th>
            <th>SĐT</th>
            <th>Ngày đặt</th>
            <th>Tổng tiền</th>
            <th>Trạng thái</th>
            <th>Thao tác</th>
          </tr>
        </thead>

        <tbody>
          <tr v-for="order in filteredOrders" :key="order.id">
            <td class="order-id">#{{ order.id }}</td>
            <td>{{ order.user?.username || 'N/A' }}</td>
            <td>{{ order.soDienThoaiUser || 'N/A' }}</td>
            <td>{{ formatDate(order.ngayDat) }}</td>
            <td class="price">{{ formatPrice(order.tongGiaTien) }}</td>
            <td>
              <span 
                v-if="order.trangThai === 5 || order.trangThai === 6"
                :class="['status-badge', getStatusClass(order.trangThai)]"
              >
                {{ getStatusText(order.trangThai) }}
              </span>

              <!-- Nếu chưa hủy/trả, hiển thị select như cũ -->
              <select
                v-else
                :value="order.trangThai"
                @change="updateStatus(order, Number($event.target.value))"
                :class="['status-select', getStatusClass(order.trangThai)]"
              >
                <option value="1">Đang chuẩn bị hàng</option>
                <option value="2">Đã xác nhận</option>
                <option value="3">Đang giao</option>
                <option value="4">Đã giao</option>
                <option value="6" v-if="order.trangThai === 4">Đã trả hàng</option>
                <option value="5" v-if="order.trangThai < 4">Đã hủy</option>
              </select>
            </td>
            <td>
              <button @click="viewDetail(order.id)" class="btn-view">
                <i class="fas fa-eye"></i> Xem
              </button>
              <button
                @click="cancelOrder(order.id)"
                class="btn-cancel-order"
                :disabled="order.trangThai >= 4 || order.trangThai === 5 || order.trangThai === 6"
              >
                <i class="fas fa-ban"></i> Hủy
              </button>
            </td>
          </tr>
        </tbody>
      </table>

      <div v-if="filteredOrders.length === 0" class="empty-message">
        <p>{{ searchQuery ? 'Không tìm thấy đơn hàng phù hợp' : 'Chưa có đơn hàng nào' }}</p>
      </div>
    </div>

    <!-- PHÂN TRANG (chỉ hiện khi không search) -->
    <Pagination
      v-if="!searchQuery"
      :current-page="currentPage"
      :total-pages="totalPages"
      :total-items="totalItems"
      :has-next="hasNext"
      :has-previous="hasPrevious"
      item-name="đơn hàng"
      @page-change="loadOrders"
    />

    <!-- MODAL CHI TIẾT ĐƠN HÀNG -->
    <div v-if="showDetail && selectedOrder" class="modal-overlay" @click.self="showDetail = false">
      <div class="modal-content">
        <div class="modal-header">
          <h3>Chi tiết đơn hàng #{{ selectedOrder.orderId }}</h3>
          <button @click="showDetail = false" class="btn-close">✕</button>
        </div>

        <div class="modal-body">
          <div class="order-info">
            <div class="info-row">
              <strong>Khách hàng:</strong>
              <span>{{ selectedOrder.username }}</span>
            </div>
            <div class="info-row">
              <strong>Số điện thoại:</strong>
              <span>{{ selectedOrder.soDienThoai || 'N/A' }}</span>
            </div>
            <div class="info-row">
              <strong>Ngày đặt:</strong>
              <span>{{ formatDate(selectedOrder.ngayDat) }}</span>
            </div>
            <div class="info-row">
              <strong>Trạng thái:</strong>
              <span :class="['status-badge', getStatusClass(selectedOrder.trangThai)]">
                {{ selectedOrder.tenTrangThai }}
              </span>
            </div>
          </div>

          <h4>Danh sách sách</h4>
          <table class="detail-table">
            <thead>
              <tr>
                <th>Tên sách</th>
                <th>Số lượng</th>
                <th>Đơn giá</th>
                <th>Thành tiền</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="book in selectedOrder.books" :key="book.bookId">
                <td class="book-name">{{ book.tenSach }}</td>
                <td>{{ book.soLuong }}</td>
                <td class="price">{{ formatPrice(book.giaTaiThoiDiem) }}</td>
                <td class="price">{{ formatPrice(book.thanhTien) }}</td>
              </tr>
            </tbody>
            <tfoot>
              <tr class="total-row">
                <td colspan="3"><strong>Tổng cộng:</strong></td>
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
.manage-orders {
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
  overflow: hidden;
  box-shadow: 0 2px 5px rgba(0,0,0,0.1);
  margin-bottom: 20px;
}

.orders-table {
  width: 100%;
  border-collapse: collapse;
}

.orders-table th,
.orders-table td {
  padding: 15px;
  text-align: left;
  border-bottom: 1px solid #ddd;
}

.orders-table th {
  background: #34495e;
  color: white;
  font-weight: 600;
}

.orders-table tbody tr:hover {
  background: #f8f9fa;
}

.order-id {
  font-weight: 600;
  color: #2c3e50;
}

.price {
  color: #e74c3c;
  font-weight: 600;
}

/* ✅ Style cho status badge (hiển thị text) */
.status-badge {
   padding: 6px 10px;
  border-radius: 4px; 
  font-size: 13px;
  font-weight: 600;
  display: inline-block;
  border: 2px solid;
  min-width: 150px;  /* Độ rộng tối thiểu giống select */
  text-align: center; /* Căn giữa text */
  cursor: not-allowed; /* Con trỏ chuột báo không sửa được */
  user-select: none;  /* Không cho bôi đen text */
}

.status-select {
  padding: 6px 10px;
  border: 2px solid;
  border-radius: 4px;
  font-weight: 600;
  cursor: pointer;
  min-width: 150px;
}

.status-pending {
  background: #fff3cd;
  color: #856404;
  border-color: #856404;
}

.status-confirmed {
  background: #d1ecf1;
  color: #0c5460;
  border-color: #0c5460;
}

.status-shipping {
  background: #cce5ff;
  color: #004085;
  border-color: #004085;
}

.status-delivered {
  background: #d4edda;
  color: #155724;
  border-color: #155724;
}

.status-cancelled {
  background: #f8d7da;
  color: #721c24;
  border-color: #721c24;
}

.status-returned {
  background: #e2e3e5;
  color: #383d41;
  border-color: #383d41;
}

.btn-view, .btn-cancel-order {
  padding: 8px 12px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  margin-right: 5px;
  font-size: 13px;
}

.btn-view {
  background: #3498db;
  color: white;
}

.btn-view:hover {
  background: #2980b9;
}

.btn-cancel-order {
  background: #e74c3c;
  color: white;
}

.btn-cancel-order:hover:not(:disabled) {
  background: #c0392b;
}

.btn-cancel-order:disabled {
  background: #95a5a6;
  cursor: not-allowed;
}

.empty-message {
  padding: 50px;
  text-align: center;
  color: #7f8c8d;
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
  max-width: 800px;
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
  background: #34495e;
  color: white;
  border-radius: 8px 8px 0 0;
}

.modal-header h3 {
  margin: 0;
}

.btn-close {
  background: none;
  border: none;
  font-size: 24px;
  cursor: pointer;
  color: white;
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

.info-row {
  display: flex;
  justify-content: space-between;
  padding: 8px 0;
  border-bottom: 1px solid #dee2e6;
}

.info-row:last-child {
  border-bottom: none;
}

.info-row strong {
  color: #2c3e50;
}

.modal-body h4 {
  margin: 20px 0 10px 0;
  color: #2c3e50;
}

.detail-table {
  width: 100%;
  border-collapse: collapse;
}

.detail-table th,
.detail-table td {
  padding: 12px;
  text-align: left;
  border: 1px solid #ecf0f1;
}

.detail-table th {
  background: #34495e;
  color: white;
}

.detail-table tbody tr:hover {
  background: #f8f9fa;
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

@media (max-width: 768px) {
  .header {
    flex-direction: column;
    align-items: stretch;
  }

  .search-input {
    width: 100%;
  }
}
</style>