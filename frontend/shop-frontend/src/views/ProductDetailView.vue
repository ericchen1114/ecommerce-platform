<template>
  <div style="max-width:1000px;margin:32px auto;padding:0 16px">
    <div v-if="product">
      <el-row :gutter="40">
        <el-col :span="10">
          <el-image :src="product.imageUrl||'https://via.placeholder.com/400x400'" fit="cover"
            style="width:100%;height:400px;border-radius:12px;box-shadow:0 4px 20px rgba(0,0,0,.1)" />
        </el-col>
        <el-col :span="14">
          <h1 style="font-size:28px;margin-bottom:12px">{{ product.name }}</h1>
          <div style="font-size:32px;color:#e6564e;font-weight:bold;margin-bottom:16px">NT$ {{ product.price }}</div>
          <p style="color:#666;line-height:1.8;margin-bottom:24px">{{ product.description }}</p>
          <div style="display:flex;align-items:center;gap:16px;margin-bottom:24px">
            <span style="color:#666">數量：</span>
            <el-input-number v-model="qty" :min="1" :max="product.stock||99" />
          </div>
          <el-button type="primary" size="large" @click="addToCart" style="width:200px">
            <el-icon style="margin-right:6px"><ShoppingCart /></el-icon>加入購物車
          </el-button>
        </el-col>
      </el-row>
    </div>
    <el-skeleton v-else :rows="8" animated style="margin-top:32px" />
  </div>
</template>
<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ShoppingCart } from '@element-plus/icons-vue'
import { productApi } from '@/api'
import { useCartStore } from '@/stores/cart'
const route = useRoute(), cart = useCartStore()
const product = ref(null), qty = ref(1)
onMounted(async () => { try { product.value = await productApi.detail(route.params.id) } catch {} })
function addToCart() { cart.addItem(product.value, qty.value); ElMessage.success('已加入購物車 🛒') }
</script>
