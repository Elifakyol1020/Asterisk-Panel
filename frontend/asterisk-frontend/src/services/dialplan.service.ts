import { createResourceApi } from '@/api/resource'
import type { Dialplan } from '@/types/dialplan'
export default createResourceApi<Dialplan>('dialplans')
