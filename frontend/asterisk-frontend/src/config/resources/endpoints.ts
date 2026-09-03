import type { ResourceConfig } from '@/types/resource';
import { text, enabled, password, transport, numberPattern } from './fields';
export const endpointResource: ResourceConfig = {
    key: 'endpoints', title: 'Endpoint’ler', singular: 'Endpoint', description: 'SIP cihazlarını ve bağlantı ayarlarını yönetin.', icon: 'phone', api: '/endpoints', primary: 'displayName',
    columns: [
        {
            key: 'extension', label: 'Dahili'
        },
        {
            key: 'transport', label: 'Transport'
        },
        {
            key: 'enabled', label: 'Durum'
        }
    ],
    fields: [
        text('displayName', 'Görünen ad'), text('extension', 'Dahili numarası', {
            pattern: numberPattern, maxLength: 20
        }), transport, text('codecs', 'Codec’ler', {
            pattern: '[a-zA-Z0-9_,]{1,120}', default: 'alaw,ulaw', hint: 'Virgülle ayırın. Örnek: alaw,ulaw'
        }), password, enabled
    ]
};
export const endpointFields = Object.fromEntries(endpointResource.fields.map(field => [
    field.key, field
]));
