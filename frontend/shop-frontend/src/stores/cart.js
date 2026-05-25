import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
export const useCartStore = defineStore('cart', () => {
  const items = ref([])
  const total = computed(() => items.value.reduce((s, i) => s + i.price * i.quantity, 0))
  const count = computed(() => items.value.reduce((s, i) => s + i.quantity, 0))
  function addItem(product, quantity = 1) {
    const ex = items.value.find(i => i.id === product.id)
    if (ex) ex.quantity += quantity
    else items.value.push({ ...product, quantity })
  }
  function removeItem(id) { items.value = items.value.filter(i => i.id !== id) }
  function clear() { items.value = [] }
  return { items, total, count, addItem, removeItem, clear }
})
