import type { RouteRecordRaw } from 'vue-router'
import { pbxRoutes } from './pbx'

export const superadminRoutes: RouteRecordRaw[] = [
  { path: '', redirect: '/super-admin/dashboard' },
  {
    path: 'dashboard',
    component: () => import('@/views/superadmin/DashboardView.vue'),
    meta: { title: 'Genel bakış' },
  },
  {
    path: 'tenants',
    component: () => import('@/views/superadmin/tenants/TenantListView.vue'),
    meta: { title: 'Tenant’lar' },
  },
  {
    path: 'tenants/create',
    component: () => import('@/views/superadmin/tenants/TenantCreateView.vue'),
    meta: { title: 'Tenant oluştur' },
  },
  {
    path: 'tenants/:id/edit',
    component: () => import('@/views/superadmin/tenants/TenantEditView.vue'),
    meta: { title: 'Tenant düzenle' },
  },
  {
    path: 'users',
    component: () => import('@/views/superadmin/users/UserListView.vue'),
    meta: { title: 'Kullanıcılar' },
  },
  {
    path: 'users/create',
    component: () => import('@/views/superadmin/users/UserCreateView.vue'),
    meta: { title: 'Kullanıcı oluştur' },
  },
  {
    path: 'users/:id/edit',
    component: () => import('@/views/superadmin/users/UserEditView.vue'),
    meta: { title: 'Kullanıcı düzenle' },
  },
  ...pbxRoutes,
]
