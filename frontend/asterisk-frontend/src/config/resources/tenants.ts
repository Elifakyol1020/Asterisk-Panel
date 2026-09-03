import type { ResourceConfig } from '@/types/resource';
import { text, select } from './fields';
export const tenantResource: ResourceConfig = {
    key: 'tenants', title: 'Tenant’lar', singular: 'Tenant', description: 'Kurumları ve platform erişimlerini yönetin.', icon: 'building', api: '/admin/tenants', primary: 'name', softDelete: true,
    columns: [
        {
            key: 'code', label: 'Tenant kodu'
        },
        {
            key: 'status', label: 'Durum'
        },
        {
            key: 'createdAt', label: 'Oluşturulma'
        }
    ],
    fields: [
        text('name', 'Kurum adı'), text('code', 'Tenant kodu', {
            pattern: '[a-z][a-z0-9_]{1,47}', maxLength: 48, hint: '2–48 karakter; küçük harfle başlar, küçük harf, rakam ve alt çizgi içerir.'
        }), select('status', 'Durum', [
            'ACTIVE', 'INACTIVE'
        ])
    ]
};
export const tenantFields = Object.fromEntries(tenantResource.fields.map(field => [
    field.key, field
]));
