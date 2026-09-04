<script setup lang="ts">
import { computed } from 'vue'
import type { RecordData } from '@/api/platform'
import { queueMemberFields as fields } from '@/config/resources/members'
import FormField from '@/components/forms/FormField.vue'
import { recordLabel } from '@/utils/display'

const props = defineProps<{
  form: RecordData
  errors: Record<string, string>
  disabled?: boolean
  editing?: boolean
  targets: RecordData[]
  lookupLoading: boolean
  scope?: number
}>()
const options = computed(() => props.targets.map(target => ({
  value: Number(target.id),
  label: `${target.extension || '—'} · ${target.displayName || recordLabel(target)} · #${target.id}`,
})))
</script>

<template>
  <FormField
    v-model="form.endpointId"
    :field="fields.endpointId!"
    :error="errors.endpointId"
    :disabled="disabled || lookupLoading"
    :editing="editing"
    :options="options"
    :placeholder="lookupLoading ? 'Yükleniyor…' : 'Hedef seçin'"
  >
    <template #hint>
      <span v-if="!lookupLoading && !targets.length" class="form-hint">
        {{ scope ? 'Bu tenant’ta hedef yok.' : 'Tenant seçin.' }}
      </span>
    </template>
  </FormField>
  <p class="form-hint full">Yalnızca bu kuyruğun tenant’ına ait SIP endpoint’leri gösterilir. Seçilen üye realtime queue_members tablosuna tenant-aware PJSIP adıyla yazılır.</p>
  <FormField
    v-model="form.penalty"
    :field="fields.penalty!"
    :error="errors.penalty"
    :disabled="disabled"
    :editing="editing"
  />
  <FormField
    v-model="form.paused"
    :field="fields.paused!"
    :error="errors.paused"
    :disabled="disabled"
    :editing="editing"
  />
</template>
