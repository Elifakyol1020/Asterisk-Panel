import { onMounted, ref } from 'vue'
import { errorMessage, listPage, type RecordData } from '@/api/platform'
import { resources } from '@/config/resources'

/** Fetches configuration counts; dashboard composition belongs to each role's view. */
export function useDashboardData(primaryKey: string, resourceKeys: string[]) {
  const loading = ref(false)
  const errors = ref<string[]>([])
  const totals = ref<Record<string, number>>({})
  const rows = ref<RecordData[]>([])
  const updatedAt = ref('')

  async function load() {
    if (loading.value) return
    loading.value = true
    errors.value = []
    totals.value = {}
    rows.value = []
    await Promise.all(resourceKeys.map(async key => {
      try {
        const result = await listPage(resources[key]!.api, { size: key === primaryKey ? 6 : 1 })
        totals.value[key] = result.totalElements
        if (key === primaryKey) rows.value = result.content
      } catch (cause) {
        errors.value.push(`${resources[key]!.title}: ${errorMessage(cause)}`)
      }
    }))
    updatedAt.value = new Date().toLocaleTimeString('tr-TR', { hour: '2-digit', minute: '2-digit' })
    loading.value = false
  }

  onMounted(load)
  return { loading, errors, totals, rows, updatedAt, load }
}
