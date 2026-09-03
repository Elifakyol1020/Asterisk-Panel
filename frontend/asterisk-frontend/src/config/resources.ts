import type { ResourceConfig } from '@/types/resource'
import { tenantResource } from './resources/tenants'
import { userResource } from './resources/users'
import { endpointResource } from './resources/endpoints'
import { trunkResource } from './resources/trunks'
import { queueResource } from './resources/queues'
import { ivrResource } from './resources/ivrs'
import { extensionResource } from './resources/extensions'
import { dialplanResource } from './resources/dialplans'
import { queueMemberResource } from './resources/members'
import { ivrOptionResource } from './resources/options'

export type { Field, ResourceConfig } from '@/types/resource'
export { recordLabel, displayValue } from '@/utils/display'

export const resources: Record<string, ResourceConfig> = {
  tenants: tenantResource,
  users: userResource,
  endpoints: endpointResource,
  trunks: trunkResource,
  queues: queueResource,
  ivrs: ivrResource,
  extensions: extensionResource,
  dialplans: dialplanResource,
  members: queueMemberResource,
  options: ivrOptionResource,
}
export const pbxKeys = ['endpoints', 'trunks', 'queues', 'ivrs', 'extensions', 'dialplans']
