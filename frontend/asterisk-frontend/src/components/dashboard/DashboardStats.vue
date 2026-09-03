<script setup lang="ts">
import AppIcon from '@/components/common/AppIcon.vue'
defineProps<{
  cards: { key: string; title: string; icon: string; to: string }[]
  totals: Record<string, number>
  loading: boolean
  note: string
}>()
</script>

<template>
  <section class="stats-grid" aria-label="Kaynak sayıları">
    <RouterLink v-for="card in cards" :key="card.key" :to="card.to" class="stat-card">
      <div class="stat-top"><span>{{ card.title }}</span><span class="stat-icon"><AppIcon :name="card.icon" :size="19" /></span></div>
      <div v-if="loading" class="skeleton stat-loading" />
      <strong v-else class="stat-value">{{ totals[card.key]?.toLocaleString('tr-TR') ?? '—' }}</strong>
      <span class="stat-note">{{ note }}</span>
    </RouterLink>
  </section>
</template>

<style scoped>
.stat-loading { margin: 20px 0; }
</style>
