<script setup>
import { ref, onMounted } from 'vue'
import api from '@/api/axios'

const users = ref([])

const loadUsers = async () => {
  const res = await api.get('/users')
  users.value = res.data
}

onMounted(loadUsers)

const deleteUser = async (id) => {
  if (!confirm('Xóa user này?')) return

  await api.delete(`/users/${id}`)
  loadUsers()
}
</script>

<template>
  <div>
    <h3>Quản lý user</h3>

    <table>
      <tr>
        <th>ID</th>
        <th>Username</th>
        <th>Role</th>
        <th></th>
      </tr>

      <tr v-for="u in users" :key="u.id">
        <td>{{ u.id }}</td>
        <td>{{ u.username }}</td>
        <td>{{ u.roles }}</td>

        <td>
          <button @click="deleteUser(u.id)">Xóa</button>
        </td>
      </tr>
    </table>
  </div>
</template>
