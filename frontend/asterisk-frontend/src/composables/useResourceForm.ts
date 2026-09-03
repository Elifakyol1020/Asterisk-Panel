import { computed, nextTick, onMounted, reactive, ref } from 'vue'
import { onBeforeRouteLeave } from 'vue-router'
import { errorMessage, fieldErrors, type RecordData } from '@/api/platform'
import { resourceService } from '@/services/resource.service'
import { buildPayload, validatePayload } from '@/utils/resourceForm'
import { useResourceContext } from './useResourceContext'
import { useResourceTargets } from './useResourceTargets'
import type { ResourceConfig } from '@/types/resource'

export function useResourceForm(config: ResourceConfig, mode: 'create' | 'edit') {
  const context = useResourceContext(config)
  const { key, auth, route, router, tenantId, nested, apiPath, basePath, url } = context
  const editing = mode === 'edit'
  const form = reactive<RecordData>({})
  const loadedRecord = ref<RecordData | null>(null)
  const loading = ref(false)
  const saving = ref(false)
  const ready = ref(false)
  const error = ref('')
  const validation = ref<Record<string, string>>({})
  const tenantRequired = auth.isSuperAdmin && key !== 'tenants' && !nested
  const scope = computed(() => Number(loadedRecord.value?.tenantId) || context.scope.value)
  const fields = computed(() => config.fields.filter(field => !(field.key === 'targetId' && form.actionType === 'HANGUP')))
  const targets = useResourceTargets({ key, parentId: context.parentId, form, scope, ready })
  let initialForm = ''

  async function initialize() {
    loading.value = true
    error.value = ''
    ready.value = false
    try {
      await context.loadContext()
      config.fields.forEach(field => { form[field.key] = field.default ?? (field.type === 'checkbox' ? false : '') })
      if (editing) {
        const record = nested
          ? (await resourceService.all(apiPath)).find(item => String(item.id) === String(route.params.id))
          : await resourceService.get(`${apiPath}/${route.params.id}`)
        if (!record) throw new Error('Düzenlenecek kayıt bulunamadı.')
        loadedRecord.value = record
        config.fields.forEach(field => {
          if (field.key !== 'password') form[field.key] = record[field.key] ?? form[field.key]
        })
        if (record.tenantId) tenantId.value = String(record.tenantId)
      }
      await targets.loadTargets()
      initialForm = JSON.stringify(form)
      await nextTick()
      ready.value = true
    } catch (cause) {
      error.value = errorMessage(cause)
    } finally {
      loading.value = false
    }
  }

  async function save() {
    if (saving.value || !ready.value) return
    error.value = ''
    const data = buildPayload(fields.value, form, editing, key, auth.isSuperAdmin, scope.value)
    validation.value = validatePayload(data, key)
    if (Object.keys(validation.value).length) return
    if (tenantRequired && !scope.value) { error.value = 'Önce bir tenant seçin.'; return }
    saving.value = true
    try {
      if (editing) await resourceService.update(`${apiPath}/${route.params.id}`, data)
      else await resourceService.create(key === 'users' ? `/admin/tenants/${tenantId.value}/users` : apiPath, data)
      initialForm = JSON.stringify(form)
      await router.push({ ...url(basePath), query: { ...url(basePath).query, saved: '1' } })
    } catch (cause) {
      error.value = errorMessage(cause)
      validation.value = fieldErrors(cause)
    } finally {
      saving.value = false
    }
  }

  onBeforeRouteLeave(() => {
    if (ready.value && JSON.stringify(form) !== initialForm && !saving.value) {
      return window.confirm('Kaydedilmemiş değişiklikleriniz var. Bu sayfadan ayrılmak istiyor musunuz?')
    }
  })
  onMounted(initialize)

  return {
    ...context, ...targets, editing, form, loadedRecord, loading, saving, ready,
    error, validation, tenantRequired, scope, initialize, save,
  }
}
