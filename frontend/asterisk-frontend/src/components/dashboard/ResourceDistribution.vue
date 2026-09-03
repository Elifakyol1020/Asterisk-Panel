<script setup lang="ts">
import { computed } from 'vue'
import { resources, pbxKeys } from '@/config/resources'
import AppIcon from '@/components/common/AppIcon.vue'
const props = defineProps<{ totals: Record<string, number>; prefix: string; loading: boolean }>()
const maxTotal = computed(() => Math.max(1, ...pbxKeys.map(key => props.totals[key] || 0)))
</script>

<template>
  <section class="panel">
    <header class="panel-heading">
      <div><h2>Santral kaynak dağılımı</h2><p>Kayıtlı yapılandırmalar · canlı çağrı verisi değildir</p></div>
      <AppIcon name="network" :size="19" />
    </header>
    <div class="resource-bars">
      <RouterLink v-for="key in pbxKeys" :key="key" :to="`${prefix}/${key}`">
        <div class="resource-bar-label"><span>{{ resources[key]!.title }}</span><strong>{{ loading ? '…' : totals[key] ?? '—' }}</strong></div>
        <div class="bar-track"><div class="bar-fill" :style="{ width: `${(totals[key] || 0) / maxTotal * 100}%` }" /></div>
      </RouterLink>
    </div>
  </section>
</template>
