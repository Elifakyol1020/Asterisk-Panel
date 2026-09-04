<script setup lang="ts">
import AppIcon from './AppIcon.vue'
</script>

<template>
  <section class="panel integration-card">
    <span class="integration-icon"><AppIcon name="info" :size="20" /></span>
    <h3>Yapılandırma kayıtları</h3>
    <p>Santral ayarları veritabanında saklanır.</p>
    <div class="integration-status">Kayıtlı yapılandırmalar</div>
  </section>
</template>
