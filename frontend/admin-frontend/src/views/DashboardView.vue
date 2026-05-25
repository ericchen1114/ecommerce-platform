<template>
  <div>
    <h2 style="margin-bottom:24px;color:#1a1a2e">📊 數據看板</h2>
    <el-row :gutter="16" style="margin-bottom:24px">
      <el-col :span="6" v-for="s in stats" :key="s.label">
        <el-card shadow="hover" style="border-radius:12px">
          <div style="display:flex;align-items:center;gap:16px">
            <div :style="`width:56px;height:56px;border-radius:12px;background:${s.bg};display:flex;align-items:center;justify-content:center`">
              <el-icon :size="28" color="#fff"><component :is="s.icon" /></el-icon>
            </div>
            <div>
              <p style="color:#999;font-size:13px;margin:0 0 4px">{{ s.label }}</p>
              <p style="font-size:22px;font-weight:bold;margin:0;color:#1a1a2e">{{ s.value }}</p>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
    <el-row :gutter="16">
      <el-col :span="14">
        <el-card shadow="hover" style="border-radius:12px">
          <h3 style="margin-bottom:16px">最近 7 天訂單量</h3>
          <div ref="barRef" style="height:260px"></div>
        </el-card>
      </el-col>
      <el-col :span="10">
        <el-card shadow="hover" style="border-radius:12px">
          <h3 style="margin-bottom:16px">訂單狀態分佈</h3>
          <div ref="pieRef" style="height:260px"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>
<script setup>
import { ref, onMounted } from 'vue'
import * as echarts from 'echarts'
const barRef = ref(), pieRef = ref()
const stats = [
  { label:'今日訂單', value:'128',      bg:'#409eff', icon:'List' },
  { label:'今日營收', value:'NT$48,230', bg:'#67c23a', icon:'Money' },
  { label:'商品總數', value:'356',       bg:'#e6a23c', icon:'Goods' },
  { label:'會員總數', value:'2,841',     bg:'#f56c6c', icon:'User' },
]
onMounted(() => {
  echarts.init(barRef.value).setOption({
    tooltip: { trigger:'axis' },
    xAxis: { type:'category', data:['Mon','Tue','Wed','Thu','Fri','Sat','Sun'] },
    yAxis: { type:'value' },
    series: [{ data:[82,93,110,134,120,150,128], type:'bar', color:'#409eff',
      itemStyle:{ borderRadius:[4,4,0,0] } }]
  })
  echarts.init(pieRef.value).setOption({
    tooltip: { trigger:'item' },
    legend: { bottom:0 },
    series: [{ type:'pie', radius:['40%','65%'],
      data:[
        { value:45, name:'待付款', itemStyle:{color:'#e6a23c'} },
        { value:28, name:'處理中', itemStyle:{color:'#409eff'} },
        { value:120,name:'已完成', itemStyle:{color:'#67c23a'} },
        { value:8,  name:'已取消', itemStyle:{color:'#f56c6c'} },
      ]
    }]
  })
})
</script>
