import axios from 'axios'
import api from './axios'
export type RecordData = Record<string, string | number | boolean | null | undefined>
export interface Page<T = RecordData> { content: T[]; totalElements: number; totalPages: number; number: number; size: number }
export async function listPage(path: string, params: Record<string, unknown> = {}) {
  return (await api.get<Page>(path, { params: { page: 0, size: 10, sort: 'id,desc', ...params } })).data
}
export function errorMessage(error: unknown) {
  if (axios.isAxiosError(error)) {
    if (!error.response) return 'Sunucuya ulaşılamıyor. Backend bağlantısını ve CORS ayarlarını kontrol edin.'
    const status = error.response.status
    if (status === 401) return 'E-posta veya şifre hatalı ya da oturumunuzun süresi doldu.'
    if (status === 403) return 'Bu işlem için yetkiniz bulunmuyor.'
    if (status === 503) return 'Sunucu geçici olarak kullanılamıyor. Bağlantı düzeldikten sonra işlem sonucunu kontrol edin.'
    if (status === 400 && error.response.data?.error === 'VALIDATION_ERROR') return 'Formdaki hatalı alanları kontrol edin.'
    if (status === 400 && error.response.data?.message === 'Tenant is inactive') return 'Bu tenant pasif. Santral kayıtlarını değiştirmek için önce tenant’ı aktifleştirin.'
    if (status === 409 && error.response.data?.message === 'Record changed; reload and retry') return 'Kayıt başka bir işlem tarafından değiştirildi. Listeyi yenileyip tekrar deneyin.'
    if (status === 409) return 'Bu kayıt zaten mevcut veya başka kayıtlarda kullanılıyor. Bağlantıları kontrol edin.'
    if (status === 404) return 'Kayıt bulunamadı. Listeyi yenileyerek tekrar deneyin.'
    return error.response.data?.message || 'İşlem tamamlanamadı. Lütfen tekrar deneyin.'
  }
  return error instanceof Error ? error.message : 'Beklenmeyen bir hata oluştu.'
}
export function fieldErrors(error: unknown): Record<string, string> {
  return axios.isAxiosError(error) ? error.response?.data?.errors || {} : {}
}
