import api from '@/api/axios'
import { listPage, type RecordData } from '@/api/platform'

/** Shared HTTP operations. Domain paths are supplied by each resource definition. */
export const resourceService = {
  list: listPage,

  async all(path: string, params: Record<string, unknown> = {}) {
    const first = await listPage(path, { size: 100, ...params })
    const records = [...first.content]
    for (let page = 1; page < first.totalPages; page++) {
      const result = await listPage(path, { size: 100, page, ...params })
      records.push(...result.content)
    }
    return records
  },

  async get(path: string) {
    return (await api.get<RecordData>(path)).data
  },

  async create(path: string, data: RecordData) {
    return (await api.post<RecordData>(path, data)).data
  },

  async update(path: string, data: RecordData) {
    return (await api.put<RecordData>(path, data)).data
  },

  async remove(path: string) {
    await api.delete(path)
  },
}
