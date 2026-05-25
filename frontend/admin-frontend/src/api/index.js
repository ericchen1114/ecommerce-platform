import axios from 'axios'
const api = axios.create({ baseURL: '/api', timeout: 10000 })
api.interceptors.request.use(config => {
  const token = localStorage.getItem('admin_token')
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})
export const adminApi = {
  login:         (d)     => api.post('/users/login', d).then(r => r.data),
  getProducts:   (p)     => api.get('/products', { params: p }).then(r => r.data),
  createProduct: (d)     => api.post('/products', d).then(r => r.data),
  updateProduct: (id, d) => api.put(`/products/${id}`, d).then(r => r.data),
  deleteProduct: (id)    => api.delete(`/products/${id}`).then(r => r.data),
  getOrders:     (p)     => api.get('/orders', { params: p }).then(r => r.data),
  updateOrder:   (id, d) => api.put(`/orders/${id}`, d).then(r => r.data),
  getUsers:      (p)     => api.get('/users', { params: p }).then(r => r.data),
}
export default api
