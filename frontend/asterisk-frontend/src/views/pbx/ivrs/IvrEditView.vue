<script setup lang="ts">
import { ivrResource as config } from '@/config/resources/ivrs'
import { useResourceForm } from '@/composables/useResourceForm'
import IvrFields from '@/components/ivrs/IvrFields.vue'
import PageHeader from '@/components/common/PageHeader.vue'
import InlineFeedback from '@/components/common/InlineFeedback.vue'
import EmptyState from '@/components/common/EmptyState.vue'
import AppIcon from '@/components/common/AppIcon.vue'
import TenantSelect from '@/components/forms/TenantSelect.vue'
import FormActions from '@/components/forms/FormActions.vue'

const {
  route,
  prefix,
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
  scope,
  url,
  initialize,
  save,
} = useResourceForm(config, 'edit')
</script>

<template>
  <PageHeader title="IVR düzenle" :description="config.description">
    <RouterLink :to="url(basePath)" class="button">Listeye dön</RouterLink>
  </PageHeader>
  <InlineFeedback :error="error" :retry="!ready && !loading" @retry="initialize" />
  <EmptyState v-if="loading" loading />
  <div v-else-if="ready" class="form-layout">
    <form class="form-panel" @submit.prevent="save">
      <h2>Kaydı düzenle</h2>
      <p class="form-description">Kayıt bilgileri.</p>
      <div class="form-grid">
        <label v-if="tenantRequired" class="full">
          Tenant *
          <TenantSelect v-model="tenantId" :tenants="tenants" required :disabled="true" />
          <span class="form-hint">Tenant değiştirilemez.</span>
        </label>
        <IvrFields :form="form" :errors="validation" :disabled="saving" :tenant-id="scope" editing />
      </div>
      <FormActions :cancel-to="url(basePath)" :saving="saving" />
    </form>
    <aside class="form-aside">
      <AppIcon name="shield" :size="25" />
      <h3>Notlar</h3>
      <p>Kayıt seçili tenant kapsamında saklanır.</p>
      <h3>Bağlı kayıtlar</h3>
      <RouterLink :to="url(`${prefix}/ivrs/${route.params.id}/options`)" class="text-link">Tuşlama seçenekleri<AppIcon name="arrow" :size="15" /></RouterLink>
      <p class="form-description">Her tuş kaydında realtime extensions satırları yeniden derlenir.</p>
    </aside>
  </div>
</template>
