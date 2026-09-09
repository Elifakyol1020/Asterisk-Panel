<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import api from '@/api/axios'
import PageHeader from '@/components/common/PageHeader.vue'
import InlineFeedback from '@/components/common/InlineFeedback.vue'
import FormField from '@/components/forms/FormField.vue'
import type { Field } from '@/types/resource'

const fields: Field[] = [
  { key:'userAgent', label:'User-Agent', type:'text', maxLength:40, hint:'SIP mesajlarında görünen yazılım adı. Boşsa Asterisk sürümünün varsayılanı kullanılır.' },
  { key:'maxForwards', label:'Max-Forwards', type:'number', min:1, max:255, hint:'SIP isteğinin geçebileceği azami yönlendirme sayısı. Asterisk varsayılanı: 70.' },
  { key:'defaultFromUser', label:'Varsayılan From kullanıcısı', type:'text', maxLength:40, hint:'Daha özel bir arayan bilgisi yoksa kullanılan SIP From kullanıcı adı. Varsayılan: asterisk.' },
  { key:'defaultRealm', label:'Varsayılan realm', type:'text', maxLength:40, hint:'SIP kimlik doğrulama alanı. Varsayılan: asterisk. Değişmesi telefonların kimlik doğrulamasını etkileyebilir.' },
  { key:'keepAliveInterval', label:'Keep-alive aralığı (saniye)', type:'number', min:0, max:2147483647, hint:'TCP/TLS gibi bağlantılı transportların keep-alive aralığı. UDP kayıt süresi değildir. Varsayılan: 90.' },
  { key:'endpointIdentifierOrder', label:'Endpoint tanıma sırası', type:'text', maxLength:40, hint:'Örnek: ip,username,anonymous. Kurulu tanıyıcıları Asterisk üzerinde pjsip show identifiers ile kontrol edin.' },
  { key:'contactExpirationCheckInterval', label:'Contact süre kontrolü (saniye)', type:'number', min:1, max:2147483647, hint:'Süresi dolan SIP contact kayıtlarının kontrol sıklığı. Varsayılan: 30.' },
  { key:'defaultVoicemailExtension', label:'Varsayılan sesli mesaj numarası', type:'text', maxLength:40, hint:'Endpoint/AOR üzerinde belirtilmemişse sesli mesaj bildiriminde kullanılan numara; sesli mesaj servisi oluşturmaz.' },
]
const form = reactive<Record<string,string|number|null>>({})
const revision = ref(''), loading = ref(false), saving = ref(false), error = ref(''), success = ref('')
function message(e: unknown) { return (e as {response?:{data?:{message?:string}}}).response?.data?.message || 'Global ayarlar alınamadı veya kaydedilemedi.' }
async function load() {
  loading.value=true; error.value=''; success.value=''; revision.value=''
  try { const {data}=await api.get('/admin/pjsip-global'); Object.assign(form,data.settings); revision.value=data.revision }
  catch(e) { error.value=message(e) } finally { loading.value=false }
}
async function save() {
  saving.value=true; error.value=''; success.value=''
  const settings=Object.fromEntries(fields.map(f=>[f.key, form[f.key] === '' || form[f.key] == null ? null : f.type === 'number' ? Number(form[f.key]) : form[f.key]]))
  try {
    const {data}=await api.put('/admin/pjsip-global',{revision:revision.value,settings})
    Object.assign(form,data.settings);revision.value=data.revision
    success.value='Veritabanına kaydedildi. Asterisk üzerinde pjsip reload çalıştırıp pjsip show settings ile doğrulayın.'
  } catch(e) {error.value=message(e)} finally {saving.value=false}
}
onMounted(load)
</script>

<template>
  <PageHeader title="PJSIP global ayarları" description="Bu ayarlar santraldeki tüm tenant’ları etkiler. Yalnızca süper admin yönetebilir.">
    <button class="button" :disabled="loading || saving" @click="load">Yeniden yükle</button>
  </PageHeader>
  <InlineFeedback :error="error" :success="success" />
  <section class="panel form-panel">
    <p class="notice">Bu ekran veritabanındaki yapılandırmayı gösterir; Asterisk’in canlı ayarlarını göstermez. Boş alanlar Asterisk varsayılanını kullanır. Kaydettikten sonra reload ve doğrulama gerekir.</p>
    <form @submit.prevent="save">
      <div class="form-grid"><FormField v-for="field in fields" :key="field.key" v-model="form[field.key]" :field="field" :disabled="loading || saving || !revision" /></div>
      <button class="button button-primary save" :disabled="loading || saving || !revision">{{ saving ? 'Kaydediliyor…' : 'Veritabanına kaydet' }}</button>
    </form>
  </section>
  <section class="panel form-panel help">
    <h2>Asterisk’e uygulama</h2>
    <ol>
      <li>İlk kurulumda Sorcery ve Realtime eşleştirmesini ps_globals tablosuna bağlayın.</li>
      <li>Tabloda ve dosya yapılandırmasında birden fazla global nesne olmadığını kontrol edin.</li>
      <li>Asterisk CLI’de <code>pjsip reload</code> çalıştırın.</li>
      <li><code>pjsip show settings</code> çıktısında değiştirdiğiniz değerleri doğrulayın.</li>
    </ol>
    <p>Kurulum rehberi: docs/PJSIP-GLOBALS.md. Bu sayfa AMI, RTP portları veya transport ayarlarını değiştirmez.</p>
  </section>
</template>

<style scoped>
.notice{margin-bottom:24px;color:var(--muted);line-height:1.6}.save{margin-top:24px}.help{margin-top:24px}.help h2{font-size:18px;margin-bottom:16px}.help ol{padding-left:22px;line-height:1.9}.help p{margin-top:12px;color:var(--muted)}
</style>
