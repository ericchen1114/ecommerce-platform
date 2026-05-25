<template>
  <div>
    <el-carousel height="320px">
      <el-carousel-item v-for="n in 3" :key="n">
        <div :style="`height:100%;background:linear-gradient(135deg,hsl(${n*80},70%,60%),hsl(${n*80+40},70%,50%));display:flex;align-items:center;justify-content:center`">
          <h1 style="color:#fff;font-size:36px">精選商品 {{ n }}</h1>
        </div>
      </el-carousel-item>
    </el-carousel>
    <div style="max-width:1200px;margin:32px auto;padding:0 16px">
      <h2 style="margin-bottom:20px">熱門商品</h2>
      <ProductGrid :products="products" />
    </div>
  </div>
</template>
<script setup>
import { ref, onMounted } from 'vue'
import { productApi } from '@/api'
import ProductGrid from '@/components/ProductGrid.vue'
const products = ref([])
onMounted(async () => {
  try { const r = await productApi.list({ size: 8 }); products.value = r.content ?? r ?? [] } catch { products.value = [] }
})
</script>
