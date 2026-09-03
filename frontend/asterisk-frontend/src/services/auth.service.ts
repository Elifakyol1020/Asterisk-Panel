import api from '@/api/axios'
import type { LoginRequest, LoginResponse } from '@/types/auth'

export const authService = {
  async login(payload: LoginRequest): Promise<LoginResponse> {
    return (await api.post<LoginResponse>('/auth/login', payload)).data
  },
}
