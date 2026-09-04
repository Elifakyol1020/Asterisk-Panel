<script setup lang="ts">
import { queueMemberResource as config } from '@/config/resources/members'
import { useResourceForm } from '@/composables/useResourceForm'
import QueueMemberFields from '@/components/members/QueueMemberFields.vue'
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
  <PageHeader title="Kuyruk üyesi düzenle" :description="config.description">
    <template #breadcrumb>
      <div class="page-kicker"><RouterLink :to="`${prefix}/queues`">{{ parent?.name || `#${parentId}` }}</RouterLink> / Kuyruk üyeleri</div>
    </template>
    <RouterLink :to="url(basePath)" class="button">Listeye dön</RouterLink>
  </PageHeader>
  <InlineFeedback :error="error" :retry="!ready && !loading" @retry="initialize" />
  <EmptyState v-if="loading" loading />
  <div v-else-if="ready" class="form-layout">
    <form class="form-panel" @submit.prevent="save">
      <h2>Kaydı düzenle</h2>
      <p class="form-description">Kayıt bilgileri.</p>
      <div class="form-grid">
        <QueueMemberFields :form="form" :errors="validation" :disabled="saving" editing :targets="targets" :lookup-loading="lookupLoading" :scope="scope" />
      </div>
      <InlineFeedback :error="lookupError" retry @retry="loadTargets" />
      <FormActions :cancel-to="url(basePath)" :saving="saving" :disabled="lookupLoading" />
    </form>
    <aside class="form-aside">
      <AppIcon name="shield" :size="25" />
      <h3>Notlar</h3>
      <p>Endpoint aynı tenant’a ait olmalıdır. Aynı endpoint bir kuyrukta yalnızca bir kez üye yapılabilir.</p>
    </aside>
  </div>
</template>
