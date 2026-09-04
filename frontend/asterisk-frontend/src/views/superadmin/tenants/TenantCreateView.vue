<script setup lang="ts">
import { tenantResource as config } from '@/config/resources/tenants'
import { useResourceForm } from '@/composables/useResourceForm'
import TenantFields from '@/components/tenants/TenantFields.vue'
import PageHeader from '@/components/common/PageHeader.vue'
import InlineFeedback from '@/components/common/InlineFeedback.vue'
import EmptyState from '@/components/common/EmptyState.vue'
import AppIcon from '@/components/common/AppIcon.vue'
import FormActions from '@/components/forms/FormActions.vue'

const {
  basePath,
  form,
  validation,
  loading,
  saving,
  ready,
  error,
  url,
  initialize,
  save,
} = useResourceForm(config, 'create')
</script>

<template>
  <PageHeader title="Tenant oluştur" :description="config.description">
    <RouterLink :to="url(basePath)" class="button">Listeye dön</RouterLink>
  </PageHeader>
  <InlineFeedback :error="error" :retry="!ready && !loading" @retry="initialize" />
  <EmptyState v-if="loading" loading />
  <div v-else-if="ready" class="form-layout">
    <form class="form-panel" @submit.prevent="save">
      <h2>Yeni kayıt</h2>
      <p class="form-description">Yeni kayıt.</p>
      <div class="form-grid">
        <TenantFields :form="form" :errors="validation" :disabled="saving" />
      </div>
      <FormActions :cancel-to="url(basePath)" :saving="saving" />
    </form>
    <aside class="form-aside">
      <AppIcon name="shield" :size="25" />
      <h3>Notlar</h3>
      <p>Tenant’ı pasifleştirmek kurumun erişimini kapatır; kayıtlarını silmez.</p>
    </aside>
  </div>
</template>
