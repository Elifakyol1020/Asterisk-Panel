import { ref } from 'vue'
import { defineStore } from 'pinia'
import type { Tenant } from '@/types/tenant'

export const useTenantStore = defineStore('tenant', () => {
  const currentTenant = ref<Tenant | null>(null)
  function setCurrentTenant(tenant: Tenant) { currentTenant.value = tenant }
  function clearCurrentTenant() { currentTenant.value = null }
  return { currentTenant, setCurrentTenant, clearCurrentTenant }
})
