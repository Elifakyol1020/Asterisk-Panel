<script setup lang="ts">
import { useDashboardData } from '@/composables/useDashboardData'
import { pbxKeys } from '@/config/resources'
import PageHeader from '@/components/common/PageHeader.vue'
import AppIcon from '@/components/common/AppIcon.vue'
import EmptyState from '@/components/common/EmptyState.vue'
import RecordIdentity from '@/components/common/RecordIdentity.vue'
import StatusBadge from '@/components/common/StatusBadge.vue'
import ConfigurationNotice from '@/components/common/ConfigurationNotice.vue'
import DashboardStats from '@/components/dashboard/DashboardStats.vue'
import ResourceDistribution from '@/components/dashboard/ResourceDistribution.vue'
import QuickActions from '@/components/dashboard/QuickActions.vue'

const { loading, errors, totals, rows, updatedAt, load } = useDashboardData('tenants', ['tenants', ...pbxKeys])
const date = new Date().toLocaleDateString('tr-TR', { day: 'numeric', month: 'long', year: 'numeric', weekday: 'long' })
const cards = [
  { key: 'tenants', title: 'Tenant’lar', icon: 'building', to: '/super-admin/tenants' },
  { key: 'endpoints', title: 'Endpoint’ler', icon: 'phone', to: '/super-admin/endpoints' },
  { key: 'trunks', title: 'Trunk’lar', icon: 'network', to: '/super-admin/trunks' },
  { key: 'queues', title: 'Çağrı kuyrukları', icon: 'queue', to: '/super-admin/queues' },
]
const quickActions = [
  { to: '/super-admin/tenants/create', icon: 'building', title: 'Tenant oluştur', description: 'Kurumunuzu platforma ekleyin' },
  { to: '/super-admin/users', icon: 'users', title: 'Kullanıcıları yönet', description: 'Tenant yöneticisi ekleyin' },
  { to: '/super-admin/trunks/create', icon: 'network', title: 'Trunk oluştur', description: 'Operatör bağlantısını yapılandırın' },
]
</script>

<template>
  <div class="page-kicker">{{ date }}</div>
  <PageHeader title="Platforma genel bakış" description="Tenant’larınız ve iletişim altyapınız, tek bir ekranda.">
    <button class="button" :disabled="loading" @click="load"><AppIcon name="refresh" :size="16" />Yenile</button>
    <RouterLink to="/super-admin/tenants/create" class="button button-primary"><AppIcon name="plus" :size="17" />Tenant oluştur</RouterLink>
  </PageHeader>
  <div v-if="errors.length" class="form-error" role="alert">
    <strong>Bazı veriler yüklenemedi.</strong>
    <p v-for="error in errors" :key="error">{{ error }}</p>
  </div>
  <DashboardStats :cards="cards" :totals="totals" :loading="loading" note="Platform genelinde kayıtlı" />
  <div class="dashboard-columns">
    <div class="dashboard-main">
      <section class="panel">
        <header class="panel-heading">
          <div><h2>Son eklenen tenant’lar</h2><p>Platformunuza katılan kurumları yönetin.</p></div>
          <RouterLink to="/super-admin/tenants" class="text-link">Tümünü gör<AppIcon name="arrow" :size="15" /></RouterLink>
        </header>
        <EmptyState v-if="loading" loading />
        <EmptyState v-else-if="!rows.length" icon="building" :title="errors.length ? 'Veriler görüntülenemiyor' : 'Henüz bir kayıt yok'" :description="errors.length ? 'Bağlantınızı kontrol edip yeniden deneyin.' : 'İlk kaydınızı oluşturarak başlayabilirsiniz.'">
          <RouterLink v-if="!errors.length" to="/super-admin/tenants/create" class="button button-primary">İlk kaydı oluştur</RouterLink>
        </EmptyState>
        <div v-else class="table-scroll">
          <table>
            <thead><tr><th>Kurum</th><th>Tenant kodu</th><th>Durum</th><th aria-label="Düzenle" /></tr></thead>
            <tbody>
              <tr v-for="row in rows" :key="String(row.id)">
                <td><RecordIdentity :name="row.name" :id="row.id" icon="building" /></td>
                <td>{{ row.code }}</td>
                <td><StatusBadge :value="row.status" /></td>
                <td><RouterLink :to="`/super-admin/tenants/${row.id}/edit`" class="icon-button" :aria-label="`${row.name} düzenle`"><AppIcon name="chevron" :size="16" /></RouterLink></td>
              </tr>
            </tbody>
          </table>
        </div>
        <footer class="panel-footer">
          <span>{{ loading ? 'Yükleniyor…' : `${totals.tenants ?? '—'} kayıttan ${rows.length} kayıt gösteriliyor` }}</span>
          <span>Son kontrol {{ updatedAt || '—' }}</span>
        </footer>
      </section>
      <ResourceDistribution :totals="totals" :loading="loading" prefix="/super-admin" />
    </div>
    <aside class="side-stack">
      <QuickActions :items="quickActions" />
      <ConfigurationNotice />
      <section class="panel access-note">
        <AppIcon name="shield" :size="21" />
        <h3>Yetkiler sizin kontrolünüzde</h3>
        <p>Kurum erişimini tenant durumu üzerinden, yönetici erişimini kullanıcı hesabından düzenleyin.</p>
      </section>
    </aside>
  </div>
</template>

<style scoped>
.dashboard-main { display: flex; flex-direction: column; gap: 20px; min-width: 0; }
.access-note { padding: 22px; }
.access-note svg { color: #70988e; }
.access-note h3 { margin: 12px 0 7px; font-size: 14px; }
.access-note p { color: var(--muted); font-size: 13px; }
</style>
