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
      <h2>Kayıt bilgilerini düzenle</h2>
      <p class="form-description">Yıldızlı alanlar zorunludur. Değişiklikler kaydettikten sonra uygulanır.</p>
      <div class="form-grid">
        <label v-if="tenantRequired" class="full">
          Tenant *
          <TenantSelect v-model="tenantId" :tenants="tenants" required :disabled="true" />
          <span class="form-hint">Kaydın tenant’ı değiştirilemez.</span>
        </label>
        <IvrFields :form="form" :errors="validation" :disabled="saving" editing />
      </div>
      <FormActions :cancel-to="url(basePath)" :saving="saving" />
    </form>
    <aside class="form-aside">
      <AppIcon name="shield" :size="25" />
      <h3>Yapılandırma notları</h3>
      <p>Hedef kayıtlar aynı tenant’a ait olmalıdır. Santral bağlamı (context) backend tarafından oluşturulur.</p>
      <p>Değişiklikler uygulama veritabanına kaydedilir. Kayıt oluşturulması, canlı santral üzerinde etkinleştirildiği anlamına gelmez.</p>
      <h3>Bağlı kayıtlar</h3>
      <RouterLink :to="url(`${prefix}/ivrs/${route.params.id}/options`)" class="text-link">Tuşlama seçenekleri<AppIcon name="arrow" :size="15" /></RouterLink>
    </aside>
  </div>
</template>
