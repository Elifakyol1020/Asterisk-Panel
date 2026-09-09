import type { ResourceConfig } from '@/types/resource';
import { text, enabled, password, transport, numberPattern } from './fields';
export const endpointResource: ResourceConfig = {
    key: 'endpoints', title: 'Dahililer', singular: 'Dahili', description: 'SIP cihazlarını ve bağlantı ayarlarını yönetin.', icon: 'phone', api: '/endpoints', primary: 'displayName',
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
        }), transport, password, enabled
    ]
};
export const endpointFields = Object.fromEntries(endpointResource.fields.map(field => [
    field.key, field
]));
