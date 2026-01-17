<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'

import AppHeader from '@/components/common/AppHeader.vue'
import AppFooter from '@/components/common/AppFooter.vue'

const route = useRoute()

/**
 * Ẩn Header / Footer ở trang login & register
 * (thực tế dự án hay làm vậy)
 */
const showLayout = computed(() => {
  return !['/login', '/register'].includes(route.path)
})
</script>

<template>
  <div class="app-container">
    <!-- HEADER -->
    <AppHeader v-if="showLayout" />

    <!-- MAIN CONTENT -->
    <main class="min-h-screen">
      <router-view />
    </main>

    <!-- FOOTER -->
    <AppFooter v-if="showLayout" />
  </div>
</template>

<style scoped>
.app-container {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
}

main {
  flex: 1;
  padding: 16px;
  background-color: #f8f9fa;
}
</style>
