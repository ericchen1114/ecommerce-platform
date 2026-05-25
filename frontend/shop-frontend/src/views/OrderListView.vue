<template>
  <div style="max-width:900px;margin:32px auto;padding:0 16px">
    <h2 style="margin-bottom:20px">我的訂單</h2>
    <el-table :data="orders" v-loading="loading" border>
      <el-table-column label="訂單編號" prop="id" min-width="180" />
      <el-table-column label="建立時間" prop="createdAt" width="180" />
      <el-table-column label="金額" width="130">
        <template #default="{ row }"><b style="color:#e6564e">NT$ {{ row.totalAmount }}</b></template>
      </el-table-column>
      <el-table-column label="狀態" width="120">
        <template #default="{ row }">
          <el-tag :type="{PENDING:'warning',CONFIRMED:'success',CANCELLED:'danger'}[row.status]||''">{{ row.status }}</el-tag>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>
<script setup>
import { ref, onMounted } from 'vue'
import { orderApi } from '@/api'
const orders = ref([]), loading = ref(false)
onMounted(async () => {
  loading.value = true
  try { orders.value = await orderApi.list() } catch { orders.value = [] } finally { loading.value = false }
})
</script>
