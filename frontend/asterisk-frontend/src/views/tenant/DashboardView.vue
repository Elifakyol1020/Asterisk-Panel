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

const { loading, errors, totals, rows, updatedAt, load } = useDashboardData('endpoints', pbxKeys)
const date = new Date().toLocaleDateString('tr-TR', { day: 'numeric', month: 'long', year: 'numeric', weekday: 'long' })
const cards = [
  { key: 'endpoints', title: 'Endpoint’ler', icon: 'phone', to: '/tenant/endpoints' },
  { key: 'extensions', title: 'Dahililer', icon: 'route', to: '/tenant/extensions' },
  { key: 'queues', title: 'Çağrı kuyrukları', icon: 'queue', to: '/tenant/queues' },
  { key: 'ivrs', title: 'Sesli yanıt (IVR)', icon: 'mic', to: '/tenant/ivrs' },
]
const quickActions = [
  { to: '/tenant/endpoints/create', icon: 'phone', title: 'Endpoint oluştur', description: 'SIP cihazınızı ekleyin' },
  { to: '/tenant/queues/create', icon: 'queue', title: 'Kuyruk oluştur', description: 'Çağrı dağıtımını yapılandırın' },
  { to: '/tenant/ivrs/create', icon: 'mic', title: 'IVR oluştur', description: 'Karşılama anonsunu yapılandırın' },
]
</script>

<template>
  <div class="page-kicker">{{ date }}</div>
  <PageHeader title="Santralinize genel bakış" description="Kurumunuzun santral kaynaklarını buradan yönetin.">
    <button class="button" :disabled="loading" @click="load"><AppIcon name="refresh" :size="16" />Yenile</button>
    <RouterLink to="/tenant/endpoints/create" class="button button-primary"><AppIcon name="plus" :size="17" />Endpoint oluştur</RouterLink>
  </PageHeader>
  <div v-if="errors.length" class="form-error" role="alert">
    <strong>Bazı veriler yüklenemedi.</strong>
    <p v-for="error in errors" :key="error">{{ error }}</p>
  </div>
  <DashboardStats :cards="cards" :totals="totals" :loading="loading" note="Tenant’ınıza ait kayıtlar" />
  <div class="dashboard-columns">
    <div class="dashboard-main">
      <section class="panel">
        <header class="panel-heading">
          <div><h2>Son eklenen endpoint’ler</h2><p>SIP cihazlarınızın yapılandırma özeti.</p></div>
          <RouterLink to="/tenant/endpoints" class="text-link">Tümünü gör<AppIcon name="arrow" :size="15" /></RouterLink>
        </header>
        <EmptyState v-if="loading" loading />
        <EmptyState v-else-if="!rows.length" icon="phone" :title="errors.length ? 'Veriler görüntülenemiyor' : 'Henüz bir kayıt yok'" :description="errors.length ? 'Bağlantınızı kontrol edip yeniden deneyin.' : 'İlk kaydınızı oluşturarak başlayabilirsiniz.'">
          <RouterLink v-if="!errors.length" to="/tenant/endpoints/create" class="button button-primary">İlk kaydı oluştur</RouterLink>
        </EmptyState>
        <div v-else class="table-scroll">
          <table>
            <thead><tr><th>Endpoint</th><th>Dahili</th><th>Durum</th><th aria-label="Düzenle" /></tr></thead>
            <tbody>
              <tr v-for="row in rows" :key="String(row.id)">
                <td><RecordIdentity :name="row.displayName" :id="row.id" icon="phone" /></td>
                <td>{{ row.extension }}</td>
                <td><StatusBadge :value="row.enabled" /></td>
                <td><RouterLink :to="`/tenant/endpoints/${row.id}/edit`" class="icon-button" :aria-label="`${row.displayName} düzenle`"><AppIcon name="chevron" :size="16" /></RouterLink></td>
              </tr>
            </tbody>
          </table>
        </div>
        <footer class="panel-footer">
          <span>{{ loading ? 'Yükleniyor…' : `${totals.endpoints ?? '—'} kayıttan ${rows.length} kayıt gösteriliyor` }}</span>
          <span>Son kontrol {{ updatedAt || '—' }}</span>
        </footer>
      </section>
      <ResourceDistribution :totals="totals" :loading="loading" prefix="/tenant" />
    </div>
    <aside class="side-stack">
      <QuickActions :items="quickActions" />
      <ConfigurationNotice />
      <section class="panel access-note">
        <AppIcon name="shield" :size="21" />
        <h3>İzole çalışma alanı</h3>
        <p>Bu panelde yalnızca kendi tenant’ınıza ait kayıtlar listelenir.</p>
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
