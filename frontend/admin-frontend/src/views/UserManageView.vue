<template>
  <el-card>
    <h2 style="margin-bottom:20px">用戶管理</h2>
    <el-table :data="users" v-loading="loading" border stripe>
      <el-table-column label="帳號" prop="username" width="150" />
      <el-table-column label="Email" prop="email" min-width="200" />
      <el-table-column label="建立時間" prop="createdAt" width="180" />
      <el-table-column label="狀態" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="row.active?'success':'danger'" size="small">{{ row.active?'正常':'停用' }}</el-tag>
        </template>
      </el-table-column>
    </el-table>
  </el-card>
</template>
<script setup>
import { ref, onMounted } from 'vue'
import { adminApi } from '@/api'
const users=ref([]), loading=ref(false)
onMounted(async () => { loading.value=true; try { const r=await adminApi.getUsers(); users.value=r.content??r??[] } catch { users.value=[] } finally { loading.value=false } })
</script>
