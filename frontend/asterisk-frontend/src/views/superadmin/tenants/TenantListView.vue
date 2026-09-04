<script setup lang="ts">
import { tenantResource as config } from '@/config/resources/tenants'
import { useResourceList } from '@/composables/useResourceList'
import { displayValue } from '@/utils/display'
import AppIcon from '@/components/common/AppIcon.vue'
import PageHeader from '@/components/common/PageHeader.vue'
import InlineFeedback from '@/components/common/InlineFeedback.vue'
import EmptyState from '@/components/common/EmptyState.vue'
import RecordIdentity from '@/components/common/RecordIdentity.vue'
import StatusBadge from '@/components/common/StatusBadge.vue'
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
} = useResourceList(config)
</script>

<template>
  <PageHeader title="Tenant’lar" :description="config.description">
    <button class="button" :disabled="loading" @click="initialize"><AppIcon name="refresh" :size="16" />Yenile</button>
    <RouterLink :to="url(`${basePath}/create`)" class="button button-primary"><AppIcon name="plus" :size="16" />Tenant oluştur</RouterLink>
  </PageHeader>
  <InlineFeedback :error="error" :success="success || (route.query.saved ? 'Değişiklikler başarıyla kaydedildi.' : '')" :retry="!ready && !loading" @retry="initialize" />
  <section class="panel">
    <ListToolbar v-model:search="search" v-model:sort="sort" :total="total" :loading="loading" />
    <EmptyState v-if="loading" loading />
    <EmptyState
      v-else-if="!visibleRows.length"
      icon="building"
      :title="error ? 'Veriler yüklenemedi' : search ? 'Eşleşen kayıt yok' : 'Henüz bir kayıt yok'"
      :description="error ? 'Bağlantınızı kontrol edip yeniden deneyin.' : search ? 'Arama yalnızca açık sayfadaki kayıtları kapsar.' : 'İlk tenant kaydınızı oluşturabilirsiniz.'"
    />
    <div v-else class="table-scroll">
      <table>
        <thead><tr>
          <th>Kayıt</th>
          <th>Kısa kod</th>
          <th>Durum</th>
          <th>Oluşturulma</th>
          <th class="actions-heading">İşlemler</th>
        </tr></thead>
        <tbody>
          <tr v-for="row in visibleRows" :key="String(row.id)">
            <td><RecordIdentity :name="row.name" :id="row.id" icon="building" /></td>
            <td>{{ displayValue(row.code, 'code') }}</td>
            <td><StatusBadge :value="row.status" field="status" /></td>
            <td>{{ displayValue(row.createdAt, 'createdAt') }}</td>
            <td><div class="actions">
              <RouterLink :to="{ path: `${prefix}/users`, query: { tenantId: String(row.id) } }" class="button button-small">Kullanıcılar</RouterLink>
              <RouterLink :to="url(`${basePath}/${row.id}/edit`)" class="icon-button" :aria-label="`${row.name} düzenle`"><AppIcon name="edit" :size="17" /></RouterLink>
              <button class="icon-button" :aria-label="`${row.name} pasifleştir`" :disabled="row.status === 'INACTIVE'" @click="deleteTarget = row"><AppIcon name="trash" :size="17" /></button>
            </div></td>
          </tr>
        </tbody>
      </table>
    </div>
    <PaginationBar :page="page" :total-pages="totalPages" :visible-count="visibleRows.length" :loading="loading" @change="changePage" />
  </section>
  <ConfirmDialog :open="Boolean(deleteTarget)" title="Kaydı pasifleştir" confirm-label="Pasifleştir" :busy="deleting" @cancel="deleteTarget = null" @confirm="remove">
    <strong>{{ deleteTarget?.name }}</strong> pasifleştirilecek. Erişim kapanır, veriler korunur.
  </ConfirmDialog>
</template>

<style scoped>
.actions-heading { text-align: right; }
</style>
