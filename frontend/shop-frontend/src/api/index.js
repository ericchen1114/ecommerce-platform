import axios from 'axios'

const api = axios.create({ baseURL: '/api', timeout: 10000 })

// ── Request 攔截：自動帶入 JWT ────────────────────────────────────
api.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

// ── Response 攔截：解包 Result<T>，401 自動導向登入 ──────────────
api.interceptors.response.use(
  res => {
    const result = res.data
    // 成功：直接返回 data 欄位
    if (result?.code === '0000') return result.data
    // 業務錯誤：拋出帶 code 的 Error
    const err = new Error(result?.message || '請求失敗')
    err.code = result?.code
    return Promise.reject(err)
  },
  err => {
    if (err.response?.status === 401) {
      localStorage.removeItem('token')
      window.location.href = '/login'
    }
    // HTTP 層錯誤也嘗試解析 Result
    const result = err.response?.data
    if (result?.code && result?.message) {
      const e = new Error(result.message)
      e.code = result.code
      return Promise.reject(e)
    }
    return Promise.reject(err)
  }
)

// ── User API ─────────────────────────────────────────────────────
export const userApi = {
  /** 判斷手機號或 Email 是否已是會員，返回 { exists: boolean } */
  check:    (identifier) => api.post('/users/check',    { identifier }),
  /** 登入，返回 AuthResponse { token, user } */
  login:    (d)          => api.post('/users/login',    d),
  /** 註冊，返回 AuthResponse { token, user } */
  register: (d)          => api.post('/users/register', d),
  /** 取得當前會員資料 */
  profile:  ()           => api.get('/users/me'),
}

export const productApi = {
  list:   (p)  => api.get('/products', { params: p }),
  detail: (id) => api.get(`/products/${id}`),
}

export const cartApi = {
  get:    ()       => api.get('/cart'),
  add:    (d)      => api.post('/cart/items', d),
  remove: (itemId) => api.delete(`/cart/items/${itemId}`),
}

export const orderApi = {
  create: (d)  => api.post('/orders', d),
  list:   ()   => api.get('/orders'),
  detail: (id) => api.get(`/orders/${id}`),
}

export default api
