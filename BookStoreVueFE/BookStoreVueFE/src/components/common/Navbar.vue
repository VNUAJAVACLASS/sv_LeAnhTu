<script setup>
import { useRouter } from 'vue-router'
import { useCartStore } from '@/stores/cart.store'

const router = useRouter()
const cartStore = useCartStore()

const goToCart = () => {
  router.push('/cart')
}
</script>
<template>
  <nav class="navbar">

    <!-- Các menu khác của bạn -->

    <button @click="goToCart" class="cart-btn">
      🛒
      <span v-if="cartStore.items.length > 0" class="badge">
        {{ cartStore.items.length }}
      </span>
    </button>

  </nav>
</template>
<style scoped>
.cart-btn {
  position: relative;
  font-size: 22px;
  border: none;
  background: none;
  cursor: pointer;
  margin-left: 15px;
}

.badge {
  position: absolute;
  top: -6px;
  right: -10px;
  background: red;
  color: white;
  border-radius: 50%;
  padding: 2px 7px;
  font-size: 12px;
  font-weight: bold;
}
</style>
