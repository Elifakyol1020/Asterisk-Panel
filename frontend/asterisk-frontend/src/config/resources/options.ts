import type { ResourceConfig } from '@/types/resource';
import { select } from './fields';
export const ivrOptionResource: ResourceConfig = {
    key: 'options', title: 'IVR seçenekleri', singular: 'Tuşlama seçeneği', description: 'Her tuş için bir hedef veya çağrı sonlandırma adımı tanımlayın.', icon: 'route', api: '', primary: 'digit',
    columns: [
        {
            key: 'actionType', label: 'İşlem türü'
        },
        {
            key: 'targetId', label: 'Hedef ID'
        }
    ],
    fields: [
        select('digit', 'Tuş', [
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '*', '#'
        ]), select('actionType', 'İşlem', [
            'QUEUE', 'EXTENSION', 'IVR', 'HANGUP'
        ]),
        {
            key: 'targetId', label: 'Hedef kayıt', type: 'select', required: true
        }
    ]
};
export const ivrOptionFields = Object.fromEntries(ivrOptionResource.fields.map(field => [
    field.key, field
]));
