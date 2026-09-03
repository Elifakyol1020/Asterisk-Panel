import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth.store'
import { resourceService } from '@/services/resource.service'
import type { RecordData } from '@/api/platform'
import type { ResourceConfig } from '@/types/resource'

/** Tenant scope and parent paths shared by list/form workflows; never renders a page. */
export function useResourceContext(config: ResourceConfig) {
  const route = useRoute()
  const router = useRouter()
  const auth = useAuthStore()
  const key = config.key
  const nested = key === 'members' || key === 'options'
  const prefix = auth.isSuperAdmin ? '/super-admin' : '/tenant'
  const parentId = String(route.params.parentId || '')
  const parentResource = key === 'members' ? 'queues' : 'ivrs'
  const basePath = nested ? `${prefix}/${parentResource}/${parentId}/${key}` : `${prefix}/${key}`
  const apiPath = nested ? `/${parentResource}/${parentId}/${key}` : config.api
  const tenantId = ref(String((auth.isTenantAdmin ? auth.user?.tenantId : route.query.tenantId) || ''))
  const tenants = ref<RecordData[]>([])
  const parent = ref<RecordData | null>(null)
  const scope = computed(() => Number(parent.value?.tenantId || tenantId.value) || undefined)

  function url(path: string) {
    return { path, query: tenantId.value && auth.isSuperAdmin ? { tenantId: tenantId.value } : {} }
  }

  async function loadContext() {
    if (auth.isSuperAdmin && key !== 'tenants' && !nested) {
      tenants.value = await resourceService.all('/admin/tenants', { sort: 'name,asc' })
    }
    if (nested) parent.value = await resourceService.get(`/${parentResource}/${parentId}`)
  }

  return {
    route, router, auth, config, key, nested, prefix, parentId, parentResource,
    basePath, apiPath, tenantId, tenants, parent, scope, url, loadContext,
  }
}
