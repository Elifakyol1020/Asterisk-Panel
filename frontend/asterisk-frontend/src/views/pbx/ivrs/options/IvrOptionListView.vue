<script setup lang="ts">
import { ivrOptionResource as config } from '@/config/resources/options'
import { useResourceList } from '@/composables/useResourceList'
import { displayValue } from '@/utils/display'
import AppIcon from '@/components/common/AppIcon.vue'
import PageHeader from '@/components/common/PageHeader.vue'
import InlineFeedback from '@/components/common/InlineFeedback.vue'
import EmptyState from '@/components/common/EmptyState.vue'
import RecordIdentity from '@/components/common/RecordIdentity.vue'
import ConfirmDialog from '@/components/common/ConfirmDialog.vue'
import ListToolbar from '@/components/tables/ListToolbar.vue'
import PaginationBar from '@/components/tables/PaginationBar.vue'

const {
  route,
  prefix,
  basePath,
  visibleRows,
  loading,
  deleting,
  ready,
  error,
  success,
  page,
  total,
  totalPages,
  search,
  sort,
  deleteTarget,
  url,
  initialize,
  remove,
  changePage,
  parent,
  parentId,
} = useResourceList(config)
</script>

<template>
  <PageHeader title="IVR seçenekleri" :description="config.description">
    <template #breadcrumb>
      <div class="page-kicker"><RouterLink :to="`${prefix}/ivrs`">{{ parent?.name || `#${parentId}` }}</RouterLink> / IVR seçenekleri</div>
    </template>
    <button class="button" :disabled="loading" @click="initialize"><AppIcon name="refresh" :size="16" />Yenile</button>
    <RouterLink :to="url(`${basePath}/create`)" class="button button-primary"><AppIcon name="plus" :size="16" />Tuşlama seçeneği oluştur</RouterLink>
  </PageHeader>
  <InlineFeedback :error="error" :success="success || (route.query.saved ? 'Değişiklikler başarıyla kaydedildi.' : '')" :retry="!ready && !loading" @retry="initialize" />
  <section class="panel">
    <ListToolbar v-model:search="search" v-model:sort="sort" :total="total" :loading="loading" />
    <EmptyState v-if="loading" loading />
    <EmptyState
      v-else-if="!visibleRows.length"
      icon="route"
      :title="error ? 'Veriler yüklenemedi' : search ? 'Eşleşen kayıt yok' : 'Henüz bir kayıt yok'"
      :description="error ? 'Bağlantınızı kontrol edip yeniden deneyin.' : search ? 'Arama yalnızca açık sayfadaki kayıtları kapsar.' : 'İlk tuşlama seçeneği kaydınızı oluşturabilirsiniz.'"
    />
    <div v-else class="table-scroll">
      <table>
        <thead><tr>
          <th>Tuş</th>
          <th>İşlem türü</th>
          <th>Hedef ID</th>
          <th class="actions-heading">İşlemler</th>
        </tr></thead>
        <tbody>
          <tr v-for="row in visibleRows" :key="String(row.id)">
            <td><RecordIdentity :name="row.digit" :id="row.id" icon="route" /></td>
            <td>{{ displayValue(row.actionType, 'actionType') }}</td>
            <td>{{ displayValue(row.targetId, 'targetId') }}</td>
            <td><div class="actions">
              <RouterLink :to="url(`${basePath}/${row.id}/edit`)" class="icon-button" :aria-label="`${row.digit} düzenle`"><AppIcon name="edit" :size="17" /></RouterLink>
              <button class="icon-button" :aria-label="`${row.digit} sil`" @click="deleteTarget = row"><AppIcon name="trash" :size="17" /></button>
            </div></td>
          </tr>
        </tbody>
      </table>
    </div>
    <PaginationBar :page="page" :total-pages="totalPages" :visible-count="visibleRows.length" :loading="loading" @change="changePage" />
  </section>
  <ConfirmDialog :open="Boolean(deleteTarget)" title="Kaydı sil" confirm-label="Evet, sil" :busy="deleting" @cancel="deleteTarget = null" @confirm="remove">
    <strong>{{ deleteTarget?.digit }}</strong> kalıcı olarak silinecek. Bu işlem geri alınamaz.
  </ConfirmDialog>
</template>

<style scoped>
.actions-heading { text-align: right; }
</style>
