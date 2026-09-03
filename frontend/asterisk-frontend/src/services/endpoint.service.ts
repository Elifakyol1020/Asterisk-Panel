import { createResourceApi } from '@/api/resource'
import type { Endpoint } from '@/types/endpoint'
export default createResourceApi<Endpoint>('endpoints')
