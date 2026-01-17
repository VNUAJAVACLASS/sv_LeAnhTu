<script setup>
import { ref } from 'vue'

import ManageBooks from './ManageBooks.vue'
import ManageUsers from './ManageUsers.vue'
import ManageOrders from './ManageOrders.vue'

const activeTab = ref('books')
</script>

<template>
  <div class="dashboard-container">

    <!-- SIDEBAR -->
    <div class="sidebar">
      <h3>Admin Panel</h3>

      <button @click="activeTab = 'books'">
        📚 Quản lý sách
      </button>

      <button @click="activeTab = 'users'">
        👤 Quản lý user
      </button>

      <button @click="activeTab = 'orders'">
        📦 Đơn hàng
      </button>

      <button @click="activeTab = 'history'">
        📜 Lịch sử giao dịch
      </button>
    </div>

    <!-- MAIN CONTENT -->
    <div class="content">
      <ManageBooks v-if="activeTab === 'books'" />
      <ManageUsers v-if="activeTab === 'users'" />
      <ManageOrders v-if="activeTab === 'orders'" />

      <div v-if="activeTab === 'history'">
        <h3>Lịch sử giao dịch</h3>
        (Sẽ tích hợp sau khi có API)
      </div>
    </div>

  </div>
</template>

<style scoped>
.dashboard-container {
  display: flex;
  min-height: 80vh;
}

/* SIDEBAR */
.sidebar {
  width: 25%;
  border-right: 1px solid #ccc;
  padding: 15px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.sidebar button {
  padding: 10px;
  text-align: left;
}

/* CONTENT */
.content {
  width: 75%;
  padding: 20px;
}
</style>
