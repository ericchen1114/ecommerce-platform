<template>
  <el-card border-radius="12px">
    <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:20px">
      <h2 style="margin:0">商品管理</h2>
      <el-button type="primary" @click="openDialog()"><el-icon><Plus /></el-icon>新增商品</el-button>
    </div>
    <el-table :data="products" v-loading="loading" border stripe>
      <el-table-column label="名稱" prop="name" min-width="160" />
      <el-table-column label="價格" width="110">
        <template #default="{ row }"><b style="color:#e6564e">NT$ {{ row.price }}</b></template>
      </el-table-column>
      <el-table-column label="庫存" prop="stock" width="80" align="center" />
      <el-table-column label="狀態" width="90" align="center">
        <template #default="{ row }">
          <el-tag :type="row.active?'success':'info'" size="small">{{ row.active?'上架':'下架' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="140" align="center">
        <template #default="{ row }">
          <el-button size="small" @click="openDialog(row)">編輯</el-button>
          <el-button size="small" type="danger" @click="del(row.id)">刪除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="show" :title="form.id?'編輯商品':'新增商品'" width="480px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="商品名稱"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="價格"><el-input-number v-model="form.price" :min="0" style="width:100%" /></el-form-item>
        <el-form-item label="庫存"><el-input-number v-model="form.stock" :min="0" style="width:100%" /></el-form-item>
        <el-form-item label="描述"><el-input v-model="form.description" type="textarea" :rows="3" /></el-form-item>
        <el-form-item label="狀態"><el-switch v-model="form.active" active-text="上架" inactive-text="下架" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="show=false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="save">儲存</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>
<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { adminApi } from '@/api'
const products=ref([]), loading=ref(false), saving=ref(false), show=ref(false)
const form=ref({ id:null, name:'', price:0, stock:0, description:'', active:true })
async function load() { loading.value=true; try { const r=await adminApi.getProducts(); products.value=r.content??r??[] } catch { products.value=[] } finally { loading.value=false } }
function openDialog(p=null) { form.value=p?{...p}:{id:null,name:'',price:0,stock:0,description:'',active:true}; show.value=true }
async function save() { saving.value=true; try { form.value.id?await adminApi.updateProduct(form.value.id,form.value):await adminApi.createProduct(form.value); ElMessage.success('儲存成功'); show.value=false; load() } catch { ElMessage.error('失敗') } finally { saving.value=false } }
async function del(id) { await ElMessageBox.confirm('確定刪除？','提示',{type:'warning'}); await adminApi.deleteProduct(id); ElMessage.success('已刪除'); load() }
onMounted(load)
</script>
