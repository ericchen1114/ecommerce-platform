import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { userApi } from '@/api'

export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem('token') || '')
  const user  = ref(JSON.parse(localStorage.getItem('user') || 'null'))

  const isLoggedIn  = computed(() => !!token.value)
  const memberName  = computed(() => user.value?.fullName || '')
  const memberId    = computed(() => user.value?.memberId || '')

  /** 判斷是否已是會員 */
  async function check(identifier) {
    const data = await userApi.check(identifier)
    return data?.exists ?? false
  }

  /** 登入 */
  async function login(identifier, password) {
    const data = await userApi.login({ identifier, password })
    _saveSession(data)
  }

  /** 註冊 */
  async function register(form) {
    const data = await userApi.register(form)
    _saveSession(data)
  }

  /** 登出 */
  function logout() {
    token.value = ''
    user.value  = null
    localStorage.removeItem('token')
    localStorage.removeItem('user')
  }

  function _saveSession(data) {
    token.value = data.token
    user.value  = data.user
    localStorage.setItem('token', data.token)
    localStorage.setItem('user', JSON.stringify(data.user))
  }

  return { token, user, isLoggedIn, memberName, memberId, check, login, register, logout }
})
