import type { RouteRecordRaw } from 'vue-router'

// The same PBX features are available in both role scopes; each route has its own page.
export const pbxRoutes: RouteRecordRaw[] = [
  {
    path: 'endpoints',
    component: () => import('@/views/pbx/endpoints/EndpointListView.vue'),
    meta: { title: 'Endpoint’ler' },
  },
  {
    path: 'endpoints/create',
    component: () => import('@/views/pbx/endpoints/EndpointCreateView.vue'),
    meta: { title: 'Endpoint oluştur' },
  },
  {
    path: 'endpoints/:id/edit',
    component: () => import('@/views/pbx/endpoints/EndpointEditView.vue'),
    meta: { title: 'Endpoint düzenle' },
  },
  {
    path: 'trunks',
    component: () => import('@/views/pbx/trunks/TrunkListView.vue'),
    meta: { title: 'Trunk’lar' },
  },
  {
    path: 'trunks/create',
    component: () => import('@/views/pbx/trunks/TrunkCreateView.vue'),
    meta: { title: 'Trunk oluştur' },
  },
  {
    path: 'trunks/:id/edit',
    component: () => import('@/views/pbx/trunks/TrunkEditView.vue'),
    meta: { title: 'Trunk düzenle' },
  },
  {
    path: 'queues',
    component: () => import('@/views/pbx/queues/QueueListView.vue'),
    meta: { title: 'Çağrı kuyrukları' },
  },
  {
    path: 'queues/create',
    component: () => import('@/views/pbx/queues/QueueCreateView.vue'),
    meta: { title: 'Kuyruk oluştur' },
  },
  {
    path: 'queues/:id/edit',
    component: () => import('@/views/pbx/queues/QueueEditView.vue'),
    meta: { title: 'Kuyruk düzenle' },
  },
  {
    path: 'ivrs',
    component: () => import('@/views/pbx/ivrs/IvrListView.vue'),
    meta: { title: 'Sesli yanıt (IVR)' },
  },
  {
    path: 'ivrs/create',
    component: () => import('@/views/pbx/ivrs/IvrCreateView.vue'),
    meta: { title: 'IVR oluştur' },
  },
  {
    path: 'ivrs/:id/edit',
    component: () => import('@/views/pbx/ivrs/IvrEditView.vue'),
    meta: { title: 'IVR düzenle' },
  },
  {
    path: 'extensions',
    component: () => import('@/views/pbx/extensions/ExtensionListView.vue'),
    meta: { title: 'Dahililer' },
  },
  {
    path: 'extensions/create',
    component: () => import('@/views/pbx/extensions/ExtensionCreateView.vue'),
    meta: { title: 'Dahili oluştur' },
  },
  {
    path: 'extensions/:id/edit',
    component: () => import('@/views/pbx/extensions/ExtensionEditView.vue'),
    meta: { title: 'Dahili düzenle' },
  },
  {
    path: 'dialplans',
    component: () => import('@/views/pbx/dialplans/DialplanListView.vue'),
    meta: { title: 'Arama planları' },
  },
  {
    path: 'dialplans/create',
    component: () => import('@/views/pbx/dialplans/DialplanCreateView.vue'),
    meta: { title: 'Arama planı oluştur' },
  },
  {
    path: 'dialplans/:id/edit',
    component: () => import('@/views/pbx/dialplans/DialplanEditView.vue'),
    meta: { title: 'Arama planı düzenle' },
  },
  {
    path: 'queues/:parentId/members',
    component: () => import('@/views/pbx/queues/members/QueueMemberListView.vue'),
    meta: { title: 'Kuyruk üyeleri' },
  },
  {
    path: 'queues/:parentId/members/create',
    component: () => import('@/views/pbx/queues/members/QueueMemberCreateView.vue'),
    meta: { title: 'Kuyruk üyesi oluştur' },
  },
  {
    path: 'ivrs/:parentId/options',
    component: () => import('@/views/pbx/ivrs/options/IvrOptionListView.vue'),
    meta: { title: 'IVR seçenekleri' },
  },
  {
    path: 'ivrs/:parentId/options/create',
    component: () => import('@/views/pbx/ivrs/options/IvrOptionCreateView.vue'),
    meta: { title: 'Tuşlama seçeneği oluştur' },
  },
  {
    path: 'ivrs/:parentId/options/:id/edit',
    component: () => import('@/views/pbx/ivrs/options/IvrOptionEditView.vue'),
    meta: { title: 'Tuşlama seçeneği düzenle' },
  },
]
