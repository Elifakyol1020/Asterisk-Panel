import api from '@/api/axios'
import type { Page } from '@/api/platform'
import type { CdrRecord } from '@/types/cdr'
export const cdrService = {
  async list(params: Record<string, unknown>, signal?: AbortSignal) {
    return (await api.get<Page<CdrRecord>>('/cdr', { params, signal })).data
  },
  async get(id: string, signal?: AbortSignal) {
    return (await api.get<CdrRecord>(`/cdr/${encodeURIComponent(id)}`, { signal })).data
  },
}
