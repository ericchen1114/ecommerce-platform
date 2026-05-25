<template>
  <el-container style="height:100vh">
    <el-aside width="220px" style="background:#001529;overflow:hidden">
      <div style="color:#fff;font-size:18px;font-weight:bold;padding:20px;text-align:center;border-bottom:1px solid #1a2e42">
        🛒 後台管理
      </div>
      <el-menu router background-color="#001529" text-color="rgba(255,255,255,0.7)"
        active-text-color="#fff" style="border:none">
        <el-menu-item index="/dashboard">
          <el-icon><DataAnalysis /></el-icon><span>數據看板</span>
        </el-menu-item>
        <el-menu-item index="/products">
          <el-icon><Goods /></el-icon><span>商品管理</span>
        </el-menu-item>
        <el-menu-item index="/orders">
          <el-icon><List /></el-icon><span>訂單管理</span>
        </el-menu-item>
        <el-menu-item index="/users">
          <el-icon><User /></el-icon><span>用戶管理</span>
        </el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header style="background:#fff;box-shadow:0 1px 4px rgba(0,0,0,.1);display:flex;align-items:center;justify-content:flex-end;padding:0 24px">
        <el-button text type="danger" @click="logout"><el-icon><SwitchButton /></el-icon>登出</el-button>
      </el-header>
      <el-main style="background:#f0f2f5;padding:24px">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>
<script setup>
import { useRouter } from 'vue-router'
const router = useRouter()
function logout() { localStorage.removeItem('admin_token'); router.push('/login') }
</script>
