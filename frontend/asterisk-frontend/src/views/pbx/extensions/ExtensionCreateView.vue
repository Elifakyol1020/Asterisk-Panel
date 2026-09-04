<script setup lang="ts">
import { ref } from 'vue'
import { extensionResource as config } from '@/config/resources/extensions'
import { useResourceForm } from '@/composables/useResourceForm'
import ExtensionFields from '@/components/extensions/ExtensionFields.vue'
import PageHeader from '@/components/common/PageHeader.vue'
import InlineFeedback from '@/components/common/InlineFeedback.vue'
import EmptyState from '@/components/common/EmptyState.vue'
import AppIcon from '@/components/common/AppIcon.vue'
import TenantSelect from '@/components/forms/TenantSelect.vue'
import FormActions from '@/components/forms/FormActions.vue'
import api from '@/api/axios'
import { errorMessage } from '@/api/platform'

type Step = { application: string; applicationData: string }
const mode = ref<'target' | 'flow'>('target')
const applications = ['Answer', 'Playback', 'Wait', 'Hangup']
const steps = ref<Step[]>([
  { application: 'Answer', applicationData: '' },
  { application: 'Playback', applicationData: '' },
  { application: 'Hangup', applicationData: '' },
])

const {
  basePath,
  tenantId,
  tenants,
  form,
  validation,
  loading,
  saving,
  ready,
  error,
  tenantRequired,
  targets,
  lookupLoading,
  lookupError,
  scope,
  url,
  initialize,
  save,
  loadTargets,
  router,
} = useResourceForm(config, 'create')

function addStep() { steps.value.push({ application: 'Wait', applicationData: '' }) }
function removeStep(index: number) { if (steps.value.length > 1) steps.value.splice(index, 1) }
function move(index: number, delta: number) {
  const target = index + delta
  if (target < 0 || target >= steps.value.length) return
  const [step] = steps.value.splice(index, 1)
  steps.value.splice(target, 0, step!)
}
function requiresData(application: string) { return application === 'Playback' || application === 'Wait' }

async function submit() {
  if (mode.value === 'target') { await save(); return }
  if (saving.value || !ready.value) return
  if (!scope.value) { error.value = 'Önce bir tenant seçin.'; return }
  if (!/^\d{1,20}$/.test(String(form.extensionNumber || ''))) { error.value = 'Geçerli bir dahili numarası girin.'; return }
  if (steps.value.some(step => requiresData(step.application) && !step.applicationData)) {
    error.value = 'Playback ve Wait işlemleri için parametre zorunludur.'; return
  }
  saving.value = true
  error.value = ''
  try {
    await api.post('/dialplans/flow', {
      tenantId: scope.value,
      extension: String(form.extensionNumber),
      enabled: Boolean(form.enabled),
      steps: steps.value.map(step => ({ application: step.application, applicationData: requiresData(step.application) ? step.applicationData : '' })),
    })
    await router.push({ ...url(basePath), query: { ...url(basePath).query, saved: '1' } })
  } catch (cause) { error.value = errorMessage(cause) }
  finally { saving.value = false }
}
</script>

<template>
  <PageHeader title="Dahili oluştur" :description="config.description">
    <RouterLink :to="url(basePath)" class="button">Listeye dön</RouterLink>
  </PageHeader>
  <InlineFeedback :error="error" :retry="!ready && !loading" @retry="initialize" />
  <EmptyState v-if="loading" loading />
  <div v-else-if="ready" class="form-layout">
    <form class="form-panel" @submit.prevent="submit">
      <h2>Yeni dahili</h2>
      <div class="mode-picker">
        <button type="button" class="button" :class="{ 'button-primary': mode === 'target' }" @click="mode = 'target'">Hazır hedefe yönlendir</button>
        <button type="button" class="button" :class="{ 'button-primary': mode === 'flow' }" @click="mode = 'flow'">Gelişmiş çağrı akışı</button>
      </div>
      <p class="form-description">{{ mode === 'target' ? 'Numarayı endpoint, kuyruk, IVR veya trunk kaydına yönlendirin.' : 'Numara için sıralı Asterisk işlemleri oluşturun.' }}</p>
      <div class="form-grid">
        <label v-if="tenantRequired" class="full">
          Tenant *
          <TenantSelect v-model="tenantId" :tenants="tenants" required :disabled="saving" />
          <span class="form-hint">Aktif bir tenant seçin.</span>
        </label>
        <ExtensionFields v-if="mode === 'target'" :form="form" :errors="validation" :disabled="saving" :targets="targets" :lookup-loading="lookupLoading" :scope="scope" />
        <template v-else>
          <label>Dahili numarası *<input v-model="form.extensionNumber" required pattern="[0-9]{1,20}" /></label>
          <label class="checkbox"><input v-model="form.enabled" type="checkbox" /> Aktif</label>
        </template>
      </div>
      <InlineFeedback v-if="mode === 'target'" :error="lookupError" retry @retry="loadTargets" />
      <template v-else>
        <h3>İşlem sırası</h3>
        <div v-for="(step, index) in steps" :key="index" class="flow-step">
          <div class="form-grid">
            <label>Öncelik<input :value="index + 1" type="number" disabled /></label>
            <label>Uygulama *<select v-model="step.application"><option v-for="app in applications" :key="app">{{ app }}</option></select></label>
            <label class="full">Parametre<input v-model="step.applicationData" :required="requiresData(step.application)" :disabled="!requiresData(step.application)" :placeholder="step.application === 'Playback' ? 'Örn. welcome' : step.application === 'Wait' ? '0–300 saniye' : 'Parametre gerekmez'" /></label>
          </div>
          <div class="table-actions"><button type="button" class="button" :disabled="index === 0" @click="move(index, -1)">Yukarı</button><button type="button" class="button" :disabled="index === steps.length - 1" @click="move(index, 1)">Aşağı</button><button type="button" class="button danger" :disabled="steps.length === 1" @click="removeStep(index)">Kaldır</button></div>
        </div>
        <button type="button" class="button" @click="addStep">+ İşlem ekle</button>
      </template>
      <FormActions :cancel-to="url(basePath)" :saving="saving" :disabled="mode === 'target' && lookupLoading" />
    </form>
    <aside class="form-aside">
      <AppIcon name="shield" :size="25" />
      <h3>Notlar</h3>
      <p>{{ mode === 'target' ? 'Dial, Queue ve Goto satırları seçtiğiniz hedefe göre otomatik üretilir.' : 'İşlemler sıralarıyla birlikte Realtime extensions tablosuna yazılır.' }}</p>
    </aside>
  </div>
</template>

<style scoped>
.mode-picker { display: flex; gap: .75rem; flex-wrap: wrap; margin-bottom: 1rem; }
.flow-step { padding: 1rem 0; border-top: 1px solid var(--border-color, #dde3ea); }
</style>
