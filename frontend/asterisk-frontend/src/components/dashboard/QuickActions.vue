<script setup lang="ts">
import AppIcon from '@/components/common/AppIcon.vue'
defineProps<{ items: { to: string; icon: string; title: string; description: string }[] }>()
</script>

<template>
  <section class="panel">
    <header class="panel-heading"><h2>Hızlı işlemler</h2><AppIcon name="plus" :size="17" /></header>
    <div class="quick-actions">
      <RouterLink v-for="item in items" :key="item.to" :to="item.to" class="quick-action">
        <AppIcon :name="item.icon" :size="21" />
        <div><strong>{{ item.title }}</strong><small>{{ item.description }}</small></div>
        <AppIcon name="chevron" :size="15" />
      </RouterLink>
    </div>
  </section>
</template>
