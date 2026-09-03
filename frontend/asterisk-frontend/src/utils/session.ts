import type { User } from '@/types/auth'
// UI routing only. The backend verifies signatures and authorizes every request.
export function readClaims(token: string, email = ''): User | null {
  try {
    const part = token.split('.')[1]
    if (!part) return null
    const value = part.replace(/-/g, '+').replace(/_/g, '/')
    const data = JSON.parse(new TextDecoder().decode(Uint8Array.from(atob(value.padEnd(Math.ceil(value.length / 4) * 4, '=')), c => c.charCodeAt(0))))
    if (!['SUPER_ADMIN', 'TENANT_ADMIN'].includes(data.role) || !Number.isFinite(data.exp) || data.exp * 1000 <= Date.now() || !data.sub) return null
    if (data.role === 'TENANT_ADMIN' && !data.tenantId) return null
    return { id: String(data.sub), role: data.role, tenantId: data.tenantId ? String(data.tenantId) : undefined, email, username: email.split('@')[0] || `Kullanıcı ${data.sub}`, exp: data.exp }
  } catch { return null }
}
