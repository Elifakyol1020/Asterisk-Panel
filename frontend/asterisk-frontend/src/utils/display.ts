import type { RecordData } from '@/api/platform'

export function recordLabel(row: RecordData): string { return String(row.displayName || row.name || row.username || row.extension || row.extensionNumber || `#${row.id}`) }
export function displayValue(value: unknown, key = ''): string {
  if (value === null || value === undefined || value === '') return '—'
  if (key.endsWith('At')) return new Intl.DateTimeFormat('tr-TR', { day: '2-digit', month: 'short', year: 'numeric' }).format(new Date(String(value)))
  const labels: Record<string, string> = { ACTIVE: 'Aktif', INACTIVE: 'Pasif', TENANT_ADMIN: 'Tenant admin', SUPER_ADMIN: 'Süperadmin', ENDPOINT: 'Dahili', QUEUE: 'Kuyruk', IVR: 'Sesli yanıt menüsü', TRUNK: 'Trunk', HANGUP: 'Çağrıyı sonlandır', EXTENSION: 'İç arama kuralı' }
  if (typeof value === 'boolean') return key === 'paused' ? (value ? 'Evet' : 'Hayır') : (value ? 'Aktif' : 'Pasif')
  return labels[String(value)] || String(value)
}
