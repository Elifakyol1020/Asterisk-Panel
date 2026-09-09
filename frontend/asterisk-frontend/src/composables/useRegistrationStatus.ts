import { ref, watch, onMounted, onBeforeUnmount, type Ref } from 'vue'
import api from '@/api/axios'
import type { RecordData } from '@/api/platform'
export function useRegistrationStatus(rows: Ref<RecordData[]>) {
 const statuses = ref<Record<string,string>>({})
 let request: AbortController | undefined
 let timer: ReturnType<typeof setInterval> | undefined
 async function refresh() {
  if (document.hidden || request || !rows.value.length) return
  const controller = new AbortController();request=controller
  const ids=rows.value.map(row=>row.id).join(',')
  try { const result=await api.get<Record<string,string>>('/endpoints/registration-status',{params:{ids},signal:controller.signal});if(!controller.signal.aborted)statuses.value=result.data }
  catch { if(!controller.signal.aborted)statuses.value=Object.fromEntries(rows.value.map(row=>[String(row.id),'UNKNOWN'])) }
  finally {if(request===controller)request=undefined}
 }
 function stop() {if(timer!==undefined)clearInterval(timer);timer=undefined;request?.abort();request=undefined}
 function start() {stop();if(document.hidden)return;void refresh();timer=setInterval(()=>{void refresh()},5000)}
 watch(()=>rows.value.map(row=>row.id).join(','),()=>{request?.abort();request=undefined;statuses.value={};void refresh()})
 onMounted(()=>{document.addEventListener('visibilitychange',start);start()})
 onBeforeUnmount(()=>{document.removeEventListener('visibilitychange',start);stop()})
 function label(id: unknown) {return ({REGISTERED:'Kayıtlı',UNREGISTERED:'Kayıtlı değil',UNKNOWN:'Durum alınamadı'}[statuses.value[String(id)] || ''] || 'Kontrol ediliyor…')}
 return {statuses,label}
}
