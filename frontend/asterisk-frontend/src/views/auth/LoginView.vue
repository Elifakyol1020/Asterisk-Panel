<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth.store'
import { errorMessage } from '@/api/platform'
import AppIcon from '@/components/common/AppIcon.vue'
const auth = useAuthStore(), router = useRouter(), route = useRoute()
const loading = ref(false), error = ref(''), showPassword = ref(false)
const form = reactive({ email: '', password: '' })
async function submit() {
  if (loading.value) return
  loading.value = true; error.value = ''
  try { await auth.login({ email: form.email.trim().toLowerCase(), password: form.password }); await router.replace(auth.isSuperAdmin ? '/super-admin/dashboard' : '/tenant/dashboard') }
  catch (e) { error.value = errorMessage(e) }
  finally { loading.value = false }
}
</script>
<template>
  <main class="login-page">
    <aside class="login-story">
      <a href="/login" class="brand"><span class="brand-mark">n</span><span>netgsm<span class="brand-caption">SANTRAL YÖNETİMİ</span></span></a>
      <div class="story-content"><span class="eyebrow light">İLETİŞİMİN MERKEZİNDE</span><h1>Tüm bağlantılar.<br>Tek bir <em>merkez.</em></h1><p>Santral altyapınızı, tenant’larınızı ve çağrı yönlendirmelerinizi tek noktadan yönetin.</p>
      <div class="connection-map"><div class="map-node"><AppIcon name="building" /><span>Tenant’lar</span></div><span class="map-line"></span><div class="map-hub"><AppIcon name="network" :size="34" /></div><span class="map-line"></span><div class="map-node"><AppIcon name="phone" /><span>Santraller</span></div></div>
      <div class="story-features"><span><AppIcon name="shield" :size="17" /> Rol bazlı erişim</span><span><AppIcon name="network" :size="17" /> Çoklu tenant yönetimi</span></div></div>
      <div class="story-footer">NETGSM <span>Kurumsal iletişim, sadeleşti.</span></div>
    </aside>
    <section class="login-main"><div class="login-form-wrap"><span class="login-symbol"><AppIcon name="lock" :size="25" /></span><p class="eyebrow">YÖNETİM PANELİ</p><h2>Tekrar hoş geldiniz.</h2><p class="muted">Devam etmek için hesabınıza giriş yapın.</p>
      <form @submit.prevent="submit"><label for="email">E-posta adresi<input id="email" v-model="form.email" type="email" required maxlength="254" autocomplete="username" placeholder="ornek@sirketiniz.com" :disabled="loading" /></label>
      <label for="password">Şifre<span class="password-field"><input id="password" v-model="form.password" :type="showPassword ? 'text' : 'password'" required maxlength="72" autocomplete="current-password" placeholder="Şifrenizi girin" :disabled="loading" /><button type="button" class="icon-button" :aria-label="showPassword ? 'Şifreyi gizle' : 'Şifreyi göster'" :aria-pressed="showPassword" @click="showPassword = !showPassword"><AppIcon name="eye" /></button></span></label>
      <p v-if="route.query.expired && !error" class="notice">Oturumunuz sona erdi. Lütfen yeniden giriş yapın.</p><p v-if="error" class="form-error" role="alert">{{ error }}</p>
      <button class="button button-primary login-submit" :disabled="loading">{{ loading ? 'Giriş yapılıyor…' : 'Giriş yap' }}<AppIcon name="arrow" :size="18" /></button></form>
      <div class="login-help"><AppIcon name="info" :size="18" /><p>Hesabınız veya şifreniz için sistem yöneticinizle iletişime geçin.</p></div>
      <div class="secure-caption"><AppIcon name="shield" :size="16" /> Yetkili kullanıcılar için güvenli erişim</div>
    </div><footer>© {{ new Date().getFullYear() }} Netgsm · Asterisk Yönetim Platformu</footer></section>
  </main>
</template>
