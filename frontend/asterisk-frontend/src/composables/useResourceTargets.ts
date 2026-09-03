import { computed, ref, watch, type ComputedRef, type Ref } from 'vue'
import { errorMessage, type RecordData } from '@/api/platform'
import { resourceService } from '@/services/resource.service'

interface TargetOptions {
  key: string
  parentId: string
  form: RecordData
  scope: ComputedRef<number | undefined>
  ready: Ref<boolean>
}

export function useResourceTargets({ key, parentId, form, scope, ready }: TargetOptions) {
  const targets = ref<RecordData[]>([])
  const lookupError = ref('')
  const lookupLoading = ref(false)
  let version = 0
  const resource = computed(() => key === 'members' ? 'endpoints' : ({
    ENDPOINT: 'endpoints', TRUNK: 'trunks', QUEUE: 'queues', IVR: 'ivrs', EXTENSION: 'extensions',
  }[String(form.targetType || form.actionType)]))

  async function loadTargets() {
    const requestVersion = ++version
    targets.value = []
    lookupError.value = ''
    lookupLoading.value = false
    if (!resource.value || !scope.value) return
    lookupLoading.value = true
    try {
      const records = await resourceService.all(`/${resource.value}`, { tenantId: scope.value })
      if (requestVersion !== version) return
      targets.value = records.filter(row => !(key === 'options' && form.actionType === 'IVR' && String(row.id) === parentId))
    } catch (cause) {
      if (requestVersion === version) lookupError.value = errorMessage(cause)
    } finally {
      if (requestVersion === version) lookupLoading.value = false
    }
  }

  watch([resource, scope], async () => {
    if (!ready.value) return
    form.targetId = ''
    form.endpointId = ''
    await loadTargets()
  })

  return { targets, lookupError, lookupLoading, loadTargets }
}
