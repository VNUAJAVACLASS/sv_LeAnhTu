<template>
  <div class="overlay">
    <div class="box">
      <header>
        <h3>{{ title }}</h3>
        <button @click="$emit('close')">✖</button>
      </header>
      <slot />
    </div>
  </div>
</template>
