import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import { authService } from '@/services/auth.service'
import { readClaims } from '@/utils/session'
import type { LoginRequest } from '@/types/auth'
export const useAuthStore = defineStore('auth', () => {
  const token = ref(sessionStorage.getItem('auth_token'))
  const user = ref(token.value ? readClaims(token.value, sessionStorage.getItem('auth_email') || '') : null)
  const isAuthenticated = computed(() => Boolean(token.value && user.value && user.value.exp * 1000 > Date.now()))
  const role = computed(() => user.value?.role ?? null)
  const isSuperAdmin = computed(() => role.value === 'SUPER_ADMIN')
  const isTenantAdmin = computed(() => role.value === 'TENANT_ADMIN')
  let expiryTimer: ReturnType<typeof setTimeout> | undefined
  function scheduleExpiry() {
    clearTimeout(expiryTimer)
    if (user.value) expiryTimer = setTimeout(() => {
      if (user.value && user.value.exp * 1000 > Date.now()) { scheduleExpiry(); return }
      logout(); window.location.assign('/login?expired=1')
    }, Math.min(user.value.exp * 1000 - Date.now(), 2147483647))
  }
  async function login(payload: LoginRequest) {
    const response = await authService.login(payload)
    const claims = readClaims(response.accessToken, payload.email)
    if (!claims) throw new Error('Sunucudan geçerli bir oturum bilgisi alınamadı.')
    token.value = response.accessToken; user.value = claims
    sessionStorage.setItem('auth_token', response.accessToken); sessionStorage.setItem('auth_email', payload.email)
    scheduleExpiry()
  }
  function logout() {
    clearTimeout(expiryTimer); token.value = null; user.value = null
    sessionStorage.removeItem('auth_token'); sessionStorage.removeItem('auth_email')
    localStorage.removeItem('auth_token'); localStorage.removeItem('auth_user')
  }
  if (!user.value) logout(); else scheduleExpiry()
  return { token, user, isAuthenticated, role, isSuperAdmin, isTenantAdmin, login, logout }
})
