<script setup lang="ts">
import { ref } from 'vue'
import { dialplanResource as config } from '@/config/resources/dialplans'
import { useResourceForm } from '@/composables/useResourceForm'
import PageHeader from '@/components/common/PageHeader.vue'
import InlineFeedback from '@/components/common/InlineFeedback.vue'
import EmptyState from '@/components/common/EmptyState.vue'
import TenantSelect from '@/components/forms/TenantSelect.vue'
import FormActions from '@/components/forms/FormActions.vue'
import api from '@/api/axios'
import { errorMessage } from '@/api/platform'

type Step = { application: string; applicationData: string }
const applications = ['Answer', 'Playback', 'Wait', 'Hangup']
const steps = ref<Step[]>([
  { application: 'Answer', applicationData: '' },
  { application: 'Playback', applicationData: '' },
  { application: 'Hangup', applicationData: '' },
])
const context = useResourceForm(config, 'create')
const { basePath, tenantId, tenants, form, loading, saving, ready, error, tenantRequired, scope, url, initialize, router } = context

function addStep() { steps.value.push({ application: 'Wait', applicationData: '' }) }
function removeStep(index: number) { if (steps.value.length > 1) steps.value.splice(index, 1) }
function move(index: number, delta: number) {
  const target = index + delta
  if (target < 0 || target >= steps.value.length) return
  const [step] = steps.value.splice(index, 1)
  steps.value.splice(target, 0, step!)
}
function requiresData(application: string) { return application === 'Playback' || application === 'Wait' }

async function saveFlow() {
  if (saving.value || !ready.value) return
  if (!scope.value) { error.value = 'Önce bir tenant seçin.'; return }
  if (!/^\d{1,20}$/.test(String(form.extension || ''))) { error.value = 'Geçerli bir dahili numarası girin.'; return }
  if (steps.value.some(step => requiresData(step.application) && !step.applicationData)) {
    error.value = 'Playback ve Wait işlemleri için parametre zorunludur.'; return
  }
  saving.value = true
  error.value = ''
  try {
    await api.post('/dialplans/flow', {
      tenantId: scope.value,
      extension: String(form.extension),
      enabled: Boolean(form.enabled),
      steps: steps.value.map(step => ({
        application: step.application,
        applicationData: requiresData(step.application) ? step.applicationData : '',
      })),
    })
    const extensionsPath = basePath.replace(/\/dialplans$/, '/extensions')
    await router.push({ ...url(extensionsPath), query: { ...url(extensionsPath).query, saved: '1' } })
  } catch (cause) {
    error.value = errorMessage(cause)
  } finally {
    saving.value = false
  }
}
</script>

<template>
  <PageHeader title="Gelişmiş çağrı akışı" description="Bir dahili numarası için sıralı Answer, Playback, Wait ve Hangup işlemleri tanımlayın.">
    <RouterLink :to="url(basePath.replace(/\/dialplans$/, '/extensions'))" class="button">Dahililere dön</RouterLink>
  </PageHeader>
  <InlineFeedback :error="error" :retry="!ready && !loading" @retry="initialize" />
  <EmptyState v-if="loading" loading />
  <div v-else-if="ready" class="form-layout">
    <form class="form-panel" @submit.prevent="saveFlow">
      <h2>Çağrı akışı</h2>
      <div class="form-grid">
        <label v-if="tenantRequired" class="full">Tenant *
          <TenantSelect v-model="tenantId" :tenants="tenants" required :disabled="saving" />
        </label>
        <label>Dahili numarası *<input v-model="form.extension" required pattern="[0-9]{1,20}" /></label>
        <label class="checkbox"><input v-model="form.enabled" type="checkbox" /> Aktif</label>
      </div>
      <h3>İşlem sırası</h3>
      <div v-for="(step, index) in steps" :key="index" class="form-grid">
        <label>Öncelik<input :value="index + 1" type="number" disabled /></label>
        <label>Uygulama *
          <select v-model="step.application"><option v-for="app in applications" :key="app">{{ app }}</option></select>
        </label>
        <label class="full">Parametre
          <input v-model="step.applicationData" :required="requiresData(step.application)" :disabled="!requiresData(step.application)" :placeholder="step.application === 'Playback' ? 'Örn. custom/tenant1/...' : step.application === 'Wait' ? '0–300 saniye' : 'Bu işlem parametre almaz'" />
        </label>
        <div class="table-actions full">
          <button type="button" class="button" :disabled="index === 0" @click="move(index, -1)">Yukarı</button>
          <button type="button" class="button" :disabled="index === steps.length - 1" @click="move(index, 1)">Aşağı</button>
          <button type="button" class="button danger" :disabled="steps.length === 1" @click="removeStep(index)">Kaldır</button>
        </div>
      </div>
      <button type="button" class="button" @click="addStep">+ İşlem ekle</button>
      <FormActions :cancel-to="url(basePath.replace(/\/dialplans$/, '/extensions'))" :saving="saving" />
    </form>
    <aside class="form-aside"><h3>Nasıl çalışır?</h3><p>Her işlem aynı extension için ayrı priority satırı olarak realtime <code>extensions</code> tablosuna yazılır.</p></aside>
  </div>
</template>
