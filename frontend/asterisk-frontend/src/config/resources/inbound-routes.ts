import type { ResourceConfig } from '@/types/resource'
import {text,select,enabled} from './fields'
export const inboundRouteResource: ResourceConfig = {
 key:'inbound-routes',title:'Arama kuralları',singular:'Gelen arama kuralı',description:'Şirket numarasına gelen çağrıları bir dahiliye, kuyruğa veya IVR menüsüne yönlendirin.',icon:'route',api:'/inbound-routes',primary:'name',
 columns:[{key:'did',label:'Aranan şirket numarası'},{key:'trunkId',label:'Kaynak trunk'},{key:'targetType',label:'Hedef türü'}],
 fields:[text('name','Kural adı'),{key:'trunkId',label:'Kaynak trunk',type:'select',required:true},text('did','Aranan şirket numarası (DID)',{pattern:'[+]?[0-9]{1,20}',maxLength:21,hint:'Operatörün gönderdiği biçimi kullanın. Örn. 90850… veya +90850…'}),select('targetType','Hedef türü',['ENDPOINT','QUEUE','IVR']),{key:'targetId',label:'Hedef',type:'select',required:true},enabled]
}
export const inboundFields = Object.fromEntries(inboundRouteResource.fields.map(field=>[field.key,field]))
