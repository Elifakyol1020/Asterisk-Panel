<script setup lang="ts">
import { extensionResource as config } from '@/config/resources/extensions'
import { useResourceForm } from '@/composables/useResourceForm'
import ExtensionFields from '@/components/extensions/ExtensionFields.vue'
import PageHeader from '@/components/common/PageHeader.vue'
import InlineFeedback from '@/components/common/InlineFeedback.vue'
import EmptyState from '@/components/common/EmptyState.vue'
import AppIcon from '@/components/common/AppIcon.vue'
import TenantSelect from '@/components/forms/TenantSelect.vue'
import FormActions from '@/components/forms/FormActions.vue'

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
} = useResourceForm(config, 'edit')
</script>

<template>
  <PageHeader title="Dahili düzenle" :description="config.description">
    <RouterLink :to="url(basePath)" class="button">Listeye dön</RouterLink>
  </PageHeader>
  <InlineFeedback :error="error" :retry="!ready && !loading" @retry="initialize" />
  <EmptyState v-if="loading" loading />
  <div v-else-if="ready" class="form-layout">
    <form class="form-panel" @submit.prevent="save">
      <h2>Kayıt bilgilerini düzenle</h2>
      <p class="form-description">Yıldızlı alanlar zorunludur. Değişiklikler kaydettikten sonra uygulanır.</p>
      <div class="form-grid">
        <label v-if="tenantRequired" class="full">
          Tenant *
          <TenantSelect v-model="tenantId" :tenants="tenants" required :disabled="true" />
          <span class="form-hint">Kaydın tenant’ı değiştirilemez.</span>
        </label>
        <ExtensionFields :form="form" :errors="validation" :disabled="saving" editing :targets="targets" :lookup-loading="lookupLoading" :scope="scope" />
      </div>
      <InlineFeedback :error="lookupError" retry @retry="loadTargets" />
      <FormActions :cancel-to="url(basePath)" :saving="saving" :disabled="lookupLoading" />
    </form>
    <aside class="form-aside">
      <AppIcon name="shield" :size="25" />
      <h3>Yapılandırma notları</h3>
      <p>Hedef kayıtlar aynı tenant’a ait olmalıdır. Santral bağlamı (context) backend tarafından oluşturulur.</p>
      <p>Değişiklikler uygulama veritabanına kaydedilir. Kayıt oluşturulması, canlı santral üzerinde etkinleştirildiği anlamına gelmez.</p>
    </aside>
  </div>
</template>
