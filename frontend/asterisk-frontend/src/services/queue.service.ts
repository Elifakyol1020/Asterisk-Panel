import { createResourceApi } from '@/api/resource'
import type { Queue } from '@/types/queue'
export default createResourceApi<Queue>('queues')
