import type { ResourceConfig } from '@/types/resource';
import { text, num, select, enabled, identifier } from './fields';
export const queueResource: ResourceConfig = {
    key: 'queues', title: 'Çağrı kuyrukları', singular: 'Kuyruk', description: 'Çağrı dağıtım stratejilerini ve kuyruk üyelerini düzenleyin.', icon: 'queue', api: '/queues', primary: 'name',
    columns: [
        {
            key: 'strategy', label: 'Strateji'
        },
        {
            key: 'timeout', label: 'Zaman aşımı (sn)'
        },
        {
            key: 'enabled', label: 'Durum'
        }
    ],
    fields: [
        text('name', 'Kuyruk adı', {
            pattern: identifier, maxLength: 80
        }), select('strategy', 'Dağıtım stratejisi', [
            'ringall', 'leastrecent', 'fewestcalls', 'random', 'rrmemory', 'linear', 'wrandom'
        ]), num('timeout', 'Zaman aşımı (sn)', 1, 3600, 30), num('retry', 'Tekrar bekleme (sn)', 1, 3600, 5), num('wrapupTime', 'Çağrı sonrası süre (sn)', 0, 3600, 0), num('maxLength', 'Maksimum çağrı (0 = sınırsız)', 0, 100000, 0), text('musicOnHold', 'Bekleme müziği sınıfı', {
            pattern: identifier, maxLength: 80, default: 'default'
        }), enabled
    ]
};
export const queueFields = Object.fromEntries(queueResource.fields.map(field => [
    field.key, field
]));
