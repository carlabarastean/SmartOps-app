<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import type { Device, DevicePayload, DeviceType, Room } from '../types'

const props = defineProps<{ device: Device | null; rooms: Room[] }>()
const emit = defineEmits(['cancel', 'save'])

const form = ref<DevicePayload>({
  name: '',
  type: 'LIGHT',
  status: true,
  value: null,
  roomId: 0,
})

const loading = ref(false)

const brightness = ref(100)
const colorTemp = ref(4000)
const targetTemp = ref(22)
const schedule = ref({
  morning: 20,
  day: 22,
  evening: 21,
  night: 18
})

watch(
  () => props.device,
  (device) => {


    if (device) {
      form.value = {
        name: device.name,
        type: device.type,
        status: device.status,
        value: device.value,
        roomId: device.room.id,
      }
      if (device.type === 'LIGHT' && device.value !== null) {
        brightness.value = device.value
      }
      if (device.type === 'THERMOSTAT' && device.value !== null) {
        targetTemp.value = device.value
      }
    } else {

      const selectedRoom = props.rooms.length > 0 ? props.rooms[0] : null
      form.value = {
        name: '',
        type: 'LIGHT',
        status: true,
        value: 100,
        roomId: selectedRoom?.id ?? (props.rooms.length > 0 ? props.rooms[0].id : 0),
      }
      brightness.value = 100
      colorTemp.value = 4000
      targetTemp.value = 22
      schedule.value = {
        morning: 20,
        day: 22,
        evening: 21,
        night: 18
      }


      setTimeout(() => {
        if (!form.value.name.trim()) {
          generateAutoName()

        }
      }, 100)
    }
  },
  { immediate: true }
)

const typeOptions: { title: string; value: DeviceType; icon: string }[] = [
  { title: 'Light', value: 'LIGHT', icon: 'mdi-lightbulb' },
  { title: 'Thermostat', value: 'THERMOSTAT', icon: 'mdi-thermometer' },
  { title: 'Lock', value: 'LOCK', icon: 'mdi-lock' },
]

const roomOptions = computed(() => props.rooms.map((room) => ({ title: room.name, value: room.id })))

const selectedRoom = computed(() => props.rooms.find(r => r.id === form.value.roomId))

const generateAutoName = () => {
  if (!selectedRoom.value) {
    return
  }

  const typeNames: Record<DeviceType, string> = {
    LIGHT: 'Light',
    THERMOSTAT: 'Thermostat',
    LOCK: 'Lock'
  }

  const generatedName = `${selectedRoom.value.name} ${typeNames[form.value.type]}`
  form.value.name = generatedName

}

watch(() => [form.value.type, form.value.roomId], () => {
  if (!props.device) {
    generateAutoName()
  }
}, { deep: true })

watch(() => form.value.type, (newType) => {
  if (newType === 'LIGHT') {
    form.value.value = brightness.value
  } else if (newType === 'THERMOSTAT') {
    form.value.value = targetTemp.value
  } else {
    form.value.value = null
  }
})

watch(brightness, (val) => {
  if (form.value.type === 'LIGHT') {
    form.value.value = val
  }
})

watch(targetTemp, (val) => {
  if (form.value.type === 'THERMOSTAT') {
    form.value.value = val
  }
})

const formRef = ref()
const formValid = ref(false)

const submit = async () => {
  const { valid } = formRef.value ? await formRef.value.validate() : { valid: false }
  if (!valid) {
    return
  }

  if (!form.value.name.trim()) {
    generateAutoName()
  }

  if (!form.value.name.trim()) {
    alert('Please enter a device name')
    return
  }

  if (!form.value.roomId) {
    alert('Please select a room')
    return
  }

  loading.value = true
  try {
    emit('save', form.value)
  } catch (error) {
    alert('Failed to save device. Please try again.')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <v-card class="device-form" rounded="lg">
    <v-card-title class="d-flex align-center pa-4">
      <v-icon :icon="typeOptions.find(t => t.value === form.type)?.icon" class="mr-3" color="primary" />
      {{ props.device ? 'Edit Device' : 'Add New Device' }}
    </v-card-title>
    <v-divider />
    <v-card-text class="pa-4">
      <v-alert
        v-if="roomOptions.length === 0"
        type="warning"
        variant="tonal"
        class="mb-4"
      >
        <v-icon start>mdi-alert</v-icon>
        No rooms available. Please create a room first before adding devices.
      </v-alert>

      <v-form ref="formRef" v-model="formValid" @submit.prevent="submit">
        <v-select
          v-model="form.roomId"
          :items="roomOptions"
          label="Room Location"
          variant="outlined"
          prepend-inner-icon="mdi-home"
          class="mb-3"
          required
          :rules="[v => !!v || 'Please select a room']"
          :disabled="roomOptions.length === 0"
          :hint="roomOptions.length === 0 ? 'No rooms available. Create a room first.' : ''"
          persistent-hint
        />

        <v-select
          v-model="form.type"
          :items="typeOptions"
          label="Device Type"
          variant="outlined"
          prepend-inner-icon="mdi-devices"
          class="mb-3"
          required
        >
          <template #item="{ item, props }">
            <v-list-item v-bind="props">
              <template #prepend>
                <v-icon :icon="item.raw.icon" />
              </template>
            </v-list-item>
          </template>
        </v-select>

        <v-text-field
          v-model="form.name"
          label="Device Name"
          variant="outlined"
          prepend-inner-icon="mdi-label"
          class="mb-3"
          required
          :rules="[v => !!v || 'Device name is required']"
          hint="Enter a custom name or use auto-generated name"
          persistent-hint
        >
          <template #append-inner>
            <v-btn
              icon="mdi-auto-fix"
              size="small"
              variant="text"
              @click="generateAutoName"
              title="Auto-generate name"
            />
          </template>
        </v-text-field>

        <v-expand-transition>
          <div v-if="form.type === 'LIGHT'" class="device-config mb-4">
            <v-card variant="tonal" color="primary" class="pa-3">
              <p class="text-subtitle-2 mb-3">
                <v-icon size="small" class="mr-1">mdi-tune</v-icon>
                Light Configuration
              </p>

              <div class="mb-4">
                <label class="text-caption text-medium-emphasis">Brightness</label>
                <v-slider
                  v-model="brightness"
                  :min="0"
                  :max="100"
                  :step="1"
                  thumb-label
                  color="yellow"
                  class="mt-2"
                >
                  <template #append>
                    <v-chip size="small" color="yellow">{{ brightness }}%</v-chip>
                  </template>
                </v-slider>
              </div>

              <div>
                <label class="text-caption text-medium-emphasis">Color Temperature</label>
                <v-slider
                  v-model="colorTemp"
                  :min="2700"
                  :max="6500"
                  :step="100"
                  thumb-label
                  color="orange"
                  class="mt-2"
                >
                  <template #append>
                    <v-chip size="small" color="orange">{{ colorTemp }}K</v-chip>
                  </template>
                </v-slider>
                <div class="d-flex justify-space-between text-caption mt-1">
                  <span>Warm</span>
                  <span>Cool</span>
                </div>
              </div>
            </v-card>
          </div>
        </v-expand-transition>

        <v-expand-transition>
          <div v-if="form.type === 'THERMOSTAT'" class="device-config mb-4">
            <v-card variant="tonal" color="orange" class="pa-3">
              <p class="text-subtitle-2 mb-3">
                <v-icon size="small" class="mr-1">mdi-tune</v-icon>
                Temperature Control
              </p>

              <div class="mb-4">
                <label class="text-caption text-medium-emphasis">Target Temperature</label>
                <v-slider
                  v-model="targetTemp"
                  :min="16"
                  :max="30"
                  :step="0.5"
                  thumb-label
                  color="orange"
                  class="mt-2"
                >
                  <template #append>
                    <v-chip size="small" color="orange">{{ targetTemp }}°C</v-chip>
                  </template>
                </v-slider>
              </div>

              <v-divider class="my-3" />

              <p class="text-caption text-medium-emphasis mb-2">
                <v-icon size="small" class="mr-1">mdi-clock-outline</v-icon>
                Daily Schedule
              </p>

              <div class="schedule-grid">
                <div class="schedule-item">
                  <v-icon size="small" color="orange">mdi-weather-sunset-up</v-icon>
                  <span class="text-caption">Morning (6-12)</span>
                  <v-text-field
                    v-model.number="schedule.morning"
                    type="number"
                    variant="outlined"
                    density="compact"
                    suffix="°C"
                    hide-details
                    :min="16"
                    :max="30"
                  />
                </div>

                <div class="schedule-item">
                  <v-icon size="small" color="yellow">mdi-white-balance-sunny</v-icon>
                  <span class="text-caption">Day (12-18)</span>
                  <v-text-field
                    v-model.number="schedule.day"
                    type="number"
                    variant="outlined"
                    density="compact"
                    suffix="°C"
                    hide-details
                    :min="16"
                    :max="30"
                  />
                </div>

                <div class="schedule-item">
                  <v-icon size="small" color="orange-darken-2">mdi-weather-sunset-down</v-icon>
                  <span class="text-caption">Evening (18-22)</span>
                  <v-text-field
                    v-model.number="schedule.evening"
                    type="number"
                    variant="outlined"
                    density="compact"
                    suffix="°C"
                    hide-details
                    :min="16"
                    :max="30"
                  />
                </div>

                <div class="schedule-item">
                  <v-icon size="small" color="blue">mdi-weather-night</v-icon>
                  <span class="text-caption">Night (22-6)</span>
                  <v-text-field
                    v-model.number="schedule.night"
                    type="number"
                    variant="outlined"
                    density="compact"
                    suffix="°C"
                    hide-details
                    :min="16"
                    :max="30"
                  />
                </div>
              </div>
            </v-card>
          </div>
        </v-expand-transition>

        <v-expand-transition>
          <div v-if="form.type === 'LOCK'" class="device-config mb-4">
            <v-card variant="tonal" color="blue-grey" class="pa-3">
              <p class="text-subtitle-2 mb-2">
                <v-icon size="small" class="mr-1">mdi-shield-lock</v-icon>
                Security Lock
              </p>
              <p class="text-caption text-medium-emphasis">
                Lock devices are binary (locked/unlocked). No additional configuration needed.
              </p>
            </v-card>
          </div>
        </v-expand-transition>

        <v-switch
          v-model="form.status"
          :label="form.status ? 'Device Enabled' : 'Device Disabled'"
          color="success"
          hide-details
          inset
        />
      </v-form>
    </v-card-text>
    <v-divider />
    <v-card-actions class="pa-4">
      <v-spacer />
      <v-btn variant="text" @click="emit('cancel')">Cancel</v-btn>
      <v-btn
        color="primary"
        :loading="loading"
        :disabled="!formValid || roomOptions.length === 0"
        @click="submit"
      >
        <v-icon start>{{ props.device ? 'mdi-content-save' : 'mdi-plus' }}</v-icon>
        {{ props.device ? 'Save Changes' : 'Add Device' }}
      </v-btn>
    </v-card-actions>
  </v-card>
</template>

<style scoped lang="scss">
.device-form {
  border: 1px solid rgba(var(--v-theme-primary), 0.2);
}

.device-config {
  animation: slideIn 0.3s ease-out;
}

@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateY(-10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.schedule-grid {
  display: grid;
  gap: 12px;
}

.schedule-item {
  display: grid;
  grid-template-columns: 24px 1fr 100px;
  align-items: center;
  gap: 8px;
}
</style>

