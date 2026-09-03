<script setup lang="ts">
import { userResource as config } from '@/config/resources/users'
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
import TenantSelect from '@/components/forms/TenantSelect.vue'

const {
  route,
  auth,
  basePath,
  tenantId,
  tenants,
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
  <PageHeader title="Kullanıcılar" :description="config.description">
    <button class="button" :disabled="loading" @click="initialize"><AppIcon name="refresh" :size="16" />Yenile</button>
    <RouterLink :to="url(`${basePath}/create`)" class="button button-primary"><AppIcon name="plus" :size="16" />Kullanıcı oluştur</RouterLink>
  </PageHeader>
  <InlineFeedback :error="error" :success="success || (route.query.saved ? 'Değişiklikler başarıyla kaydedildi.' : '')" :retry="!ready && !loading" @retry="initialize" />
  <section class="panel">
    <ListToolbar v-model:search="search" v-model:sort="sort" :total="total" :loading="loading">
      <TenantSelect v-if="auth.isSuperAdmin" v-model="tenantId" :tenants="tenants" :disabled="loading" placeholder="Tenant seçin" />
    </ListToolbar>
    <EmptyState v-if="loading" loading />
    <EmptyState v-else-if="!tenantId" icon="building" title="Önce bir tenant seçin" description="Yönetici hesaplarını görmek için yukarıdan bir kurum seçin." />
    <EmptyState
      v-else-if="!visibleRows.length"
      icon="users"
      :title="error ? 'Veriler yüklenemedi' : search ? 'Eşleşen kayıt yok' : 'Henüz bir kayıt yok'"
      :description="error ? 'Bağlantınızı kontrol edip yeniden deneyin.' : search ? 'Arama yalnızca açık sayfadaki kayıtları kapsar.' : 'İlk kullanıcı kaydınızı oluşturabilirsiniz.'"
    />
    <div v-else class="table-scroll">
      <table>
        <thead><tr>
          <th>Kayıt</th>
          <th>E-posta</th>
          <th>Rol</th>
          <th>Durum</th>
          <th v-if="auth.isSuperAdmin">Tenant</th>
          <th class="actions-heading">İşlemler</th>
        </tr></thead>
        <tbody>
          <tr v-for="row in visibleRows" :key="String(row.id)">
            <td><RecordIdentity :name="row.username" :id="row.id" icon="users" /></td>
            <td>{{ displayValue(row.email, 'email') }}</td>
            <td>{{ displayValue(row.role, 'role') }}</td>
            <td><StatusBadge :value="row.enabled" field="enabled" /></td>
            <td v-if="auth.isSuperAdmin">#{{ row.tenantId }}</td>
            <td><div class="actions">
              <RouterLink :to="url(`${basePath}/${row.id}/edit`)" class="icon-button" :aria-label="`${row.username} düzenle`"><AppIcon name="edit" :size="17" /></RouterLink>
              <button class="icon-button" :aria-label="`${row.username} pasifleştir`" :disabled="row.enabled === false" @click="deleteTarget = row"><AppIcon name="trash" :size="17" /></button>
            </div></td>
          </tr>
        </tbody>
      </table>
    </div>
    <PaginationBar :page="page" :total-pages="totalPages" :visible-count="visibleRows.length" :loading="loading" @change="changePage" />
  </section>
  <ConfirmDialog :open="Boolean(deleteTarget)" title="Kaydı pasifleştir" confirm-label="Pasifleştir" :busy="deleting" @cancel="deleteTarget = null" @confirm="remove">
    <strong>{{ deleteTarget?.username }}</strong> pasifleştirilecek. Erişim kapanır, veriler korunur.
  </ConfirmDialog>
</template>

<style scoped>
.actions-heading { text-align: right; }
</style>
