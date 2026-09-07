import type { ResourceConfig } from '@/types/resource';
import { text, select } from './fields';
export const tenantResource: ResourceConfig = {
    key: 'tenants', title: 'Tenant’lar', singular: 'Tenant', description: 'Kurumları ve platform erişimlerini yönetin.', icon: 'building', api: '/admin/tenants', primary: 'name', softDelete: true,
    columns: [
        {
            key: 'code', label: 'Santral numarası'
        },
        {
            key: 'status', label: 'Durum'
        },
        {
            key: 'createdAt', label: 'Oluşturulma'
        }
    ],
    fields: [
        text('name', 'Kurum adı'), text('code', 'Santral numarası', {
            maxLength: 48, pattern: '[0-9]{1,48}', hint: 'Benzersiz santral numarası. Yalnızca rakam girin; kaydedildikten sonra değiştirilemez.'
        }), select('status', 'Durum', [
            'ACTIVE', 'INACTIVE'
        ])
    ]
};
export const tenantFields = Object.fromEntries(tenantResource.fields.map(field => [
    field.key, field
]));
