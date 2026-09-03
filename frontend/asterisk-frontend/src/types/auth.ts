export type Role = 'SUPER_ADMIN' | 'TENANT_ADMIN'
export interface User { id: string; username: string; email: string; role: Role; tenantId?: string; exp: number }
export interface LoginRequest { email: string; password: string }
export interface LoginResponse { accessToken: string; tokenType: string }
