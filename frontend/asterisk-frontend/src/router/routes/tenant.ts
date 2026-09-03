import type { RouteRecordRaw } from 'vue-router'
import { pbxRoutes } from './pbx'

export const tenantRoutes: RouteRecordRaw[] = [
  { path: '', redirect: '/tenant/dashboard' },
  {
    path: 'dashboard',
    component: () => import('@/views/tenant/DashboardView.vue'),
    meta: { title: 'Genel bakış' },
  },
  ...pbxRoutes,
]
