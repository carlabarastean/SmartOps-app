<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import type { Device, User } from '../types'
import { getAllDevices, getTemperatureTrend } from '../services/api'

const props = defineProps<{ currentUser: User | null }>()
const devices = ref<Device[]>([])
const loading = ref(false)
const telemetry = ref<number[]>([])
const temperatureData = ref<any[]>([])
const error = ref<string | null>(null)

const fetchDevices = async () => {
  loading.value = true
  error.value = null
  try {
    const response = await getAllDevices()
    devices.value = response.data


    const temperatureResponse = await getTemperatureTrend()
    temperatureData.value = temperatureResponse.data


    telemetry.value = temperatureResponse.data
      .slice(-24)
      .map(reading => reading.temperature)
      .reverse()

  } catch (err) {
    console.error('Error fetching data:', err)
    error.value = 'Unable to load stats from backend.'
  } finally {
    loading.value = false
  }
}

onMounted(fetchDevices)

const totalDevices = computed(() => devices.value.length)
const activeDevices = computed(() => devices.value.filter((d) => d.status).length)
const inactiveDevices = computed(() => devices.value.filter((d) => !d.status).length)
const averageValue = computed(() => {
  const thermostats = devices.value.filter((d) => d.type === 'THERMOSTAT' && typeof d.value === 'number')
  if (!thermostats.length) return '0.0'
  const sum = thermostats.reduce((acc, d) => acc + (d.value ?? 0), 0)
  return (sum / thermostats.length).toFixed(1)
})
const systemHealth = computed(() => (devices.value.length ? Math.min(100, (activeDevices.value / devices.value.length) * 100).toFixed(0) : '100'))

const kpiCards = computed(() => [
  { label: 'Active Devices', value: activeDevices.value, icon: 'mdi-power', color: 'success' },
  { label: 'Inactive', value: inactiveDevices.value, icon: 'mdi-power-off', color: 'grey' },
  { label: 'Avg Temp', value: `${averageValue.value}°C`, icon: 'mdi-thermometer', color: 'orange' },
  { label: 'System Health', value: `${systemHealth.value}%`, icon: 'mdi-shield-check', color: 'green' },
])

const rooms = computed(() => {
  const grouped = new Map<number, { name: string; floor: number; devices: number; active: number }>()
  devices.value.forEach((device) => {
    const room = device.room
    const existing = grouped.get(room.id) ?? { name: room.name, floor: room.floor, devices: 0, active: 0 }
    existing.devices += 1
    if (device.status) existing.active += 1
    grouped.set(room.id, existing)
  })
  return Array.from(grouped.values())
})

const devicesByType = computed(() => {
  const types: Record<string, number> = {}
  devices.value.forEach(d => {
    types[d.type] = (types[d.type] || 0) + 1
  })
  return Object.entries(types).map(([type, count]) => ({ type, count }))
})
</script>

<template>
  <div class="dashboard">
    <div class="dashboard__header">
      <div>
        <h1 class="text-h4 mb-2">Dashboard</h1>
        <p class="text-subtitle-1 text-medium-emphasis">System overview and statistics</p>
      </div>
      <v-btn variant="text" @click="fetchDevices" :loading="loading">
        <v-icon start>mdi-refresh</v-icon>
        Refresh
      </v-btn>
    </div>

    <v-alert v-if="error" type="warning" variant="tonal" class="mb-4">
      {{ error }}
    </v-alert>

    <v-progress-linear v-if="loading" indeterminate color="primary" class="mb-4" />

    <section class="dashboard__kpi mb-4">
      <v-row>
        <v-col v-for="card in kpiCards" :key="card.label" cols="12" sm="6" md="3">
          <v-card variant="outlined" class="kpi-card">
            <v-card-text class="d-flex align-center">
              <v-icon :color="card.color" size="40" class="mr-3">{{ card.icon }}</v-icon>
              <div>
                <p class="text-caption text-medium-emphasis">{{ card.label }}</p>
                <p class="text-h5">{{ card.value }}</p>
              </div>
            </v-card-text>
          </v-card>
        </v-col>
      </v-row>
    </section>

    <v-row>
      <v-col cols="12" md="6">
        <v-card variant="outlined" class="room-status-card">
          <v-card-title class="d-flex align-center">
            <v-icon class="mr-2">mdi-home-group</v-icon>
            Room Status
            <v-spacer />
            <v-chip color="primary" variant="tonal" size="small">{{ rooms.length }} rooms</v-chip>
          </v-card-title>
          <v-divider />
          <v-card-text v-if="rooms.length === 0" class="text-center py-8">
            <v-icon size="48" color="grey">mdi-home-off</v-icon>
            <p class="text-subtitle-1 mt-2">No rooms configured</p>
          </v-card-text>
          <v-list v-else density="compact">
            <v-list-item
              v-for="room in rooms"
              :key="room.name"
              class="room-item"
            >
              <template #prepend>
                <v-avatar color="primary" size="40">
                  <v-icon color="white">mdi-home</v-icon>
                </v-avatar>
              </template>
              <v-list-item-title>{{ room.name }}</v-list-item-title>
              <v-list-item-subtitle>Floor {{ room.floor }}</v-list-item-subtitle>
              <template #append>
                <div class="text-right">
                  <v-chip size="x-small" color="success" class="mr-1">{{ room.active }} on</v-chip>
                  <v-chip size="x-small" color="grey">{{ room.devices }} total</v-chip>
                </div>
              </template>
            </v-list-item>
          </v-list>
        </v-card>
      </v-col>

      <v-col cols="12" md="6">
        <v-card variant="outlined" class="device-breakdown-card">
          <v-card-title class="d-flex align-center">
            <v-icon class="mr-2">mdi-chart-pie</v-icon>
            Device Breakdown
          </v-card-title>
          <v-divider />
          <v-card-text v-if="devicesByType.length === 0" class="text-center py-8">
            <v-icon size="48" color="grey">mdi-devices</v-icon>
            <p class="text-subtitle-1 mt-2">No devices configured</p>
          </v-card-text>
          <v-card-text v-else>
            <div v-for="item in devicesByType" :key="item.type" class="device-type-item mb-3">
              <div class="d-flex align-center justify-space-between mb-2">
                <div class="d-flex align-center">
                  <v-icon
                    :color="item.type === 'LIGHT' ? 'yellow' : item.type === 'THERMOSTAT' ? 'orange' : 'blue-grey'"
                    class="mr-2"
                  >
                    {{ item.type === 'LIGHT' ? 'mdi-lightbulb' : item.type === 'THERMOSTAT' ? 'mdi-thermometer' : 'mdi-lock' }}
                  </v-icon>
                  <span>{{ item.type }}</span>
                </div>
                <v-chip size="small" variant="tonal">{{ item.count }}</v-chip>
              </div>
              <v-progress-linear
                :model-value="(item.count / totalDevices) * 100"
                :color="item.type === 'LIGHT' ? 'yellow' : item.type === 'THERMOSTAT' ? 'orange' : 'blue-grey'"
                height="6"
                rounded
              />
            </div>
          </v-card-text>
        </v-card>
      </v-col>
    </v-row>

    <v-row class="mt-4">
      <v-col cols="12">
        <v-card variant="outlined">
          <v-card-title class="d-flex align-center">
            <v-icon class="mr-2">mdi-chart-line</v-icon>
            Temperature Trend
            <v-spacer />
            <v-chip v-if="temperatureData.length" color="orange" variant="tonal" size="small">
              {{ temperatureData[0]?.temperature }}°C current
            </v-chip>
          </v-card-title>
          <v-divider />
          <v-card-text>
            <div v-if="telemetry.length === 0" class="text-center py-8">
              <v-icon size="48" color="grey">mdi-chart-line-variant</v-icon>
              <p class="text-subtitle-1 mt-2">No temperature data available</p>
              <v-btn
                color="orange"
                variant="tonal"
                @click="fetchDevices"
                class="mt-2"
              >
                <v-icon start>mdi-refresh</v-icon>
                Refresh Data
              </v-btn>
            </div>
            <div v-else>
              <v-sparkline
                :model-value="telemetry"
                :smooth="10"
                :padding="8"
                :line-width="3"
                :stroke-linecap="'round'"
                :fill="true"
                color="orange"
                height="120"
                auto-draw
              />
              <div class="d-flex justify-space-between align-center mt-3 px-2">
                <div class="text-center">
                  <p class="text-caption text-medium-emphasis">Min</p>
                  <p class="text-subtitle-2 font-weight-bold">{{ Math.min(...telemetry).toFixed(1) }}°C</p>
                </div>
                <div class="text-center">
                  <p class="text-caption text-medium-emphasis">Average</p>
                  <p class="text-subtitle-2 font-weight-bold">{{ (telemetry.reduce((a,b) => a+b, 0) / telemetry.length).toFixed(1) }}°C</p>
                </div>
                <div class="text-center">
                  <p class="text-caption text-medium-emphasis">Max</p>
                  <p class="text-subtitle-2 font-weight-bold">{{ Math.max(...telemetry).toFixed(1) }}°C</p>
                </div>
                <div class="text-center">
                  <p class="text-caption text-medium-emphasis">Readings</p>
                  <p class="text-subtitle-2 font-weight-bold">{{ temperatureData.length }}</p>
                </div>
              </div>
              <v-chip
                v-if="temperatureData.length > 0"
                size="small"
                variant="tonal"
                color="blue"
                class="mt-2"
              >
                <v-icon start size="small">mdi-clock-outline</v-icon>
                Last 12 hours • {{ temperatureData[0]?.roomName }}
              </v-chip>
            </div>
          </v-card-text>
        </v-card>
      </v-col>
    </v-row>
  </div>
</template>

<style scoped lang="scss">
.dashboard {
  padding: 24px;
}

.dashboard__header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 24px;
}

.kpi-card {
  transition: all 0.3s ease;

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(var(--v-theme-primary), 0.15);
  }
}

.room-status-card,
.device-breakdown-card {
  height: 100%;
}

.room-item {
  transition: all 0.2s ease;

  &:hover {
    background: rgba(var(--v-theme-primary), 0.05);
  }
}

.device-type-item {
  &:last-child {
    margin-bottom: 0 !important;
  }
}
</style>

