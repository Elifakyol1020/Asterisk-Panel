<script setup lang="ts">
import { computed, onMounted, onBeforeUnmount, reactive, ref } from 'vue'
import { cdrService } from '@/services/cdr.service'
import { resourceService } from '@/services/resource.service'
import { errorMessage, type Page, type RecordData } from '@/api/platform'
import { useAuthStore } from '@/stores/auth.store'
import type { CdrRecord } from '@/types/cdr'
import PageHeader from '@/components/common/PageHeader.vue'
import AppIcon from '@/components/common/AppIcon.vue'
import PaginationBar from '@/components/tables/PaginationBar.vue'

const auth = useAuthStore()
const filters = reactive({ src: '', dst: '', disposition: '', startDate: '', endDate: '', minDuration: '', maxDuration: '', uniqueId: '', linkedId: '', tenantId: '', prefix: false, size: 20 })
const data = ref<Page<CdrRecord> | null>(null), loading = ref(false), error = ref(''), formError = ref(''), tenantError = ref('')
const tenants = ref<RecordData[]>([]), detail = ref<CdrRecord | null>(null), detailError = ref(''), detailLoading = ref(false), dialog = ref<HTMLDialogElement>()
let applied: Record<string, unknown> = { size: 20 }
let listAbort: AbortController | undefined, detailAbort: AbortController | undefined
const statuses: Record<string, string> = { ANSWERED: 'Yanıtlandı', 'NO ANSWER': 'Yanıtsız', BUSY: 'Meşgul', FAILED: 'Başarısız', CONGESTION: 'Yoğunluk' }
const date = (value: string | null) => value ? new Intl.DateTimeFormat('tr-TR', { dateStyle: 'medium', timeStyle: 'medium' }).format(new Date(value)) : '—'
const duration = (value: number) => `${Math.floor(value / 3600).toString().padStart(2, '0')}:${Math.floor(value % 3600 / 60).toString().padStart(2, '0')}:${(value % 60).toString().padStart(2, '0')}`
const tenantName = (id: number | null) => id === null ? 'Kurum belirlenemedi' : tenants.value.find(t => Number(t.id) === id)?.name || `Kurum #${id}`
const nextLimit = computed(() => data.value && (data.value.number + 2) * data.value.size > 10000)
async function load(page = 0) {
  listAbort?.abort(); const request = new AbortController(); listAbort = request
  loading.value = true; error.value = ''; data.value = null
  try { const response = await cdrService.list({ ...applied, page }, request.signal); if (!request.signal.aborted) data.value = response }
  catch (e) { if (!request.signal.aborted) error.value = errorMessage(e) }
  finally { if (!request.signal.aborted) loading.value = false }
}
function apply() {
  formError.value = ''
  if (filters.startDate && filters.endDate && new Date(filters.startDate) >= new Date(filters.endDate)) { formError.value = 'Bitiş tarihi başlangıçtan sonra olmalı.'; return }
  if (filters.minDuration !== '' && filters.maxDuration !== '' && Number(filters.minDuration) > Number(filters.maxDuration)) { formError.value = 'Minimum süre maksimum süreden büyük olamaz.'; return }
  applied = { ...filters, tenantId: auth.isSuperAdmin && filters.tenantId ? Number(filters.tenantId) : undefined,
    startDate: filters.startDate ? new Date(filters.startDate).toISOString() : undefined,
    endDate: filters.endDate ? new Date(filters.endDate).toISOString() : undefined,
    minDuration: filters.minDuration === '' ? undefined : Number(filters.minDuration), maxDuration: filters.maxDuration === '' ? undefined : Number(filters.maxDuration),
    disposition: filters.disposition || undefined }
  void load()
}
function reset() { Object.assign(filters, { src: '', dst: '', disposition: '', startDate: '', endDate: '', minDuration: '', maxDuration: '', uniqueId: '', linkedId: '', tenantId: '', prefix: false, size: 20 }); apply() }
async function openDetail(id: string) {
  detailAbort?.abort(); const request = new AbortController(); detailAbort = request
  detail.value = null; detailError.value = ''; detailLoading.value = true; dialog.value?.showModal()
  try { const response = await cdrService.get(id, request.signal); if (!request.signal.aborted) detail.value = response }
  catch (e) { if (!request.signal.aborted) detailError.value = errorMessage(e) }
  finally { if (!request.signal.aborted) detailLoading.value = false }
}
function closeDetail() { detailAbort?.abort(); dialog.value?.close() }
const detailFields = computed(() => detail.value ? [
  ['UniqueId', detail.value.uniqueId], ['LinkedId', detail.value.linkedId], ['Sıra (sequence)', detail.value.sequence],
  ['Kaynak kanal', detail.value.channel], ['Hedef kanal', detail.value.dstChannel], ['Context', detail.value.context],
  ['Başlangıç', date(detail.value.startTime)], ['Yanıtlanma', date(detail.value.answerTime)], ['Bitiş', date(detail.value.endTime)],
  ['Toplam süre', duration(detail.value.duration)], ['Konuşma süresi', duration(detail.value.billsec)], ['Sonuç', statuses[detail.value.disposition] || detail.value.disposition],
  ['Ses kaydı yolu', detail.value.recordingPath],
] : [])
onMounted(() => {
  void load()
  if (auth.isSuperAdmin) resourceService.all('/admin/tenants', { sort: 'name,asc' }).then(rows => { tenants.value = rows }).catch(() => { tenantError.value = 'Kurum listesi yüklenemedi. Sayfayı yenileyin.' })
})
onBeforeUnmount(() => { listAbort?.abort(); detailAbort?.abort() })
</script>

<template>
  <PageHeader title="Çağrı kayıtları" description="Aramalarınızı bulun, sonuçlarını inceleyin ve çağrı ayrıntılarına ulaşın.">
    <button class="button" :disabled="loading" @click="load(data?.number || 0)"><AppIcon name="refresh" :size="16" />Yenile</button>
  </PageHeader>
  <section class="cdr-banner"><span class="cdr-icon"><AppIcon name="phone" :size="26" /></span><div><span class="cdr-eyebrow">ÇAĞRI GEÇMİŞİ</span><h2>Her görüşmenin ayrıntısı.</h2><p>Arayan, aranan, çağrı sonucu ve süre bilgilerini tek ekranda takip edin.</p></div><div class="cdr-total"><strong>{{ data ? data.totalElements.toLocaleString('tr-TR') : '—' }}</strong><span>filtreye uyan CDR kaydı</span></div></section>
  <section class="panel">
    <header class="panel-heading"><div><h2>Çağrı geçmişi</h2><p>En yeni kayıtlar önce · Saatler cihazınızın yerel saat dilimindedir.</p></div><span class="badge neutral">{{ auth.isSuperAdmin ? 'Platform geneli' : 'Kurumunuza özel' }}</span></header>
    <form class="cdr-filters" @submit.prevent="apply"><fieldset :disabled="loading">
      <label>Arayan<input v-model="filters.src" maxlength="255" placeholder="Örn. 1003" /></label>
      <label>Aranan<input v-model="filters.dst" maxlength="255" placeholder="Örn. 1002" /></label>
      <label>Durum<select v-model="filters.disposition"><option value="">Tüm sonuçlar</option><option v-for="(label, value) in statuses" :key="value" :value="value">{{ label }}</option></select></label>
      <label>Başlangıç<input v-model="filters.startDate" type="datetime-local" /></label>
      <label>Bitiş (hariç)<input v-model="filters.endDate" type="datetime-local" /></label>
      <label>Minimum süre (sn)<input v-model="filters.minDuration" type="number" min="0" max="2147483647" step="1" placeholder="0" /></label>
      <label v-if="auth.isSuperAdmin">Kurum<select v-model="filters.tenantId"><option value="">Tüm kurumlar</option><option v-for="tenant in tenants" :key="String(tenant.id)" :value="String(tenant.id)">{{ tenant.name }}</option></select></label>
      <label>Sayfa boyutu<select v-model.number="filters.size"><option :value="20">20 kayıt</option><option :value="50">50 kayıt</option><option :value="100">100 kayıt</option></select></label>
      <details class="cdr-advanced"><summary>Gelişmiş filtreler</summary><div><label>UniqueId<input v-model="filters.uniqueId" maxlength="150" /></label><label>LinkedId<input v-model="filters.linkedId" maxlength="150" /></label><label>Maksimum süre (sn)<input v-model="filters.maxDuration" type="number" min="0" max="2147483647" step="1" /></label></div></details>
      <div class="cdr-filter-footer"><label class="cdr-checkbox"><input v-model="filters.prefix" type="checkbox" />Numaranın başlangıcıyla eşleştir</label><div class="actions"><button class="button" type="button" @click="reset">Temizle</button><button class="button button-primary" type="submit"><AppIcon name="search" :size="16" />Filtrele</button></div></div>
    </fieldset><p v-if="formError || tenantError" class="cdr-error-text" role="alert">{{ formError || tenantError }}</p></form>
    <div v-if="loading" class="cdr-message" role="status"><AppIcon name="refresh" /><h3>Çağrı kayıtları yükleniyor…</h3></div>
    <div v-else-if="error" class="cdr-message" role="alert"><AppIcon name="info" /><h3>Kayıtlara ulaşılamadı</h3><p>{{ error }}</p><button class="button" @click="load()">Tekrar dene</button></div>
    <template v-else-if="data">
      <div v-if="!data.content.length" class="cdr-message"><span class="cdr-icon"><AppIcon name="phone" :size="26" /></span><h3>Gösterilecek çağrı kaydı yok</h3><p>Filtreleri genişletin. Henüz çağrı aktarılmadıysa kayıtlar ilk aktarımdan sonra burada görünecek.</p><button class="button" @click="reset">Filtreleri temizle</button></div>
      <div v-else class="table-scroll"><table><caption class="cdr-sr">Çağrı geçmişi ve süreleri</caption><thead><tr><th scope="col">Tarih</th><th scope="col">Arayan</th><th scope="col">Aranan</th><th scope="col">Durum</th><th scope="col">Toplam süre</th><th scope="col">Konuşma</th><th v-if="auth.isSuperAdmin" scope="col">Kurum</th><th scope="col">UniqueId / detay</th></tr></thead><tbody>
        <tr v-for="record in data.content" :key="record.id" class="cdr-row" @click="openDetail(record.id)"><td>{{ date(record.startTime) }}</td><td><strong>{{ record.src }}</strong><small>{{ record.srcName || '—' }}</small></td><td><strong>{{ record.dst }}</strong><small>{{ record.dstName || '—' }}</small></td><td><span class="badge" :class="{ 'cdr-unanswered': record.disposition === 'NO ANSWER', 'cdr-failed': ['FAILED', 'CONGESTION'].includes(record.disposition), neutral: record.disposition === 'BUSY' }">{{ statuses[record.disposition] || record.disposition }}</span></td><td class="cdr-time">{{ duration(record.duration) }}</td><td class="cdr-time">{{ duration(record.billsec) }}</td><td v-if="auth.isSuperAdmin">{{ tenantName(record.tenantId) }}</td><td><button class="button button-small" :aria-label="`${record.uniqueId} çağrı detayını aç`" @click.stop="openDetail(record.id)">{{ record.uniqueId }}<AppIcon name="eye" :size="15" /></button></td></tr>
      </tbody></table></div>
      <PaginationBar :page="data.number" :total-pages="Math.min(data.totalPages, Math.floor(10000 / data.size))" :visible-count="data.content.length" @change="load(data!.number + $event)" />
      <p v-if="nextLimit" class="cdr-limit">İlk 10.000 kayıt sınırına ulaştınız. Daha eski kayıtlar için tarih aralığını daraltın.</p>
    </template>
  </section>
  <aside class="cdr-guide"><AppIcon name="info" :size="19" /><p><strong>CDR nedir?</strong> Çağrının kimler arasında, ne zaman ve hangi sonuçla gerçekleştiğini gösteren kayıttır. Toplam süre çalma süresini de kapsar; konuşma süresi yanıt sonrasıdır. Aktarılan çağrılar birden fazla CDR oluşturabilir.</p></aside>
  <dialog ref="dialog" class="modal cdr-modal" aria-labelledby="cdr-detail-title" @cancel.prevent="closeDetail"><header><div><span class="cdr-eyebrow">ÇAĞRI AYRINTILARI</span><h2 id="cdr-detail-title">{{ detail ? `${detail.src} → ${detail.dst}` : 'Çağrı detayı' }}</h2></div><button class="icon-button" aria-label="Detayı kapat" @click="closeDetail"><AppIcon name="close" /></button></header><p v-if="detailLoading" role="status">Detay yükleniyor…</p><p v-else-if="detailError" role="alert">{{ detailError }}</p><template v-else-if="detail"><p v-if="auth.isSuperAdmin">{{ tenantName(detail.tenantId) }}</p><dl><div v-for="[label, value] in detailFields" :key="String(label)"><dt>{{ label }}</dt><dd>{{ value ?? '—' }}</dd></div></dl><p class="cdr-detail-note">Ses kaydı yolu bilgi amaçlıdır. Bu ekrandan ses dosyası indirilmez veya oynatılmaz.</p></template><div class="modal-actions"><button class="button" @click="closeDetail">Kapat</button></div></dialog>
</template>

<style scoped>
.cdr-banner{display:flex;gap:20px;align-items:center;padding:28px;margin-bottom:24px;border:1px solid var(--line);border-radius:12px;background:linear-gradient(115deg,var(--surface-soft),var(--surface))}.cdr-icon{display:grid;place-items:center;flex-shrink:0;width:54px;height:54px;border-radius:14px;background:var(--accent-soft);color:var(--accent)}.cdr-eyebrow{font-size:10px;font-weight:700;letter-spacing:.14em;color:var(--accent)}.cdr-banner h2{font-size:23px;margin:5px 0;color:var(--ink)}.cdr-banner p{font-size:13px;color:var(--muted)}.cdr-total{margin-left:auto;text-align:right;white-space:nowrap}.cdr-total strong{display:block;font-size:36px;color:var(--ink)}.cdr-total span{font-size:12px;color:var(--muted)}.cdr-filters{padding:22px;border-bottom:1px solid var(--line);background:var(--surface)}.cdr-filters fieldset{display:grid;grid-template-columns:repeat(4,minmax(0,1fr));gap:16px;border:0;padding:0;margin:0}.cdr-filters label{display:grid;gap:7px;font-size:12px;color:var(--muted)}.cdr-filters input,.cdr-filters select{width:100%;min-width:0;min-height:40px}.cdr-filter-footer{grid-column:1/-1;display:flex;justify-content:space-between;align-items:center;gap:15px}.cdr-filters .cdr-checkbox{display:flex;flex-direction:row;align-items:center;gap:8px}.cdr-checkbox input{width:16px;min-height:16px}.cdr-advanced{grid-column:1/-1;font-size:12px;color:var(--muted)}.cdr-advanced summary{cursor:pointer}.cdr-advanced>div{display:grid;grid-template-columns:repeat(3,minmax(0,1fr));gap:16px;padding-top:15px}.cdr-message{padding:55px 24px;display:flex;align-items:center;flex-direction:column;gap:14px;text-align:center;color:var(--muted)}.cdr-message p{font-size:14px;max-width:540px}.cdr-error-text{color:var(--danger);font-size:13px;margin-top:16px}.cdr-unanswered{background:var(--warning-bg);color:var(--warning)}.cdr-failed{background:var(--danger-bg);color:var(--danger)}.cdr-time{font-variant-numeric:tabular-nums}.cdr-row{cursor:pointer}.cdr-row:hover{background:var(--surface-hover)}.cdr-guide{display:flex;align-items:flex-start;gap:10px;padding:20px 4px;color:var(--muted);font-size:12px;line-height:1.7}.cdr-guide svg{flex-shrink:0;margin-top:2px}.cdr-guide strong{color:var(--ink)}.cdr-modal{width:min(750px,calc(100vw - 32px));max-height:85vh;overflow:auto}.cdr-modal header{display:flex;justify-content:space-between;align-items:flex-start}.cdr-modal dl{display:grid;grid-template-columns:1fr 1fr;gap:20px;margin-top:24px}.cdr-modal dt{font-size:11px;color:var(--muted);margin-bottom:7px}.cdr-modal dd{margin:0;font-size:13px;overflow-wrap:anywhere;color:var(--ink)}.cdr-detail-note,.cdr-limit{font-size:12px;padding-top:20px;color:var(--muted)}.cdr-limit{padding:16px 22px}.cdr-sr{position:absolute;width:1px;height:1px;overflow:hidden;clip-path:inset(50%)}@media(max-width:1100px){.cdr-filters fieldset{grid-template-columns:repeat(2,minmax(0,1fr))}}@media(max-width:760px){.cdr-banner{padding:20px;gap:12px;flex-wrap:wrap}.cdr-banner h2{font-size:20px}.cdr-total{width:100%;text-align:left;padding-top:14px;border-top:1px solid var(--line)}.cdr-total strong{font-size:28px}.cdr-filter-footer{align-items:flex-start;flex-direction:column}.cdr-filters fieldset,.cdr-advanced>div,.cdr-modal dl{grid-template-columns:1fr}.cdr-filter-footer .actions{width:100%}.cdr-filter-footer button{flex:1}}
</style>
