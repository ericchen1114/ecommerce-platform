import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
const routes = [
  { path: '/',             component: () => import('@/views/HomeView.vue') },
  { path: '/products',     component: () => import('@/views/ProductListView.vue') },
  { path: '/products/:id', component: () => import('@/views/ProductDetailView.vue') },
  { path: '/cart',         component: () => import('@/views/CartView.vue') },
  { path: '/checkout',     component: () => import('@/views/CheckoutView.vue'), meta: { requiresAuth: true } },
  { path: '/login',        component: () => import('@/views/LoginView.vue') },
  { path: '/register',     component: () => import('@/views/RegisterView.vue') },
  { path: '/orders',       component: () => import('@/views/OrderListView.vue'), meta: { requiresAuth: true } },
]
const router = createRouter({ history: createWebHistory(), routes })
router.beforeEach((to) => {
  const auth = useAuthStore()
  if (to.meta.requiresAuth && !auth.isLoggedIn) return '/login'
})
export default router
