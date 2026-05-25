import { createRouter, createWebHistory } from 'vue-router'
const routes = [
  {
    path: '/',
    component: () => import('@/layouts/AdminLayout.vue'),
    children: [
      { path: '', redirect: '/dashboard' },
      { path: '/dashboard', component: () => import('@/views/DashboardView.vue') },
      { path: '/products',  component: () => import('@/views/ProductManageView.vue') },
      { path: '/orders',    component: () => import('@/views/OrderManageView.vue') },
      { path: '/users',     component: () => import('@/views/UserManageView.vue') },
    ]
  },
  { path: '/login', component: () => import('@/views/AdminLoginView.vue') }
]
const router = createRouter({ history: createWebHistory(), routes })
router.beforeEach((to) => {
  if (to.path !== '/login' && !localStorage.getItem('admin_token')) return '/login'
})
export default router
