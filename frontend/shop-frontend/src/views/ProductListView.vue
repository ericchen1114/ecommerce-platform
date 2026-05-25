<template>
  <div style="max-width:1200px;margin:24px auto;padding:0 16px">
    <el-row :gutter="20">
      <el-col :span="5">
        <el-card>
          <h3 style="margin-bottom:16px">篩選</h3>
          <el-input v-model="keyword" placeholder="搜尋商品" clearable @input="search" prefix-icon="Search" />
          <el-divider />
          <p style="margin-bottom:8px">價格範圍</p>
          <el-slider v-model="priceRange" range :max="10000" @change="search" />
          <div style="display:flex;justify-content:space-between;font-size:12px;color:#999">
            <span>NT${{ priceRange[0] }}</span><span>NT${{ priceRange[1] }}</span>
          </div>
        </el-card>
      </el-col>
      <el-col :span="19">
        <div v-if="loading" style="text-align:center;padding:80px">
          <el-icon class="is-loading" size="48" color="#409eff"><Loading /></el-icon>
        </div>
        <ProductGrid v-else :products="products" />
        <el-pagination v-model:current-page="page" :total="total" :page-size="12"
          layout="prev,pager,next" style="margin-top:24px;justify-content:center;display:flex"
          @current-change="loadProducts" />
      </el-col>
    </el-row>
  </div>
</template>
<script setup>
import { ref, onMounted } from 'vue'
import { productApi } from '@/api'
import ProductGrid from '@/components/ProductGrid.vue'
const products = ref([]), keyword = ref(''), priceRange = ref([0,10000])
const page = ref(1), total = ref(0), loading = ref(false)
async function loadProducts() {
  loading.value = true
  try { const r = await productApi.list({ page: page.value-1, size: 12, keyword: keyword.value }); products.value = r.content ?? r ?? []; total.value = r.totalElements ?? 0 }
  catch { products.value = [] } finally { loading.value = false }
}
function search() { page.value = 1; loadProducts() }
onMounted(loadProducts)
</script>
