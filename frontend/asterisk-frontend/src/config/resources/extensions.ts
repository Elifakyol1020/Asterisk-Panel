import type { ResourceConfig } from '@/types/resource';
import { text, select, enabled, numberPattern } from './fields';
export const extensionResource: ResourceConfig = {
    key: 'extensions', title: 'Arama kuralları', singular: 'Arama kuralı', description: 'Aranan numaraları dahililere, kuyruklara, IVR menülerine veya trunk bağlantılarına yönlendirin.', icon: 'route', api: '/extensions', primary: 'name',
    columns: [
        {
            key: 'extensionNumber', label: 'Numara'
        },
        {
            key: 'targetType', label: 'Hedef türü'
        },
        {
            key: 'enabled', label: 'Durum'
        }
    ],
    fields: [
        text('name', 'Kural adı'), text('extensionNumber', 'Aranacak numara', {
            pattern: numberPattern, maxLength: 20
        }), select('targetType', 'Hedef türü', [
            'ENDPOINT', 'QUEUE', 'IVR', 'TRUNK'
        ]),
        {
            key: 'targetId', label: 'Hedef kayıt', type: 'select', required: true
        },
        enabled
    ]
};
export const extensionFields = Object.fromEntries(extensionResource.fields.map(field => [
    field.key, field
]));
