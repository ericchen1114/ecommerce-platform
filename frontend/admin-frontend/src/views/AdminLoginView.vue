<template>
  <div style="display:flex;justify-content:center;align-items:center;height:100vh;background:linear-gradient(135deg,#1a2e42,#0d1b2a)">
    <el-card style="width:400px;border-radius:12px">
      <h2 style="text-align:center;margin-bottom:28px">⚙️ 後台管理登入</h2>
      <el-form :model="form" label-width="80px">
        <el-form-item label="帳號"><el-input v-model="form.username" prefix-icon="User" /></el-form-item>
        <el-form-item label="密碼"><el-input v-model="form.password" type="password" prefix-icon="Lock" show-password /></el-form-item>
        <el-form-item>
          <el-button type="primary" style="width:100%;height:42px" :loading="loading" @click="doLogin">登入</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>
<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { adminApi } from '@/api'
const router = useRouter(), loading = ref(false)
const form = ref({ username:'', password:'' })
async function doLogin() {
  loading.value = true
  try {
    const res = await adminApi.login(form.value)
    localStorage.setItem('admin_token', res.token)
    router.push('/dashboard')
  } catch { ElMessage.error('帳號或密碼錯誤') } finally { loading.value = false }
}
</script>
