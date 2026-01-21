<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import AppHeader from '@/components/common/AppHeader.vue'
import AppFooter from '@/components/common/AppFooter.vue'

const route = useRoute()

// Ẩn Header / Footer ở trang register và login
const showLayout = computed(() => {
  return route.path !== '/register' && route.path !== '/login'
})
</script>

<template>
  <div class="app-container">
    <!-- HEADER -->
    <AppHeader v-if="showLayout" />

    <!-- MAIN CONTENT -->
    <main :class="{ 'full-page': !showLayout }">
      <router-view />
    </main>

    <!-- FOOTER -->
    <AppFooter v-if="showLayout" />
  </div>
</template>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
  background-color: #f5f5f5;
}

.app-container {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
}

main {
  flex: 1;
  padding: 20px;
  background-color: #f8f9fa;
  padding-bottom: 80px; /* Dành chỗ cho footer */
}

main.full-page {
  padding: 0;
  padding-bottom: 0;
}
</style>