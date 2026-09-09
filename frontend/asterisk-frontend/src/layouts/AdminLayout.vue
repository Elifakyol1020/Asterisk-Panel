<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth.store'
import { resources, pbxKeys } from '@/config/resources'
import ThemePicker from '@/components/common/ThemePicker.vue'
import AppIcon from '@/components/common/AppIcon.vue'
const auth = useAuthStore(), route = useRoute(), router = useRouter(), mobileOpen = ref(false)
const prefix = computed(() => auth.isSuperAdmin ? '/super-admin' : '/tenant')
const items = computed(() => [
  { title: 'Genel bakış', path: `${prefix.value}/dashboard`, icon: 'grid' },
  { title: 'Çağrı kayıtları', path: `${prefix.value}/cdr`, icon: 'phone' },
  ...(auth.isSuperAdmin ? ['tenants', 'users'] : []).concat(pbxKeys).map(key => ({ title: resources[key]!.title, path: `${prefix.value}/${key}`, icon: resources[key]!.icon })),
])
const groups = computed(() => [
  { title: 'GENEL', items: items.value.slice(0, 2) },
  ...(auth.isSuperAdmin ? [{ title: 'ORGANİZASYON', items: items.value.slice(2, 4) }] : []),
  { title: 'SANTRAL', items: items.value.slice(auth.isSuperAdmin ? 4 : 2) },
  ...(auth.isSuperAdmin ? [{ title: 'SİSTEM', items: [{ title: 'PJSIP global ayarları', path: '/super-admin/pjsip-global', icon: 'shield' }] }] : []),
])
watch(() => route.fullPath, () => { mobileOpen.value = false })
function logout() { auth.logout(); router.replace('/login') }
</script>
<template><div class="app-shell"><button v-if="mobileOpen" class="sidebar-overlay" aria-label="Menüyü kapat" @click="mobileOpen = false"></button>
  <aside class="sidebar" :class="{ 'is-open': mobileOpen }"><RouterLink :to="`${prefix}/dashboard`" class="brand"><span class="brand-mark">n</span><span>netgsm<span class="brand-caption">SANTRAL YÖNETİMİ</span></span></RouterLink>
  <div class="workspace-label"><AppIcon :name="auth.isSuperAdmin ? 'shield' : 'building'" /><div>{{ auth.isSuperAdmin ? 'Platform yönetimi' : 'Santral yönetimi' }}<small>{{ auth.isSuperAdmin ? 'Süperadmin çalışma alanı' : `Tenant #${auth.user?.tenantId}` }}</small></div></div>
  <nav aria-label="Ana menü"><div v-for="group in groups" :key="group.title" class="nav-group"><div class="nav-section-label">{{ group.title }}</div><RouterLink v-for="item in group.items" :key="item.path" :to="item.path" class="nav-link"><AppIcon :name="item.icon" :size="18" />{{ item.title }}</RouterLink></div></nav>
  <div class="sidebar-bottom"><div class="sidebar-note"><AppIcon name="shield" :size="20" /><strong>{{ auth.isSuperAdmin ? 'Merkezi kontrol' : 'Size özel çalışma alanı' }}</strong>{{ auth.isSuperAdmin ? 'Tenant’lar ve santral kaynakları tek bir yerde.' : 'Yalnızca kurumunuza ait santral kaynakları.' }}</div><div class="sidebar-version"><span>Asterisk Platform</span><span>v1.0</span></div></div></aside>
  <section class="content-shell"><header class="topbar"><div class="breadcrumb"><button class="icon-button mobile-toggle" aria-label="Menüyü aç" :aria-expanded="mobileOpen" @click="mobileOpen = !mobileOpen"><AppIcon name="menu" /></button><span>Çalışma alanı</span><AppIcon name="chevron" :size="13" /><strong>{{ route.meta.title || 'Genel bakış' }}</strong></div><div class="topbar-tools"><ThemePicker /><div class="user-menu"><div class="avatar">{{ auth.user?.username.slice(0, 2).toUpperCase() }}</div><div>{{ auth.user?.username }}<small>{{ auth.isSuperAdmin ? 'Süperadmin' : 'Tenant admin' }}</small></div><button class="icon-button" title="Güvenli çıkış" aria-label="Güvenli çıkış" @click="logout"><AppIcon name="logout" :size="19" /></button></div></div></header>
  <main class="page-content"><RouterView :key="route.path" /><footer class="page-footer"><span>© {{ new Date().getFullYear() }} Netgsm · Santral Yönetim Platformu</span><span>{{ auth.isSuperAdmin ? 'Platform genelinde yönetim' : 'Tenant kapsamlı erişim' }}</span></footer></main></section>
</div></template>
