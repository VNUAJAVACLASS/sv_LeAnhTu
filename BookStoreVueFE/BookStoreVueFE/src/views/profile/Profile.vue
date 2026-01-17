<script setup>
import { ref } from 'vue'
import { useAuthStore } from '@/stores/auth.store'
import { useRouter } from 'vue-router'

const auth = useAuthStore()
const router = useRouter()

const activeTab = ref('info')

const goHome = () => {
  router.push('/')
}
</script>

<template>
  <div class="profile-container">

    <!-- SIDEBAR -->
    <div class="sidebar">
      <h3>👤 Trang cá nhân</h3>

      <button @click="activeTab = 'info'">
        Thông tin cá nhân
      </button>

      <button @click="activeTab = 'orders'">
        Đơn hàng của tôi
      </button>

      <button @click="activeTab = 'history'">
        Lịch sử mua hàng
      </button>

      <button @click="activeTab = 'password'">
        Đổi mật khẩu
      </button>

      <button @click="activeTab = 'update'">
        Cập nhật thông tin
      </button>

      <button @click="goHome">
        Quay về trang chủ
      </button>
    </div>

    <!-- MAIN CONTENT -->
    <div class="content">

      <!-- TAB: INFO -->
      <div v-if="activeTab === 'info'">
        <h3>Thông tin tài khoản</h3>

        <p><b>Username:</b> {{ auth.user?.username }}</p>

        <p><b>Vai trò:</b></p>
        <ul>
          <li v-for="r in auth.user?.roles" :key="r">
            {{ r }}
          </li>
        </ul>
      </div>

      <!-- TAB: ORDERS -->
      <div v-if="activeTab === 'orders'">
        <h3>Đơn hàng của tôi</h3>
        <p>(Sẽ tích hợp API đơn hàng của user sau)</p>
      </div>

      <!-- TAB: HISTORY -->
      <div v-if="activeTab === 'history'">
        <h3>Lịch sử mua hàng</h3>
        <p>(Sẽ tích hợp API lịch sử sau)</p>
      </div>

      <!-- TAB: CHANGE PASSWORD -->
      <div v-if="activeTab === 'password'">
        <h3>Đổi mật khẩu</h3>

        <input placeholder="Mật khẩu cũ" type="password" />
        <input placeholder="Mật khẩu mới" type="password" />
        <input placeholder="Nhập lại mật khẩu" type="password" />

        <button>Đổi mật khẩu</button>
      </div>

      <!-- TAB: UPDATE INFO -->
      <div v-if="activeTab === 'update'">
        <h3>Cập nhật thông tin</h3>

        <input placeholder="Họ tên" />
        <input placeholder="Email" />
        <input placeholder="Số điện thoại" />

        <button>Lưu thay đổi</button>
      </div>

    </div>

  </div>
</template>

<style scoped>
.profile-container {
  display: flex;
  min-height: 80vh;
}

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

.content {
  width: 75%;
  padding: 20px;
}

.content input {
  display: block;
  margin: 10px 0;
  padding: 8px;
  width: 60%;
}
</style>
