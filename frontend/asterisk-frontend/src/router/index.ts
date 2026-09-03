import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth.store'
import type { Role } from '@/types/auth'
import { superadminRoutes } from './routes/superadmin'
import { tenantRoutes } from './routes/tenant'

const router = createRouter({ history: createWebHistory(), routes: [
  { path: '/', redirect: '/login' },
  { path: '/login', component: () => import('@/views/auth/LoginView.vue'), meta: { guest: true, title: 'Giriş' } },
  { path: '/super-admin', component: () => import('@/layouts/AdminLayout.vue'), meta: { requiresAuth: true, role: 'SUPER_ADMIN' }, children: superadminRoutes },
  { path: '/tenant', component: () => import('@/layouts/AdminLayout.vue'), meta: { requiresAuth: true, role: 'TENANT_ADMIN' }, children: tenantRoutes },
  { path: '/:pathMatch(.*)*', component: () => import('@/views/NotFoundView.vue'), meta: { title: 'Sayfa bulunamadı' } },
], scrollBehavior: () => ({ top: 0 }) })
router.beforeEach(to => {
  const auth = useAuthStore()
  const valid = auth.isAuthenticated && auth.user && auth.user.exp * 1000 > Date.now()
  const home = auth.isSuperAdmin ? '/super-admin/dashboard' : '/tenant/dashboard'
  if (to.meta.guest && valid) return home
  if (to.meta.requiresAuth && !valid) { auth.logout(); return '/login' }
  if (to.meta.role && to.meta.role as Role !== auth.role) return home
})
router.afterEach(to => { document.title = `${to.meta.title || 'Santral Yönetimi'} · Netgsm` })
export default router
