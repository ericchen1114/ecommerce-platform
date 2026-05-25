<template>
  <el-container class="app-wrap">
    <el-header class="app-header">
      <div class="hl">
        <router-link to="/" class="logo">🛍️ EShop</router-link>
        <router-link to="/">首頁</router-link>
        <router-link to="/products">商品</router-link>
      </div>
      <div class="hr">
        <router-link to="/cart">
          <el-badge :value="cart.count" :hidden="!cart.count">
            <el-button :icon="ShoppingCart" circle />
          </el-badge>
        </router-link>
        <template v-if="auth.isLoggedIn">
          <router-link to="/orders"><el-button text>訂單</el-button></router-link>
          <el-button text @click="auth.logout()">登出</el-button>
        </template>
        <template v-else>
          <router-link to="/login"><el-button type="primary" size="small">登入</el-button></router-link>
          <router-link to="/register"><el-button size="small">註冊</el-button></router-link>
        </template>
      </div>
    </el-header>
    <el-main><router-view /></el-main>
  </el-container>
</template>
<script setup>
import { ShoppingCart } from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'
import { useCartStore } from '@/stores/cart'
const auth = useAuthStore()
const cart = useCartStore()
</script>
<style>
* { margin: 0; padding: 0; box-sizing: border-box; }
body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif; }
.app-wrap { min-height: 100vh; }
.app-header { display:flex; align-items:center; justify-content:space-between;
  padding:0 32px; background:#fff; box-shadow:0 2px 8px rgba(0,0,0,.08); position:sticky; top:0; z-index:99; }
.hl { display:flex; align-items:center; gap:24px; }
.hr { display:flex; align-items:center; gap:12px; }
.logo { font-size:20px; font-weight:bold; color:#409eff; text-decoration:none; }
.hl a { text-decoration:none; color:#333; font-size:14px; }
.hl a:hover { color:#409eff; }
</style>
