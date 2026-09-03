import api from './axios'
import type { Page } from './platform'

export interface ResourceApi<T> {
  getAll: () => Promise<T[]>
  getById: (id: string) => Promise<T>
  create: (data: T) => Promise<T>
  update: (id: string, data: T) => Promise<T>
  delete: (id: string) => Promise<void>
}

export function createResourceApi<T>(resource: string): ResourceApi<T> {
  return {
    async getAll() {
      const first = (await api.get<Page<T>>(`/${resource}`, { params: { page: 0, size: 100 } })).data
      const rows = [...first.content]
      for (let page = 1; page < first.totalPages; page++) rows.push(...(await api.get<Page<T>>(`/${resource}`, { params: { page, size: 100 } })).data.content)
      return rows
    },
    async getById(id) { return (await api.get<T>(`/${resource}/${id}`)).data },
    async create(data) { return (await api.post<T>(`/${resource}`, data)).data },
    async update(id, data) { return (await api.put<T>(`/${resource}/${id}`, data)).data },
    async delete(id) { await api.delete(`/${resource}/${id}`) },
  }
}
