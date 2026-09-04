import type { ResourceConfig } from '@/types/resource';
import { text, num, select, enabled, numberPattern } from './fields';
export const dialplanResource: ResourceConfig = {
    key: 'dialplans', title: 'Arama planları', singular: 'Arama planı', description: 'Güvenli Asterisk uygulamalarıyla çağrı akışlarını tanımlayın.', icon: 'route', api: '/dialplans', primary: 'extension',
    columns: [
        {
            key: 'priority', label: 'Öncelik'
        },
        {
            key: 'application', label: 'Uygulama'
        },
        {
            key: 'enabled', label: 'Durum'
        }
    ],
    fields: [
        text('extension', 'Dahili numarası', {
            pattern: numberPattern, maxLength: 20
        }), num('priority', 'Öncelik', 1, 1000, 1), select('application', 'Uygulama', [
            'Answer', 'Hangup', 'Playback', 'Wait'
        ]), text('applicationData', 'Uygulama parametresi', {
            required: false, hint: 'Answer/Hangup boş; Playback ses adı; Wait 0–300 sn.'
        }), enabled
    ]
};
export const dialplanFields = Object.fromEntries(dialplanResource.fields.map(field => [
    field.key, field
]));
