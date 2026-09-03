<script setup lang="ts">
import { userResource as config } from '@/config/resources/users'
import { useResourceForm } from '@/composables/useResourceForm'
import UserFields from '@/components/users/UserFields.vue'
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
  url,
  initialize,
  save,
} = useResourceForm(config, 'create')
</script>

<template>
  <PageHeader title="Kullanıcı oluştur" :description="config.description">
    <RouterLink :to="url(basePath)" class="button">Listeye dön</RouterLink>
  </PageHeader>
  <InlineFeedback :error="error" :retry="!ready && !loading" @retry="initialize" />
  <EmptyState v-if="loading" loading />
  <div v-else-if="ready" class="form-layout">
    <form class="form-panel" @submit.prevent="save">
      <h2>Yeni kayıt bilgileri</h2>
      <p class="form-description">Yıldızlı alanlar zorunludur. Bilgileri doldurarak yeni kaydınızı oluşturun.</p>
      <div class="form-grid">
        <label v-if="tenantRequired" class="full">
          Tenant *
          <TenantSelect v-model="tenantId" :tenants="tenants" required :disabled="saving" />
          <span class="form-hint">Santral kaydı oluşturmak için aktif bir tenant seçin.</span>
        </label>
        <UserFields :form="form" :errors="validation" :disabled="saving" />
      </div>
      <FormActions :cancel-to="url(basePath)" :saving="saving" />
    </form>
    <aside class="form-aside">
      <AppIcon name="shield" :size="25" />
      <h3>Tenant yöneticisi</h3>
      <p>Yeni hesaplar TENANT_ADMIN rolüyle oluşturulur. Kullanıcı güncellemeleri mevcut oturumlarını geçersiz kılar.</p>
    </aside>
  </div>
</template>
