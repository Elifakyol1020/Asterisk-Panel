import type { ResourceConfig } from '@/types/resource';
import { text, num, enabled } from './fields';
export const ivrResource: ResourceConfig = {
    key: 'ivrs', title: 'Sesli yanıt (IVR)', singular: 'IVR', description: 'Karşılama anonslarını ve tuşlama seçeneklerini yönetin.', icon: 'mic', api: '/ivrs', primary: 'name',
    columns: [
        {
            key: 'audioFile', label: 'Ses dosyası'
        },
        {
            key: 'timeout', label: 'Bekleme (sn)'
        },
        {
            key: 'enabled', label: 'Durum'
        }
    ],
    fields: [
        text('name', 'IVR adı'), text('audioFile', 'Ses dosyası adı', {
            pattern: '[a-zA-Z0-9_\\-]{1,120}', hint: 'Sunucudaki ses adı; uzantı veya dizin yolu eklemeyin.'
        }), num('timeout', 'Tuşlama bekleme (sn)', 1, 3600, 10), num('maxAttempts', 'Maksimum deneme', 1, 20, 3), text('description', 'Açıklama', {
            type: 'textarea', required: false, maxLength: 1000
        }), enabled
    ]
};
export const ivrFields = Object.fromEntries(ivrResource.fields.map(field => [
    field.key, field
]));
