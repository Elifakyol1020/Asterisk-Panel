<script setup lang="ts">
import {ref,watch,onBeforeUnmount} from 'vue'
import type {RecordData} from '@/api/platform'
import {errorMessage} from '@/api/platform'
import {resourceService} from '@/services/resource.service'
import {recordLabel} from '@/utils/display'
import {inboundFields as fields} from '@/config/resources/inbound-routes'
import FormField from './FormField.vue'
const props=defineProps<{form:RecordData;errors:Record<string,string>;scope?:number;targets:RecordData[];lookupLoading:boolean;disabled?:boolean;editing?:boolean}>()
const trunks=ref<RecordData[]>([]),loading=ref(false),error=ref('')
let version=0
async function load(){const id=++version;trunks.value=[];error.value='';loading.value=false;if(!props.scope)return;loading.value=true;try{const result=await resourceService.all('/trunks',{tenantId:props.scope});if(id===version)trunks.value=result}catch(e){if(id===version)error.value=errorMessage(e)}finally{if(id===version)loading.value=false}}
watch(()=>props.scope,(next,old)=>{if(old!==undefined && next!==old)props.form.trunkId='';void load()},{immediate:true})
onBeforeUnmount(()=>{version++})
</script>
<template>
 <FormField v-model="form.name" :field="fields.name!" :error="errors.name" :disabled="disabled" />
 <FormField v-model="form.trunkId" :field="fields.trunkId!" :error="errors.trunkId" :disabled="disabled || loading" :options="trunks.map(t=>({value:Number(t.id),label:recordLabel(t)}))" placeholder="Trunk seçin" />
 <p v-if="error" role="alert">{{error}} <button type="button" class="button" @click="load">Tekrar dene</button></p>
 <FormField v-model="form.did" :field="fields.did!" :error="errors.did" :disabled="disabled" />
 <FormField v-model="form.targetType" :field="fields.targetType!" :error="errors.targetType" :disabled="disabled" />
 <FormField v-model="form.targetId" :field="fields.targetId!" :error="errors.targetId" :disabled="disabled || lookupLoading" :options="targets.map(t=>({value:Number(t.id),label:recordLabel(t)}))" placeholder="Hedef seçin" />
 <FormField v-model="form.enabled" :field="fields.enabled!" :error="errors.enabled" :disabled="disabled" />
</template>
