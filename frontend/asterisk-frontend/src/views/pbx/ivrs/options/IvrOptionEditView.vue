<script setup lang="ts">
import { ivrOptionResource as config } from '@/config/resources/options'
import { useResourceForm } from '@/composables/useResourceForm'
import IvrOptionFields from '@/components/options/IvrOptionFields.vue'
import PageHeader from '@/components/common/PageHeader.vue'
import InlineFeedback from '@/components/common/InlineFeedback.vue'
import EmptyState from '@/components/common/EmptyState.vue'
import AppIcon from '@/components/common/AppIcon.vue'
import FormActions from '@/components/forms/FormActions.vue'

const {
  prefix,
  basePath,
  form,
  validation,
  loading,
  saving,
  ready,
  error,
  targets,
  lookupLoading,
  lookupError,
  scope,
  url,
  initialize,
  save,
  loadTargets,
  parent,
  parentId,
} = useResourceForm(config, 'edit')
</script>

<template>
  <PageHeader title="Tuşlama seçeneği düzenle" :description="config.description">
    <template #breadcrumb>
      <div class="page-kicker"><RouterLink :to="`${prefix}/ivrs`">{{ parent?.name || `#${parentId}` }}</RouterLink> / IVR seçenekleri</div>
    </template>
    <RouterLink :to="url(basePath)" class="button">Listeye dön</RouterLink>
  </PageHeader>
  <InlineFeedback :error="error" :retry="!ready && !loading" @retry="initialize" />
  <EmptyState v-if="loading" loading />
  <div v-else-if="ready" class="form-layout">
    <form class="form-panel" @submit.prevent="save">
      <h2>Kayıt bilgilerini düzenle</h2>
      <p class="form-description">Yıldızlı alanlar zorunludur. Değişiklikler kaydettikten sonra uygulanır.</p>
      <div class="form-grid">
        <IvrOptionFields :form="form" :errors="validation" :disabled="saving" editing :targets="targets" :lookup-loading="lookupLoading" :scope="scope" />
      </div>
      <InlineFeedback :error="lookupError" retry @retry="loadTargets" />
      <FormActions :cancel-to="url(basePath)" :saving="saving" :disabled="lookupLoading" />
    </form>
    <aside class="form-aside">
      <AppIcon name="shield" :size="25" />
      <h3>Yapılandırma notları</h3>
      <p>HANGUP için hedef gönderilmez. IVR kendisine yönlendirilemez.</p>
    </aside>
  </div>
</template>
