<template>
  <div style="max-width:700px;margin:32px auto;padding:0 16px">
    <h2 style="margin-bottom:24px">結帳</h2>
    <el-steps :active="step" finish-status="success" style="margin-bottom:32px">
      <el-step title="確認訂單" /><el-step title="填寫資料" /><el-step title="完成" />
    </el-steps>
    <el-card v-if="step===0">
      <el-table :data="cart.items">
        <el-table-column label="商品" prop="name" />
        <el-table-column label="數量" prop="quantity" width="80" />
        <el-table-column label="小計" width="120">
          <template #default="{ row }">NT$ {{ row.price * row.quantity }}</template>
        </el-table-column>
      </el-table>
      <div style="text-align:right;margin-top:16px">
        <b style="font-size:18px">總計：NT$ {{ cart.total }}</b>
        <el-button type="primary" style="margin-left:16px" @click="step++">下一步</el-button>
      </div>
    </el-card>
    <el-card v-if="step===1">
      <el-form :model="form" label-width="90px">
        <el-form-item label="收件人"><el-input v-model="form.name" placeholder="請輸入姓名" /></el-form-item>
        <el-form-item label="連絡電話"><el-input v-model="form.phone" placeholder="請輸入電話" /></el-form-item>
        <el-form-item label="收件地址"><el-input v-model="form.address" placeholder="請輸入地址" /></el-form-item>
        <el-form-item>
          <el-button @click="step--">上一步</el-button>
          <el-button type="primary" :loading="loading" @click="placeOrder" style="margin-left:12px">確認下單</el-button>
        </el-form-item>
      </el-form>
    </el-card>
    <el-result v-if="step===2" icon="success" title="訂單成立！" :sub-title="`訂單編號：${orderId}`">
      <template #extra>
        <el-button type="primary" @click="$router.push('/orders')">查看訂單</el-button>
        <el-button @click="$router.push('/')">繼續購物</el-button>
      </template>
    </el-result>
  </div>
</template>
<script setup>
import { ref } from 'vue'
import { orderApi } from '@/api'
import { useCartStore } from '@/stores/cart'
import { ElMessage } from 'element-plus'
const cart = useCartStore(), step = ref(0), loading = ref(false), orderId = ref('')
const form = ref({ name:'', phone:'', address:'' })
async function placeOrder() {
  loading.value = true
  try {
    const res = await orderApi.create({ items: cart.items, shippingAddress: form.value.address, totalAmount: cart.total })
    orderId.value = res.orderId || res.id || 'ORD-' + Date.now()
    cart.clear(); step.value = 2
  } catch { ElMessage.error('訂單建立失敗') } finally { loading.value = false }
}
</script>
