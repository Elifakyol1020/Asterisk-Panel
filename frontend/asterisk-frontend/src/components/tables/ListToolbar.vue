<script setup lang="ts">
import AppIcon from '@/components/common/AppIcon.vue'
defineProps<{ total: number; loading?: boolean }>()
const search = defineModel<string>('search', { required: true })
const sort = defineModel<string>('sort', { required: true })
</script>

<template>
  <div class="toolbar">
    <div class="search-field">
      <AppIcon name="search" :size="17" />
      <input v-model="search" type="search" aria-label="Bu sayfadaki kayıtlarda ara" placeholder="Bu sayfada ara…" />
    </div>
    <slot />
    <select v-model="sort" aria-label="Sıralama" :disabled="loading">
      <option value="id,desc">En yeni önce</option>
      <option value="id,asc">En eski önce</option>
    </select>
    <span class="toolbar-note">{{ total.toLocaleString('tr-TR') }} kayıt</span>
  </div>
</template>
