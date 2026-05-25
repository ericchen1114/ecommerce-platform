<template>
  <el-row :gutter="16">
    <el-col v-for="p in products" :key="p.id" :xs="12" :sm="8" :md="6" style="margin-bottom:20px">
      <el-card class="pcard" shadow="hover" @click="$router.push(`/products/${p.id}`)">
        <el-image :src="p.imageUrl||`https://picsum.photos/seed/${p.id}/300/200`"
          fit="cover" style="width:100%;height:160px;border-radius:6px 6px 0 0" />
        <div style="padding:12px">
          <p style="font-weight:500;margin-bottom:6px;overflow:hidden;text-overflow:ellipsis;white-space:nowrap">{{ p.name }}</p>
          <p style="color:#e6564e;font-size:16px;font-weight:bold;margin-bottom:10px">NT$ {{ p.price }}</p>
          <el-button type="primary" size="small" style="width:100%" @click.stop="add(p)">加入購物車</el-button>
        </div>
      </el-card>
    </el-col>
    <el-col v-if="!products?.length" :span="24">
      <el-empty description="暫無商品" />
    </el-col>
  </el-row>
</template>
<script setup>
import { ElMessage } from 'element-plus'
import { useCartStore } from '@/stores/cart'
defineProps(['products'])
const cart = useCartStore()
function add(p) { cart.addItem(p); ElMessage.success(`已加入：${p.name}`) }
</script>
<style scoped>
.pcard { cursor:pointer; transition:transform .2s; }
.pcard:hover { transform:translateY(-4px); }
</style>
