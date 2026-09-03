<script setup lang="ts">
import AppIcon from './AppIcon.vue'
defineProps<{ loading?: boolean; icon?: string; title?: string; description?: string }>()
</script>

<template>
  <div class="empty-state" :role="loading ? 'status' : undefined">
    <template v-if="loading">Kayıtlar yükleniyor…</template>
    <template v-else>
      <AppIcon v-if="icon" :name="icon" :size="32" />
      <h3>{{ title }}</h3>
      <p>{{ description }}</p>
      <slot />
    </template>
  </div>
</template>
