<script setup>
import { ref, onMounted } from 'vue'
import api from '@/api/axios'

const orders = ref([])

const selectedOrder = ref(null)

const loadOrders = async () => {
  const res = await api.get('/orders')
  orders.value = res.data
}

onMounted(loadOrders)

const viewDetail = (order) => {
  selectedOrder.value = order
}

const updateStatus = async (order) => {
  await api.put(`/orders/${order.id}/status`, {
    status: order.status
  })

  alert("Cập nhật trạng thái thành công")
  loadOrders()
}
</script>

<template>
  <div>
    <h3>📦 Quản lý đơn hàng</h3>

    <table>
      <tr>
        <th>ID</th>
        <th>User</th>
        <th>Tổng tiền</th>
        <th>Trạng thái</th>
        <th>Ngày đặt</th>
        <th></th>
      </tr>

      <tr v-for="o in orders" :key="o.id">
        <td>{{ o.id }}</td>
        <td>{{ o.username }}</td>
        <td>{{ o.total }}</td>

        <td>
          <select v-model="o.status" @change="updateStatus(o)">
            <option value="1">Chờ xác nhận</option>
            <option value="2">Đã xác nhận</option>
            <option value="3">Đang giao</option>
            <option value="4">Đã giao</option>
            <option value="5">Hoàn thành</option>
            <option value="6">Hủy</option>
          </select>
        </td>

        <td>{{ o.createdAt }}</td>

        <td>
          <button @click="viewDetail(o)">
            Xem chi tiết
          </button>
        </td>
      </tr>
    </table>

    <div v-if="selectedOrder">
      <h4>Chi tiết đơn #{{ selectedOrder.id }}</h4>

      <table>
        <tr>
          <th>Sách</th>
          <th>Số lượng</th>
          <th>Giá</th>
        </tr>

        <tr v-for="d in selectedOrder.items" :key="d.id">
          <td>{{ d.tenSach }}</td>
          <td>{{ d.soLuong }}</td>
          <td>{{ d.gia }}</td>
        </tr>
      </table>
    </div>
  </div>
</template>
