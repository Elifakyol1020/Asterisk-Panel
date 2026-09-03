import type { ResourceConfig } from '@/types/resource';
import { text, num, enabled, password, transport } from './fields';
export const trunkResource: ResourceConfig = {
    key: 'trunks', title: 'Trunk’lar', singular: 'Trunk', description: 'Operatör bağlantılarını ve SIP trunk ayarlarını yönetin.', icon: 'network', api: '/trunks', primary: 'name',
    columns: [
        {
            key: 'host', label: 'Sunucu'
        },
        {
            key: 'port', label: 'Port'
        },
        {
            key: 'enabled', label: 'Durum'
        }
    ],
    fields: [
        text('name', 'Trunk adı'), text('host', 'Sunucu adresi', {
            maxLength: 253, pattern: '[a-zA-Z0-9.:\\-]{1,253}'
        }), num('port', 'Port', 1, 65535, 5060), text('username', 'SIP kullanıcı adı', {
            maxLength: 80, pattern: '[a-zA-Z0-9_.\\-]{1,80}'
        }), password, transport, text('fromUser', 'From user', {
            required: false, maxLength: 80
        }), text('fromDomain', 'From domain', {
            required: false, maxLength: 253
        }), enabled
    ]
};
export const trunkFields = Object.fromEntries(trunkResource.fields.map(field => [
    field.key, field
]));
