<template>
  <div style="max-width:900px;margin:32px auto;padding:0 16px">
    <h2 style="margin-bottom:20px">購物車</h2>
    <el-empty v-if="!cart.items.length" description="購物車是空的" :image-size="120">
      <el-button type="primary" @click="$router.push('/products')">去選購商品</el-button>
    </el-empty>
    <template v-else>
      <el-table :data="cart.items" style="margin-bottom:20px">
        <el-table-column label="商品名稱" prop="name" min-width="200" />
        <el-table-column label="單價" width="120">
          <template #default="{ row }"><span style="color:#e6564e">NT$ {{ row.price }}</span></template>
        </el-table-column>
        <el-table-column label="數量" width="160">
          <template #default="{ row }">
            <el-input-number v-model="row.quantity" :min="1" size="small" />
          </template>
        </el-table-column>
        <el-table-column label="小計" width="120">
          <template #default="{ row }"><b>NT$ {{ row.price * row.quantity }}</b></template>
        </el-table-column>
        <el-table-column label="" width="80">
          <template #default="{ row }">
            <el-button type="danger" link @click="cart.removeItem(row.id)"><el-icon><Delete /></el-icon></el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-card>
        <div style="display:flex;justify-content:space-between;align-items:center">
          <span style="font-size:18px">合計：<b style="font-size:24px;color:#e6564e">NT$ {{ cart.total }}</b></span>
          <el-button type="primary" size="large" @click="$router.push('/checkout')">前往結帳 →</el-button>
        </div>
      </el-card>
    </template>
  </div>
</template>
<script setup>
import { useCartStore } from '@/stores/cart'
const cart = useCartStore()
</script>
