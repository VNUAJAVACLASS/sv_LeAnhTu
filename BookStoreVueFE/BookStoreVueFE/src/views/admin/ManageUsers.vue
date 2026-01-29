<script setup>
import { ref, onMounted, computed } from 'vue'
import api from '@/api/axios'
import Swal from 'sweetalert2'
import Pagination from '@/components/common/Pagination.vue'

const users = ref([])
const searchQuery = ref('')
const form = ref({
  id: null,
  username: '',
  password: '',
  gmail: '',
  soDienThoai: '',
  diaChi: '',
  roles: []
})

const isEditing = ref(false)
const showForm = ref(false)

// ===== PHÂN TRANG =====
const currentPage = ref(0)
const pageSize = ref(20)
const totalPages = ref(0)
const totalItems = ref(0)
const hasNext = ref(false)
const hasPrevious = ref(false)

// Tìm kiếm local (sau khi đã load về)
const filteredUsers = computed(() => {
  if (!searchQuery.value) return users.value
  const query = searchQuery.value.toLowerCase()
  return users.value.filter(user =>
    user.username?.toLowerCase().includes(query) ||
    user.gmail?.toLowerCase().includes(query) ||
    user.soDienThoai?.includes(query)
  )
})

const loadUsers = async (page = 0) => {
  try {
    const res = await api.get('/users', {
      params: { page, size: pageSize.value }
    })

    const data = res.data
    users.value = data.content || data
    currentPage.value = data.currentPage || 0
    totalPages.value = data.totalPages || 1
    totalItems.value = data.totalItems || data.length
    hasNext.value = data.hasNext || false
    hasPrevious.value = data.hasPrevious || false
  } catch (error) {
    console.error('Lỗi tải users:', error)
  }
}

onMounted(() => loadUsers())

const openAddForm = () => {
  form.value = {
    id: null,
    username: '',
    password: '',
    gmail: '',
    soDienThoai: '',
    diaChi: '',
    roles: ['ROLE_USER']
  }
  isEditing.value = false
  showForm.value = true
}

const editUser = (user) => {
  form.value = {
    id: user.id,
    username: user.username,
    password: '',
    gmail: user.gmail || '',
    soDienThoai: user.soDienThoai || '',
    diaChi: user.diaChi || '',
    roles: user.roles?.map(r => r.name) || ['ROLE_USER']
  }
  isEditing.value = true
  showForm.value = true
}

const deleteUser = async (id) => {
  const result = await Swal.fire({
    title: 'Bạn có chắc chắn?',
    text: "User sẽ bị xóa vĩnh viễn!",
    icon: 'warning',
    showCancelButton: true,
    confirmButtonColor: '#e74c3c',
    cancelButtonColor: '#95a5a6',
    confirmButtonText: 'Xóa',
    cancelButtonText: 'Hủy'
  })

  if (result.isConfirmed) {
    try {
      await api.delete(`/users/${id}`)
      Swal.fire('Đã xóa!', 'User đã được xóa thành công', 'success')
      loadUsers(currentPage.value)
    } catch (error) {
      Swal.fire('Lỗi!', error.response?.data || 'Không thể xóa user', 'error')
    }
  }
}

const saveUser = async () => {
  try {
    if (isEditing.value) {
      await api.patch(`/users/${form.value.id}/roles`, form.value.roles)
      Swal.fire('Thành công!', 'Cập nhật user thành công', 'success')
    } else {
      if (!form.value.username || !form.value.password) {
        Swal.fire('Lỗi!', 'Username và password không được để trống', 'error')
        return
      }
      await api.post('/auth/register', {
        username: form.value.username,
        password: form.value.password
      })
      Swal.fire('Thành công!', 'Thêm user mới thành công', 'success')
    }

    showForm.value = false
    loadUsers(currentPage.value)
  } catch (error) {
    Swal.fire('Lỗi!', error.response?.data || 'Không thể lưu user', 'error')
  }
}

const cancelForm = () => {
  showForm.value = false
}

const getRoleNames = (roles) => {
  if (!roles || !Array.isArray(roles)) return 'N/A'
  return roles.map(r => r.name || r).join(', ')
}
</script>

<template>
  <div class="manage-users">
    <div class="header">
      <h3>👤 Quản lý User</h3>
      <div class="header-actions">
        <input
          v-model="searchQuery"
          type="text"
          placeholder="🔍 Tìm kiếm user..."
          class="search-input"
        />
        <button @click="openAddForm" class="btn-add">
          <i class="fas fa-plus"></i> Thêm user mới
        </button>
      </div>
    </div>

    <!-- FORM THÊM/SỬA -->
    <div v-if="showForm" class="form-overlay">
      <div class="form-container">
        <h4>{{ isEditing ? '✏️ Sửa user' : '➕ Thêm user mới' }}</h4>

        <div class="form-group">
          <label>Username:</label>
          <input
            v-model="form.username"
            type="text"
            placeholder="Nhập username"
            :disabled="isEditing"
          />
        </div>

        <div v-if="!isEditing" class="form-group">
          <label>Password:</label>
          <input v-model="form.password" type="password" placeholder="Nhập password" />
        </div>

        <div class="form-group">
          <label>Gmail:</label>
          <input v-model="form.gmail" type="email" placeholder="Nhập gmail" />
        </div>

        <div class="form-group">
          <label>Số điện thoại:</label>
          <input v-model="form.soDienThoai" type="text" placeholder="Nhập SĐT" />
        </div>

        <div class="form-group">
          <label>Địa chỉ:</label>
          <textarea v-model="form.diaChi" rows="2" placeholder="Nhập địa chỉ"></textarea>
        </div>

        <div v-if="isEditing" class="form-group">
          <label>Roles:</label>
          <div class="checkbox-group">
            <label>
              <input type="checkbox" value="ROLE_USER" v-model="form.roles" />
              User
            </label>
            <label>
              <input type="checkbox" value="ROLE_ADMIN" v-model="form.roles" />
              Admin
            </label>
          </div>
        </div>

        <div class="form-actions">
          <button @click="cancelForm" class="btn-cancel">Hủy</button>
          <button @click="saveUser" class="btn-save">
            {{ isEditing ? 'Cập nhật' : 'Thêm mới' }}
          </button>
        </div>
      </div>
    </div>

    <!-- Bảng DANH SÁCH -->
    <div class="table-container">
      <table class="users-table">
        <thead>
          <tr>
            <th>ID</th>
            <th>Username</th>
            <th>Gmail</th>
            <th>SĐT</th>
            <th>Địa chỉ</th>
            <th>Roles</th>
            <th>Thao tác</th>
          </tr>
        </thead>

        <tbody>
          <tr v-for="user in filteredUsers" :key="user.id">
            <td>{{ user.id }}</td>
            <td class="username">{{ user.username }}</td>
            <td>{{ user.gmail || 'N/A' }}</td>
            <td>{{ user.soDienThoai || 'N/A' }}</td>
            <td class="address">{{ user.diaChi || 'N/A' }}</td>
            <td>
              <span class="role-badge">{{ getRoleNames(user.roles) }}</span>
            </td>
            <td>
              <button @click="editUser(user)" class="btn-edit">
                <i class="fas fa-edit"></i>
              </button>
              <button
                @click="deleteUser(user.id)"
                class="btn-delete"
                :disabled="user.superAdmin"
              >
                <i class="fas fa-trash"></i>
              </button>
            </td>
          </tr>
        </tbody>
      </table>

      <div v-if="filteredUsers.length === 0" class="empty-message">
        <p>{{ searchQuery ? 'Không tìm thấy user phù hợp' : 'Chưa có user nào trong hệ thống' }}</p>
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
      item-name="user"
      @page-change="loadUsers"
    />
  </div>
</template>

<style scoped>
/* Copy style từ file cũ và thêm style cho phân trang */
.manage-users {
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

.header-actions {
  display: flex;
  gap: 15px;
  align-items: center;
}

.search-input {
  padding: 10px 15px;
  border: 1px solid #ddd;
  border-radius: 4px;
  width: 250px;
  font-size: 14px;
}

.search-input:focus {
  outline: none;
  border-color: #3498db;
}

.btn-add {
  padding: 10px 20px;
  background: #27ae60;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-weight: 600;
  white-space: nowrap;
}

.btn-add:hover {
  background: #229954;
}

.form-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0,0,0,0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.form-container {
  background: white;
  padding: 30px;
  border-radius: 8px;
  width: 600px;
  max-height: 80vh;
  overflow-y: auto;
}

.form-container h4 {
  margin-bottom: 20px;
  color: #2c3e50;
}

.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  font-weight: 600;
  color: #2c3e50;
}

.form-group input,
.form-group textarea {
  width: 100%;
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
}

.checkbox-group {
  display: flex;
  gap: 20px;
}

.checkbox-group label {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: normal;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 20px;
}

.btn-cancel, .btn-save {
  padding: 10px 24px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-weight: 600;
}

.btn-cancel {
  background: #95a5a6;
  color: white;
}

.btn-cancel:hover {
  background: #7f8c8d;
}

.btn-save {
  background: #3498db;
  color: white;
}

.btn-save:hover {
  background: #2980b9;
}

.table-container {
  background: white;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 5px rgba(0,0,0,0.1);
  margin-bottom: 20px;
}

.users-table {
  width: 100%;
  border-collapse: collapse;
}

.users-table th,
.users-table td {
  padding: 15px;
  text-align: left;
  border-bottom: 1px solid #ddd;
}

.users-table th {
  background: #34495e;
  color: white;
  font-weight: 600;
}

.users-table tbody tr:hover {
  background: #f8f9fa;
}

.username {
  font-weight: 600;
  color: #2c3e50;
}

.address {
  max-width: 200px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.role-badge {
  background: #3498db;
  color: white;
  padding: 4px 10px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 600;
}

.btn-edit, .btn-delete {
  padding: 8px 12px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  margin-right: 5px;
}

.btn-edit {
  background: #3498db;
  color: white;
}

.btn-edit:hover {
  background: #2980b9;
}

.btn-delete {
  background: #e74c3c;
  color: white;
}

.btn-delete:hover:not(:disabled) {
  background: #c0392b;
}

.btn-delete:disabled {
  background: #95a5a6;
  cursor: not-allowed;
}

.empty-message {
  padding: 50px;
  text-align: center;
  color: #7f8c8d;
}
</style>