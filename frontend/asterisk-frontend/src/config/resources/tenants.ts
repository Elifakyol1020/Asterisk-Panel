import type { ResourceConfig } from '@/types/resource';
import { text, select } from './fields';
export const tenantResource: ResourceConfig = {
    key: 'tenants', title: 'Tenant’lar', singular: 'Tenant', description: 'Kurumları ve platform erişimlerini yönetin.', icon: 'building', api: '/admin/tenants', primary: 'name', softDelete: true,
    columns: [
        {
            key: 'code', label: 'Kısa kod'
        },
        {
            key: 'status', label: 'Durum'
        },
        {
            key: 'createdAt', label: 'Oluşturulma'
        }
    ],
    fields: [
        text('name', 'Kurum adı'), text('code', 'Kısa kod', {
            maxLength: 120, hint: 'Otomatik doldurulur.'
        }), select('status', 'Durum', [
            'ACTIVE', 'INACTIVE'
        ])
    ]
};
export const tenantFields = Object.fromEntries(tenantResource.fields.map(field => [
    field.key, field
]));
