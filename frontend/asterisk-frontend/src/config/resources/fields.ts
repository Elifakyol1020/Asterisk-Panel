import type { Field } from '@/types/resource';
export const text = (key: string, label: string, extra: Partial<Field> = {}): Field => ({
    key, label, required: true, maxLength: 120, ...extra
});
export const num = (key: string, label: string, min: number, max: number, value: number): Field => ({
    key, label, type: 'number', required: true, min, max, default: value
});
export const select = (key: string, label: string, options: string[], value?: string): Field => ({
    key, label, type: 'select', required: true, options, default: value || options[0]
});
export const enabled: Field = {
    key: 'enabled', label: 'Kayıt aktif', type: 'checkbox', default: true
};
export const password: Field = {
    key: 'password', label: 'Şifre', type: 'password', required: true, minLength: 12, maxLength: 72, hint: '12–72 UTF-8 byte. Düzenlemede boşsa değişmez.'
};
export const transport = text('transport', 'Transport', {
    pattern: '[a-zA-Z0-9_\\-]{1,80}', maxLength: 80, default: 'transport-udp', hint: 'Asterisk transport adı.'
});
export const numberPattern = '[0-9]{1,20}';
export const identifier = '[a-zA-Z0-9_\\-]{1,80}';
