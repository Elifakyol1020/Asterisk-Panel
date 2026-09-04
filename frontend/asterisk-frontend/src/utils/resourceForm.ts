import type { Field } from '@/config/resources'
import type { RecordData } from '@/api/platform'

export function normalizeTenantCode(value: unknown): string {
  return String(value ?? '')
    .trim()
    .toLocaleLowerCase('tr-TR')
    .replace(/ı/g, 'i')
    .replace(/ğ/g, 'g')
    .replace(/ü/g, 'u')
    .replace(/ş/g, 's')
    .replace(/ö/g, 'o')
    .replace(/ç/g, 'c')
    .normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '')
    .replace(/[^a-z0-9]+/g, '_')
    .replace(/_+/g, '_')
    .replace(/^_+|_+$/g, '')
    .slice(0, 48)
    .replace(/_+$/g, '')
}

export function buildPayload(fields: Field[], form: RecordData, editing: boolean, key: string, superAdmin: boolean, tenantId?: number): RecordData {
  const data: RecordData = {}
  for (const field of fields) {
    let value = form[field.key]
    if (field.type === 'password' && editing && !value) continue
    if (field.type === 'number' || field.key === 'targetId' || field.key === 'endpointId') value = Number(value)
    else if (typeof value === 'string' && field.type !== 'password') value = value.trim()
    if (key === 'tenants' && field.key === 'code') value = normalizeTenantCode(value)
    data[field.key] = value
  }
  if (key === 'options' && form.actionType === 'HANGUP') data.targetId = null
  if (superAdmin && !['tenants', 'users', 'members', 'options'].includes(key)) data.tenantId = tenantId
  return data
}
export function validatePayload(data: RecordData, key: string): Record<string, string> {
  const errors: Record<string, string> = {}
  if (data.password && new TextEncoder().encode(String(data.password)).length > 72) errors.password = 'Şifre en fazla 72 UTF-8 byte olabilir.'
  if (key === 'dialplans') {
    const value = String(data.applicationData || '')
    const valid = ['Answer', 'Hangup'].includes(String(data.application)) ? value === '' : data.application === 'Playback' ? /^[a-zA-Z0-9_-]{1,120}$/.test(value) : data.application === 'Wait' && /^[0-9]{1,3}$/.test(value) && Number(value) <= 300
    if (!valid) errors.applicationData = 'Bu uygulama için geçersiz parametre. Alanın altındaki kuralları kontrol edin.'
  }
  return errors
}
