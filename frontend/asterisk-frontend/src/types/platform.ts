export interface BaseRecord { id: number; createdAt: string; updatedAt: string }
export interface Tenant extends BaseRecord { name: string; code: string; status: 'ACTIVE' | 'INACTIVE' }
export interface TenantRecord extends BaseRecord { tenantId: number; enabled: boolean }
export interface Endpoint extends TenantRecord { extension: string; displayName: string; transport: string; codecs: string; context: string }
export interface Trunk extends TenantRecord { name: string; host: string; port: number; username: string; transport: string; fromUser: string | null; fromDomain: string | null; context: string }
export interface Queue extends TenantRecord { name: string; strategy: string; timeout: number; retry: number; wrapupTime: number; maxLength: number; musicOnHold: string }
export interface Ivr extends TenantRecord { name: string; description: string | null; audioFile: string; timeout: number; maxAttempts: number }
export interface Extension extends TenantRecord { extensionNumber: string; name: string; targetType: 'ENDPOINT' | 'QUEUE' | 'IVR' | 'TRUNK' | 'CUSTOM'; targetId: number; context: string }
export interface Dialplan extends TenantRecord { extension: string; priority: number; application: 'Answer' | 'Hangup' | 'Playback' | 'Wait'; applicationData: string; context: string }
