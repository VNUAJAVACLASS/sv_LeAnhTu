import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import axios from 'axios'

// Import CSS
import '@fortawesome/fontawesome-free/css/all.min.css'
import 'bootstrap/dist/css/bootstrap.css'
import 'sweetalert2/dist/sweetalert2.min.css'

// Import Router
import router from './router'

// Import Auth Store
import { useAuthStore } from './stores/auth.store'

// Axios config
axios.defaults.baseURL = 'http://localhost:8080/'

const app = createApp(App)
const pinia = createPinia()

app.use(pinia)
app.use(router)

// ✅ Khởi tạo auth từ localStorage khi app load
const authStore = useAuthStore()
authStore.initAuth()

app.mount('#app')