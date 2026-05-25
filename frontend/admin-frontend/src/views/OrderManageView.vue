<template>
  <el-card>
    <h2 style="margin-bottom:20px">訂單管理</h2>
    <el-table :data="orders" v-loading="loading" border stripe>
      <el-table-column label="訂單編號" prop="id" min-width="180" />
      <el-table-column label="用戶" prop="userId" width="120" />
      <el-table-column label="金額" width="130">
        <template #default="{ row }"><b style="color:#e6564e">NT$ {{ row.totalAmount }}</b></template>
      </el-table-column>
      <el-table-column label="建立時間" prop="createdAt" width="170" />
      <el-table-column label="狀態" width="160">
        <template #default="{ row }">
          <el-select v-model="row.status" size="small" @change="updateStatus(row)" style="width:130px">
            <el-option label="待付款" value="PENDING" />
            <el-option label="處理中" value="PROCESSING" />
            <el-option label="已完成" value="CONFIRMED" />
            <el-option label="已取消" value="CANCELLED" />
          </el-select>
        </template>
      </el-table-column>
    </el-table>
  </el-card>
</template>
<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { adminApi } from '@/api'
const orders=ref([]), loading=ref(false)
onMounted(async () => { loading.value=true; try { const r=await adminApi.getOrders(); orders.value=r.content??r??[] } catch { orders.value=[] } finally { loading.value=false } })
async function updateStatus(row) { try { await adminApi.updateOrder(row.id,{status:row.status}); ElMessage.success('狀態已更新') } catch { ElMessage.error('更新失敗') } }
</script>
