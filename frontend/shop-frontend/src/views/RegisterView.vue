<template>
  <div style="display:flex;justify-content:center;align-items:center;min-height:75vh;background:#f5f7fa">
    <el-card style="width:420px;border-radius:12px">
      <h2 style="text-align:center;margin-bottom:28px">會員註冊</h2>
      <el-form :model="form" :rules="rules" ref="formRef" label-width="80px">
        <el-form-item label="帳號" prop="username"><el-input v-model="form.username" /></el-form-item>
        <el-form-item label="Email" prop="email"><el-input v-model="form.email" /></el-form-item>
        <el-form-item label="密碼" prop="password"><el-input v-model="form.password" type="password" show-password /></el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" style="width:100%" @click="doRegister">註冊</el-button>
        </el-form-item>
      </el-form>
      <p style="text-align:center;color:#666">已有帳號？<router-link to="/login" style="color:#409eff">立即登入</router-link></p>
    </el-card>
  </div>
</template>
<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { userApi } from '@/api'
const router = useRouter(), formRef = ref(), loading = ref(false)
const form = ref({ username:'', email:'', password:'' })
const rules = {
  username: [{ required: true, message: '請輸入帳號', trigger: 'blur' }],
  email:    [{ required: true, type: 'email', message: '請輸入有效 Email', trigger: 'blur' }],
  password: [{ required: true, min: 6, message: '至少 6 個字元', trigger: 'blur' }],
}
async function doRegister() {
  await formRef.value.validate()
  loading.value = true
  try { await userApi.register(form.value); ElMessage.success('註冊成功！'); router.push('/login') }
  catch { ElMessage.error('帳號已存在') } finally { loading.value = false }
}
</script>
