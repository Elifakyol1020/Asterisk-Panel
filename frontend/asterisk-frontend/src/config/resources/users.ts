import type { ResourceConfig } from '@/types/resource';
import { text, enabled, password } from './fields';
export const userResource: ResourceConfig = {
    key: 'users', title: 'Kullanıcılar', singular: 'Kullanıcı', description: 'Seçili tenant’ın yönetici hesaplarını ve erişimlerini yönetin.', icon: 'users', api: '/admin/users', primary: 'username', softDelete: true,
    columns: [
        {
            key: 'email', label: 'E-posta'
        },
        {
            key: 'role', label: 'Rol'
        },
        {
            key: 'enabled', label: 'Durum'
        }
    ],
    fields: [
        text('username', 'Kullanıcı adı', {
            pattern: '[a-zA-Z0-9_.\\-]{3,80}', maxLength: 80
        }), text('email', 'E-posta', {
            type: 'email', maxLength: 254
        }), password, enabled
    ]
};
export const userFields = Object.fromEntries(userResource.fields.map(field => [
    field.key, field
]));
