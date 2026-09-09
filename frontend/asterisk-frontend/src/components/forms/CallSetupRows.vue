<script setup lang="ts">
import { ref, watch, onBeforeUnmount } from 'vue'
import { resourceService } from '@/services/resource.service'
import { errorMessage, type RecordData } from '@/api/platform'
import { recordLabel } from '@/utils/display'
const props = defineProps<{ kind: 'ivr' | 'queue'; tenantId?: number; disabled?: boolean }>()
const rows = defineModel<RecordData[]>({ required: true })
const choices = ref<Record<string, RecordData[]>>({})
const loading = ref(false), error = ref('')
let version = 0
const digits = ['0','1','2','3','4','5','6','7','8','9','*','#']
const actions: Record<string,string> = { ENDPOINT: 'Dahiliyi ara', QUEUE: 'Kuyruğa aktar', IVR: 'Başka IVR’a aktar', HANGUP: 'Çağrıyı sonlandır' }
async function load() {
 const request = ++version; choices.value = {}; error.value = ''; loading.value = false
 if (!props.tenantId) return
 loading.value = true
 try {
  const kinds = props.kind === 'ivr' ? ['endpoints','queues','ivrs'] : ['endpoints']
  const results = await Promise.all(kinds.map(async kind => [kind, await resourceService.all('/'+kind, {tenantId:props.tenantId})] as const))
  if (request === version) choices.value = Object.fromEntries(results)
 } catch(e) { if (request === version) error.value = errorMessage(e) }
 finally { if (request === version) loading.value = false }
}
function targets(row: RecordData) { return choices.value[props.kind === 'queue' ? 'endpoints' : ({ENDPOINT:'endpoints',QUEUE:'queues',IVR:'ivrs'}[String(row.actionType)] || '')] || [] }
function add() {
 if (props.kind === 'ivr') rows.value.push({digit:digits.find(d=>!rows.value.some(r=>r.digit===d)) || '',actionType:'ENDPOINT',targetId:''})
 else rows.value.push({endpointId:'',penalty:0,paused:false})
}
watch(()=>props.tenantId, (tenant,previous)=>{ if (previous !== undefined && tenant !== previous) rows.value=[]; void load() },{immediate:true})
onBeforeUnmount(()=>{version++})
</script>
<template>
 <section class="setup-rows">
  <h3>{{ kind === 'ivr' ? 'Tuşlara basıldığında yapılacak işlemler' : 'Kuyruk üyeleri' }}</h3>
  <p>{{ kind === 'ivr' ? 'Örneğin 1 satış kuyruğuna, 2 bir dahiliye yönlendirsin.' : 'Çağrıları karşılayacak dahilileri seçin. Düşük ceza değerindeki üyeler önceliklidir.' }}</p>
  <p v-if="!tenantId">Önce kurum seçin.</p>
  <p v-if="loading" role="status">Hedefler yükleniyor…</p>
  <p v-if="error" role="alert">{{ error }} <button type="button" class="button" @click="load">Tekrar dene</button></p>
  <fieldset :disabled="disabled || loading || !tenantId || Boolean(error)">
   <div v-for="(row,index) in rows" :key="index" class="setup-row">
    <template v-if="kind === 'ivr'">
     <label>Tuş<select v-model="row.digit" required><option v-for="digit in digits" :key="digit" :disabled="rows.some((other,i)=>i!==index && other.digit===digit)">{{ digit }}</option></select></label>
     <label>İşlem<select v-model="row.actionType" @change="row.targetId = row.actionType === 'HANGUP' ? null : ''"><option v-for="(label,action) in actions" :value="action" :key="action">{{ label }}</option></select></label>
     <label v-if="row.actionType !== 'HANGUP'">Hedef<select v-model.number="row.targetId" required><option value="" disabled>Hedef seçin</option><option v-for="target in targets(row)" :key="String(target.id)" :value="Number(target.id)">{{ recordLabel(target) }} · #{{ target.id }}</option></select></label>
    </template>
    <template v-else>
     <label>Dahili<select v-model.number="row.endpointId" required><option value="" disabled>Dahili seçin</option><option v-for="target in targets(row)" :key="String(target.id)" :value="Number(target.id)" :disabled="rows.some((other,i)=>i!==index && other.endpointId===Number(target.id))">{{ target.extension }} · {{ recordLabel(target) }}</option></select></label>
     <label>Ceza (öncelik)<input v-model.number="row.penalty" type="number" min="0" max="1000" step="1" required /></label>
     <label class="paused"><input v-model="row.paused" type="checkbox" />Üye duraklatıldı</label>
    </template>
    <button type="button" class="button" @click="rows.splice(index,1)" :aria-label="(index+1)+'. satırı kaldır'">Kaldır</button>
   </div>
   <button type="button" class="button" :disabled="rows.length >= (kind === 'ivr' ? 12 : 100)" @click="add">{{ kind === 'ivr' ? 'Tuş ekle' : 'Üye ekle' }}</button>
  </fieldset>
  <p v-if="!rows.length">{{ kind === 'ivr' ? 'Henüz tuş tanımlanmadı. Tuş ekle düğmesiyle başlayın.' : 'Henüz üye eklenmedi. Üyesiz kuyruk çağrıları bir dahiliye dağıtamaz.' }}</p>
 </section>
</template>
<style scoped>
.setup-rows{margin-top:24px;border-top:1px solid var(--line);padding-top:20px}.setup-rows p{font-size:13px;color:var(--muted);margin:10px 0}.setup-rows fieldset{border:0;padding:0;margin:0}.setup-row{display:flex;align-items:end;gap:12px;flex-wrap:wrap;margin:16px 0}.setup-row label{display:grid;gap:8px;flex:1;min-width:140px;font-size:13px}.setup-row select,.setup-row input{min-width:0;width:100%}.setup-row .paused{display:flex;align-items:center}.paused input{width:16px}
</style>
