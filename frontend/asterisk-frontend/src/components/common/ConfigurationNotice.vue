<script setup lang="ts">
import AppIcon from './AppIcon.vue'
</script>

<template>
  <section class="panel integration-card">
    <span class="integration-icon"><AppIcon name="info" :size="20" /></span>
    <h3>Yapılandırma kayıtları</h3>
    <p>Santral ayarlarınız uygulama veritabanında saklanır. Bir kaydın oluşması, canlı santral üzerinde etkinleştirildiğini veya bir cihazın bağlandığını göstermez.</p>
    <div class="integration-status">Bu panel kayıtlı yapılandırmaları gösterir.</div>
  </section>
</template>
