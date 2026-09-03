import { computed, onMounted, ref, watch } from 'vue'
import { errorMessage, type RecordData } from '@/api/platform'
import { resourceService } from '@/services/resource.service'
import { useResourceContext } from './useResourceContext'
import type { ResourceConfig } from '@/types/resource'

/** Pagination, filtering and delete lifecycle; each page owns its table and actions. */
export function useResourceList(config: ResourceConfig) {
  const context = useResourceContext(config)
  const { key, auth, tenantId, nested, apiPath, router, url, basePath } = context
  const rows = ref<RecordData[]>([])
  const loading = ref(false)
  const deleting = ref(false)
  const ready = ref(false)
  const error = ref('')
  const success = ref('')
  const page = ref(0)
  const total = ref(0)
  const totalPages = ref(0)
  const search = ref('')
  const sort = ref('id,desc')
  const deleteTarget = ref<RecordData | null>(null)
  let requestVersion = 0

  const visibleRows = computed(() => rows.value.filter(row =>
    Object.values(row).join(' ').toLocaleLowerCase('tr-TR').includes(search.value.toLocaleLowerCase('tr-TR')),
  ))

  async function loadList() {
    const version = ++requestVersion
    error.value = ''
    rows.value = []
    total.value = 0
    totalPages.value = 0
    if (key === 'users' && !tenantId.value) {
      loading.value = false
      return
    }
    loading.value = true
    try {
      const path = key === 'users' ? `/admin/tenants/${tenantId.value}/users` : apiPath
      const params = { page: page.value, size: 10, sort: sort.value }
      const scoped = auth.isSuperAdmin && tenantId.value && !['tenants', 'users'].includes(key) && !nested
      const data = await resourceService.list(path, { ...params, ...(scoped ? { tenantId: tenantId.value } : {}) })
      if (version !== requestVersion) return
      rows.value = data.content
      total.value = data.totalElements
      totalPages.value = data.totalPages
      if (!data.content.length && page.value > 0) {
        page.value--
        await loadList()
      }
    } catch (cause) {
      if (version === requestVersion) error.value = errorMessage(cause)
    } finally {
      if (version === requestVersion) loading.value = false
    }
  }

  async function initialize() {
    ready.value = false
    loading.value = true
    error.value = ''
    try {
      await context.loadContext()
      await loadList()
      ready.value = true
    } catch (cause) {
      error.value = errorMessage(cause)
    } finally {
      loading.value = false
    }
  }

  async function remove() {
    if (!deleteTarget.value || deleting.value) return
    deleting.value = true
    error.value = ''
    success.value = ''
    try {
      await resourceService.remove(`${apiPath}/${deleteTarget.value.id}`)
      success.value = config.softDelete ? 'Kayıt pasifleştirildi.' : 'Kayıt silindi.'
      deleteTarget.value = null
      await loadList()
    } catch (cause) {
      deleteTarget.value = null
      error.value = errorMessage(cause)
    } finally {
      deleting.value = false
    }
  }

  async function changePage(offset: number) {
    page.value += offset
    search.value = ''
    await loadList()
  }

  watch(tenantId, async () => {
    if (!ready.value) return
    page.value = 0
    await router.replace(url(basePath))
    await loadList()
  })
  watch(sort, () => { page.value = 0; loadList() })
  onMounted(initialize)

  return {
    ...context, rows, visibleRows, loading, deleting, ready, error, success, page,
    total, totalPages, search, sort, deleteTarget, initialize, loadList, remove, changePage,
  }
}
