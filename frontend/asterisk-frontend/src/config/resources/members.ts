import type { ResourceConfig } from '@/types/resource';
import { num } from './fields';
export const queueMemberResource: ResourceConfig = {
    key: 'members', title: 'Kuyruk üyeleri', singular: 'Kuyruk üyesi', description: 'Bu kuyruğa ait SIP endpoint’lerini yönetin.', icon: 'users', api: '', primary: 'endpointId',
    columns: [
        {
            key: 'penalty', label: 'Ceza puanı'
        },
        {
            key: 'paused', label: 'Duraklatıldı'
        }
    ],
    fields: [
        {
            key: 'endpointId', label: 'Endpoint', type: 'select', required: true
        },
        num('penalty', 'Ceza puanı', 0, 1000, 0),
        {
            key: 'paused', label: 'Üyeyi duraklat', type: 'checkbox', default: false
        }
    ]
};
export const queueMemberFields = Object.fromEntries(queueMemberResource.fields.map(field => [
    field.key, field
]));
