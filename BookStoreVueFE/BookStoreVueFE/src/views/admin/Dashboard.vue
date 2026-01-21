<script setup>
import { ref } from 'vue'
import ManageBooks from './ManageBooks.vue'
import ManageUsers from './ManageUsers.vue'
import ManageOrders from './ManageOrders.vue'

const activeTab = ref('books')
</script>

<template>
  <div class="admin-container">
    
    <div class="admin-header">
      <h2>🛠️ Quản lý Website - BookStore Admin</h2>
    </div>

    <div class="dashboard-layout">

      <!-- SIDEBAR -->
      <div class="sidebar">
        <h3>Menu Quản lý</h3>

        <button 
          @click="activeTab = 'books'" 
          :class="{ active: activeTab === 'books' }"
          class="menu-btn"
        >
          📚 Quản lý Sách
        </button>

        <button 
          @click="activeTab = 'users'" 
          :class="{ active: activeTab === 'users' }"
          class="menu-btn"
        >
          👤 Quản lý User
        </button>

        <button 
          @click="activeTab = 'orders'" 
          :class="{ active: activeTab === 'orders' }"
          class="menu-btn"
        >
          📦 Quản lý Đơn hàng
        </button>

        <button 
          @click="activeTab = 'history'" 
          :class="{ active: activeTab === 'history' }"
          class="menu-btn"
        >
          📜 Lịch sử giao dịch
        </button>

        <router-link to="/" class="btn-back">
          ⬅️ Quay về Trang chủ
        </router-link>
      </div>

      <!-- MAIN CONTENT -->
      <div class="content">
        <ManageBooks v-if="activeTab === 'books'" />
        <ManageUsers v-if="activeTab === 'users'" />
        <ManageOrders v-if="activeTab === 'orders'" />

        <div v-if="activeTab === 'history'" class="history-section">
          <h3>📜 Lịch sử giao dịch</h3>
          <p>Chức năng đang phát triển...</p>
        </div>
      </div>

    </div>
  </div>
</template>

<style scoped>
.admin-container {
  min-height: 100vh;
  background: #ecf0f1;
}

.admin-header {
  background: #2c3e50;
  color: white;
  padding: 20px 30px;
  text-align: center;
}

.admin-header h2 {
  margin: 0;
}

.dashboard-layout {
  display: flex;
  min-height: calc(100vh - 80px);
}

/* SIDEBAR */
.sidebar {
  width: 250px;
  background: #34495e;
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.sidebar h3 {
  color: white;
  margin-bottom: 20px;
  text-align: center;
}

.menu-btn {
  width: 100%;
  padding: 12px;
  text-align: left;
  background: #2c3e50;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 16px;
  transition: 0.3s;
}

.menu-btn:hover {
  background: #1a252f;
}

.menu-btn.active {
  background: #3498db;
  font-weight: bold;
}

.btn-back {
  margin-top: auto;
  padding: 12px;
  background: #e74c3c;
  color: white;
  text-decoration: none;
  text-align: center;
  border-radius: 4px;
  transition: 0.3s;
}

.btn-back:hover {
  background: #c0392b;
}

/* CONTENT */
.content {
  flex: 1;
  padding: 30px;
  background: white;
  overflow-y: auto;
}

.history-section {
  text-align: center;
  padding: 50px;
}

.history-section h3 {
  color: #2c3e50;
  margin-bottom: 20px;
}

.history-section p {
  color: #7f8c8d;
  font-size: 16px;
}
</style>