import { createResourceApi } from '@/api/resource'
import type { Tenant } from '@/types/tenant'
export default createResourceApi<Tenant>('admin/tenants')
