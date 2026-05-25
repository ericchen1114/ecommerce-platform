<template>
  <div class="auth-bg">
    <el-card class="auth-card" shadow="always">

      <!-- Logo / 標題 -->
      <div class="auth-header">
        <div class="auth-logo">🛍️</div>
        <h2 class="auth-title">歡迎光臨</h2>
        <p class="auth-subtitle">{{ stepSubtitle }}</p>
      </div>

      <!-- 步驟條 -->
      <el-steps :active="stepIndex" finish-status="success" align-center class="auth-steps">
        <el-step title="輸入帳號" />
        <el-step :title="isExisting ? '輸入密碼' : '填寫資料'" />
      </el-steps>

      <!-- ══════════════════════════════════════════════ -->
      <!-- STEP 0：輸入手機號 / Email                    -->
      <!-- ══════════════════════════════════════════════ -->
      <el-form
        v-if="step === 'identifier'"
        ref="identifierFormRef"
        :model="identifierForm"
        :rules="identifierRules"
        label-position="top"
        class="auth-form"
        @submit.prevent="handleCheck"
      >
        <el-form-item label="手機號碼或 Email" prop="identifier">
          <el-input
            v-model="identifierForm.identifier"
            size="large"
            placeholder="09XXXXXXXX 或 email@example.com"
            :prefix-icon="User"
            clearable
            autofocus
          />
        </el-form-item>

        <el-button
          type="primary"
          size="large"
          native-type="submit"
          :loading="loading"
          class="auth-btn"
        >
          下一步
        </el-button>
      </el-form>

      <!-- ══════════════════════════════════════════════ -->
      <!-- STEP 1a：已是會員 → 輸入密碼登入              -->
      <!-- ══════════════════════════════════════════════ -->
      <el-form
        v-else-if="step === 'login'"
        ref="loginFormRef"
        :model="loginForm"
        :rules="loginRules"
        label-position="top"
        class="auth-form"
        @submit.prevent="handleLogin"
      >
        <!-- 顯示已輸入的帳號（唯讀）-->
        <el-form-item label="帳號">
          <el-input :model-value="identifierForm.identifier" size="large" disabled>
            <template #prefix><el-icon><User /></el-icon></template>
          </el-input>
        </el-form-item>

        <el-form-item label="密碼" prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            size="large"
            placeholder="請輸入密碼"
            :prefix-icon="Lock"
            show-password
            autofocus
          />
        </el-form-item>

        <div class="auth-btn-row">
          <el-button size="large" @click="resetStep" :icon="ArrowLeft">返回</el-button>
          <el-button
            type="primary"
            size="large"
            native-type="submit"
            :loading="loading"
            class="auth-btn-flex"
          >
            登入
          </el-button>
        </div>
      </el-form>

      <!-- ══════════════════════════════════════════════ -->
      <!-- STEP 1b：非會員 → 填寫資料並註冊              -->
      <!-- ══════════════════════════════════════════════ -->
      <el-form
        v-else-if="step === 'register'"
        ref="registerFormRef"
        :model="registerForm"
        :rules="registerRules"
        label-position="top"
        class="auth-form"
        @submit.prevent="handleRegister"
      >
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="真實姓名" prop="fullName">
              <el-input v-model="registerForm.fullName" size="large" placeholder="請輸入真實姓名" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="手機號碼" prop="phone">
              <el-input
                v-model="registerForm.phone"
                size="large"
                placeholder="09XXXXXXXX"
                :disabled="isPhoneIdentifier"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="Email" prop="email">
          <el-input
            v-model="registerForm.email"
            size="large"
            placeholder="選填"
            :disabled="!isPhoneIdentifier"
          />
        </el-form-item>

        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="出生年月日" prop="birthday">
              <el-date-picker
                v-model="registerForm.birthday"
                type="date"
                size="large"
                placeholder="選擇日期"
                format="YYYY/MM/DD"
                value-format="YYYY-MM-DD"
                :disabled-date="disableFutureDates"
                style="width:100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="密碼" prop="password">
              <el-input
                v-model="registerForm.password"
                type="password"
                size="large"
                placeholder="至少 8 碼"
                show-password
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="地址" prop="address">
          <el-input
            v-model="registerForm.address"
            size="large"
            placeholder="縣市 + 鄉鎮 + 詳細地址"
          />
        </el-form-item>

        <div class="auth-btn-row">
          <el-button size="large" @click="resetStep" :icon="ArrowLeft">返回</el-button>
          <el-button
            type="primary"
            size="large"
            native-type="submit"
            :loading="loading"
            class="auth-btn-flex"
          >
            立即註冊
          </el-button>
        </div>
      </el-form>

    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock, ArrowLeft } from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'

const auth    = useAuthStore()
const router  = useRouter()
const loading = ref(false)

// ── 步驟狀態 ────────────────────────────────────────────────────
const step       = ref('identifier')   // 'identifier' | 'login' | 'register'
const isExisting = ref(false)

const stepIndex = computed(() => step.value === 'identifier' ? 0 : 1)
const stepSubtitle = computed(() => {
  if (step.value === 'identifier') return '請輸入您的手機號碼或 Email'
  if (step.value === 'login')      return '歡迎回來！請輸入您的密碼'
  return '填寫以下資料完成會員註冊'
})

// ── 識別符表單 ──────────────────────────────────────────────────
const identifierFormRef = ref()
const identifierForm    = reactive({ identifier: '' })
const identifierRules   = {
  identifier: [
    { required: true, message: '請輸入手機號碼或 Email', trigger: 'blur' },
    {
      validator: (rule, value, cb) => {
        const isPhone = /^09\d{8}$/.test(value)
        const isEmail = /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value)
        if (!isPhone && !isEmail) cb(new Error('請輸入正確的手機號碼（09XXXXXXXX）或 Email'))
        else cb()
      },
      trigger: 'blur'
    }
  ]
}

// ── 登入表單 ────────────────────────────────────────────────────
const loginFormRef = ref()
const loginForm    = reactive({ password: '' })
const loginRules   = {
  password: [{ required: true, message: '請輸入密碼', trigger: 'blur' }]
}

// ── 註冊表單 ────────────────────────────────────────────────────
const registerFormRef = ref()
const isPhoneIdentifier = computed(() => /^09\d{8}$/.test(identifierForm.identifier))
const registerForm = reactive({
  fullName: '',
  phone:    '',
  email:    '',
  birthday: '',
  address:  '',
  password: ''
})
const registerRules = {
  fullName: [{ required: true, message: '請輸入真實姓名', trigger: 'blur' }],
  phone:    [
    { required: true, message: '請輸入手機號碼', trigger: 'blur' },
    { pattern: /^09\d{8}$/, message: '手機號格式錯誤（09XXXXXXXX）', trigger: 'blur' }
  ],
  email:    [{ type: 'email', message: 'Email 格式不正確', trigger: 'blur' }],
  birthday: [{ required: true, message: '請選擇出生年月日', trigger: 'change' }],
  address:  [{ required: true, message: '請輸入地址', trigger: 'blur' }],
  password: [
    { required: true, message: '請設定密碼', trigger: 'blur' },
    { min: 8, message: '密碼至少 8 個字元', trigger: 'blur' }
  ]
}

function disableFutureDates(date) {
  return date > new Date()
}

// ── 步驟 0：檢查是否為會員 ────────────────────────────────────
async function handleCheck() {
  await identifierFormRef.value.validate()
  loading.value = true
  try {
    const exists = await auth.check(identifierForm.identifier)
    isExisting.value = exists
    if (exists) {
      step.value = 'login'
    } else {
      // 預填手機號或 Email
      if (isPhoneIdentifier.value) {
        registerForm.phone = identifierForm.identifier
        registerForm.email = ''
      } else {
        registerForm.email  = identifierForm.identifier
        registerForm.phone  = ''
      }
      step.value = 'register'
    }
  } catch (e) {
    ElMessage.error(e.message || '查詢失敗，請稍後再試')
  } finally {
    loading.value = false
  }
}

// ── 步驟 1a：登入 ─────────────────────────────────────────────
async function handleLogin() {
  await loginFormRef.value.validate()
  loading.value = true
  try {
    await auth.login(identifierForm.identifier, loginForm.password)
    ElMessage.success(`歡迎回來，${auth.memberName}！`)
    router.push('/')
  } catch (e) {
    ElMessage.error(e.message || '帳號或密碼錯誤')
  } finally {
    loading.value = false
  }
}

// ── 步驟 1b：註冊 ─────────────────────────────────────────────
async function handleRegister() {
  await registerFormRef.value.validate()
  loading.value = true
  try {
    await auth.register({ ...registerForm })
    ElMessage.success(`註冊成功！歡迎加入，${auth.memberName}！`)
    router.push('/')
  } catch (e) {
    ElMessage.error(e.message || '註冊失敗，請稍後再試')
  } finally {
    loading.value = false
  }
}

// ── 返回步驟 0 ────────────────────────────────────────────────
function resetStep() {
  step.value       = 'identifier'
  isExisting.value = false
  loginForm.password = ''
}
</script>

<style scoped>
.auth-bg {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 24px;
}

.auth-card {
  width: 100%;
  max-width: 480px;
  border-radius: 16px;
  padding: 8px;
}

.auth-header {
  text-align: center;
  margin-bottom: 24px;
}

.auth-logo {
  font-size: 48px;
  margin-bottom: 8px;
}

.auth-title {
  font-size: 24px;
  font-weight: 700;
  color: #303133;
  margin: 0 0 6px;
}

.auth-subtitle {
  color: #909399;
  font-size: 14px;
  margin: 0;
}

.auth-steps {
  margin-bottom: 28px;
}

.auth-form {
  margin-top: 8px;
}

.auth-btn {
  width: 100%;
  height: 44px;
  font-size: 16px;
  margin-top: 4px;
}

.auth-btn-row {
  display: flex;
  gap: 12px;
  margin-top: 4px;
}

.auth-btn-flex {
  flex: 1;
  height: 44px;
  font-size: 16px;
}
</style>
