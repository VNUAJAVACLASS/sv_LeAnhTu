<template>
  <footer class="footer">
    <p>© 2025 - Thiết kế bởi <strong>{{ author }}</strong></p>
  </footer>
</template>

<script>
export default {
  name: "AppFooter",
  data() {
    return {
      author: "GV. Trần Trung Hiếu - Website: https://hieutrantrung001100.blogspot.com"
    };
  }
};
</script>

<style scoped>
.footer {
  background-color: #2c3e50;
  color: #fff;
  text-align: center;
  padding: 15px 0;
  margin-top: auto;
  font-size: 13px;
  box-shadow: 0 -2px 5px rgba(0, 0, 0, 0.1);
}

.footer p {
  margin: 0;
}

.footer strong {
  color: #3498db;
}
</style>