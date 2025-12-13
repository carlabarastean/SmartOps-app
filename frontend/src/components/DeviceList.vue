<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import DeviceForm from './DeviceForm.vue'
import type { Device, DevicePayload, Room } from '../types'
import { getAllDevices, getRooms, saveDevice, updateDevice, deleteDevice as apiDeleteDevice, updateDeviceStatus } from '../services/api'


const devices = ref<Device[]>([])
const rooms = ref<Room[]>([])
const loading = ref(false)
const error = ref<string | null>(null)
const roomFilter = ref<number | null>(null)
const statusFilter = ref<string>('all')

const dialog = ref(false)
const deleteDialog = ref(false)
const selectedDevice = ref<Device | null>(null)
const deviceToDelete = ref<Device | null>(null)
const notificationMessage = ref('')
const isSnackbarOpen = ref(false)
const deleting = ref(false)
const viewMode = ref<'grid' | 'table'>('grid')

const headers = [
  { title: 'Device', key: 'name', align: 'start' as const },
  { title: 'Type', key: 'type' },
  { title: 'Room', key: 'room.name' },
  { title: 'Value', key: 'value' },
  { title: 'Status', key: 'status' },
  { title: 'Control', key: 'control', sortable: false },
  { title: 'Actions', key: 'actions', sortable: false },
]

const getDeviceIcon = (type: string) => {
  const icons: Record<string, { icon: string; color: string }> = {
    LIGHT: { icon: 'mdi-lightbulb', color: 'yellow' },
    THERMOSTAT: { icon: 'mdi-thermometer', color: 'orange' },
    LOCK: { icon: 'mdi-lock', color: 'blue-grey' },
    CAMERA: { icon: 'mdi-cctv', color: 'blue' },
    SENSOR: { icon: 'mdi-motion-sensor', color: 'green' },
  }
  return icons[type] || { icon: 'mdi-devices', color: 'grey' }
}

const filteredDevices = computed(() => {
  return devices.value.filter((device) => {
    const matchesRoom = roomFilter.value ? device.room.id === roomFilter.value : true
    const matchesStatus =
      statusFilter.value === 'all'
        ? true
        : statusFilter.value === 'active'
        ? device.status === true
        : device.status === false
    return matchesRoom && matchesStatus
  })
})

const fetchData = async () => {
  loading.value = true
  error.value = null
  try {
    const [deviceRes, roomRes] = await Promise.all([getAllDevices(), getRooms()])
    devices.value = deviceRes.data
    rooms.value = roomRes.data
  } catch (err) {
    error.value = 'System offline. Please retry later.'
  } finally {
    loading.value = false
  }
}

onMounted(fetchData)

const roomOptions = computed(() => [
  { title: 'All Rooms', value: null },
  ...rooms.value.map((room) => ({ title: room.name, value: room.id })),
])

const handleToggle = async (device: Device) => {
  try {
    await updateDeviceStatus(device, !device.status)
    device.status = !device.status
    notificationMessage.value = `${device.name} ${device.status ? 'activated' : 'deactivated'}`
    isSnackbarOpen.value = true
  } catch (err) {
    notificationMessage.value = 'Unable to update device status.'
    isSnackbarOpen.value = true
  }
}

const openDeleteDialog = (device: Device) => {
  if (device.status) {
    notificationMessage.value = 'Deactivate device before deleting.'
    isSnackbarOpen.value = true
    return
  }
  deviceToDelete.value = device
  deleteDialog.value = true
}

const handleDelete = async () => {
  if (!deviceToDelete.value) return

  deleting.value = true
  try {
    await apiDeleteDevice(deviceToDelete.value.id)
    devices.value = devices.value.filter((d) => d.id !== deviceToDelete.value!.id)
    notificationMessage.value = 'Device deleted successfully!'
    isSnackbarOpen.value = true
    deleteDialog.value = false
  } catch (err) {
    notificationMessage.value = 'Delete failed. Check backend status.'
    isSnackbarOpen.value = true
  } finally {
    deleting.value = false
  }
}

const openCreate = () => {
  console.log('🔥 ADD DEVICE button clicked!')
  console.log('Available rooms:', rooms.value.length)
  selectedDevice.value = null
  dialog.value = true
  console.log('Dialog opened:', dialog.value)
}

const openEdit = (device: Device) => {
  selectedDevice.value = device
  dialog.value = true
}

const closeDialog = () => {
  dialog.value = false
  selectedDevice.value = null
}

const handleSave = async (payload: DevicePayload) => {
  console.log('🔥 DeviceList handleSave called!')
  console.log('Payload:', payload)
  console.log('Is editing:', !!selectedDevice.value)

  try {
    if (selectedDevice.value) {
      console.log('Updating existing device...')
      await updateDevice(selectedDevice.value.id, payload)
      notificationMessage.value = 'Device updated successfully!'
    } else {
      console.log('Creating new device...')
      await saveDevice(payload)
      notificationMessage.value = 'Device created successfully!'
    }
    console.log('✅ API call successful, refreshing data...')
    await fetchData()
    closeDialog()
    isSnackbarOpen.value = true
  } catch (err) {
    console.error('❌ Save failed:', err)
    notificationMessage.value = 'Save failed. Please verify backend.'
    isSnackbarOpen.value = true
  }
}

const activeDevicesCount = computed(() => devices.value.filter((d) => d.status).length)
const inactiveDevicesCount = computed(() => devices.value.filter((d) => !d.status).length)
</script>

<template>
  <div class="devices-view">
    <div class="devices-view__header">
      <div>
        <h1 class="text-h4 mb-2">Devices</h1>
        <p class="text-subtitle-1 text-medium-emphasis">Manage and control your smart devices.</p>
      </div>
      <div class="devices-view__actions">
        <v-btn-toggle v-model="viewMode" mandatory variant="outlined" divided density="compact">
          <v-btn value="grid" icon="mdi-view-grid" />
          <v-btn value="table" icon="mdi-view-list" />
        </v-btn-toggle>
        <v-btn color="primary" @click="openCreate">
          <v-icon start>mdi-plus</v-icon>
          ADD DEVICE
        </v-btn>
      </div>
    </div>

    <div class="devices-view__stats">
      <v-card variant="outlined" class="stat-card">
        <v-card-text class="d-flex align-center">
          <v-icon color="success" size="32" class="mr-3">mdi-power</v-icon>
          <div>
            <p class="text-caption text-medium-emphasis">Active</p>
            <p class="text-h5">{{ activeDevicesCount }}</p>
          </div>
        </v-card-text>
      </v-card>
      <v-card variant="outlined" class="stat-card">
        <v-card-text class="d-flex align-center">
          <v-icon color="grey" size="32" class="mr-3">mdi-power-off</v-icon>
          <div>
            <p class="text-caption text-medium-emphasis">Inactive</p>
            <p class="text-h5">{{ inactiveDevicesCount }}</p>
          </div>
        </v-card-text>
      </v-card>
      <v-card variant="outlined" class="stat-card">
        <v-card-text class="d-flex align-center">
          <v-icon color="primary" size="32" class="mr-3">mdi-devices</v-icon>
          <div>
            <p class="text-caption text-medium-emphasis">Total</p>
            <p class="text-h5">{{ devices.length }}</p>
          </div>
        </v-card-text>
      </v-card>
    </div>

    <div class="devices-view__filters">
      <v-select
        v-model="roomFilter"
        :items="roomOptions"
        label="Filter by Room"
        variant="outlined"
        density="compact"
        hide-details
        clearable
      />
      <v-select
        v-model="statusFilter"
        :items="[
          { title: 'All Devices', value: 'all' },
          { title: 'Active Only', value: 'active' },
          { title: 'Inactive Only', value: 'inactive' },
        ]"
        label="Filter by Status"
        variant="outlined"
        density="compact"
        hide-details
      />
      <v-btn variant="text" @click="fetchData" :loading="loading">
        <v-icon start>mdi-refresh</v-icon>
        Refresh
      </v-btn>
    </div>

    <v-alert v-if="error" type="error" variant="tonal" class="mb-4">
      <v-icon start>mdi-alert-circle</v-icon>
      {{ error }}
    </v-alert>

    <v-progress-linear v-if="loading" indeterminate color="primary" class="mb-4" />

    <v-alert v-if="!loading && filteredDevices.length === 0" type="info" variant="tonal">
      <v-icon start>mdi-information</v-icon>
      No devices found. Add your first device!
    </v-alert>

    <div v-if="!loading && filteredDevices.length > 0">
      <v-row v-if="viewMode === 'grid'">
        <v-col v-for="device in filteredDevices" :key="device.id" cols="12" sm="6" md="4" lg="3">
          <v-card class="device-card" variant="outlined" :class="{ 'device-card--active': device.status }">
            <v-card-text>
              <div class="device-card__header">
                <v-avatar :color="getDeviceIcon(device.type).color" size="48">
                  <v-icon color="white">{{ getDeviceIcon(device.type).icon }}</v-icon>
                </v-avatar>
                <v-chip :color="device.status ? 'success' : 'grey'" size="x-small" class="device-card__status">
                  {{ device.status ? 'ON' : 'OFF' }}
                </v-chip>
              </div>
              <h3 class="device-card__name mt-3">{{ device.name }}</h3>
              <p class="device-card__type">{{ device.type }}</p>
              <v-divider class="my-2" />
              <div class="device-card__info">
                <div class="info-item">
                  <v-icon size="small">mdi-home</v-icon>
                  <span>{{ device.room.name }}</span>
                </div>
                <div v-if="device.value !== null" class="info-item">
                  <v-icon size="small">mdi-gauge</v-icon>
                  <span>{{ device.value }}{{ device.type === 'THERMOSTAT' ? '°C' : '' }}</span>
                </div>
              </div>
            </v-card-text>
            <v-card-actions>
              <v-switch
                :model-value="device.status"
                @update:model-value="handleToggle(device)"
                color="success"
                hide-details
                density="compact"
                inset
              />
              <v-spacer />
              <v-btn icon="mdi-pencil" size="small" variant="text" color="primary" @click="openEdit(device)" />
              <v-btn icon="mdi-delete" size="small" variant="text" color="error" @click="openDeleteDialog(device)" />
            </v-card-actions>
          </v-card>
        </v-col>
      </v-row>

      <v-data-table
        v-else
        :headers="headers"
        :items="filteredDevices"
        class="devices-table"
        :items-per-page="10"
      >
        <template #item.name="{ item }">
          <div class="d-flex align-center">
            <v-avatar :color="getDeviceIcon(item.type).color" size="32" class="mr-3">
              <v-icon color="white" size="18">{{ getDeviceIcon(item.type).icon }}</v-icon>
            </v-avatar>
            <strong>{{ item.name }}</strong>
          </div>
        </template>
        <template #item.status="{ item }">
          <v-chip :color="item.status ? 'success' : 'grey'" size="small">
            {{ item.status ? 'ON' : 'OFF' }}
          </v-chip>
        </template>
        <template #item.value="{ item }">
          <span v-if="item.value !== null">
            {{ item.value }}{{ item.type === 'THERMOSTAT' ? '°C' : '' }}
          </span>
          <span v-else class="text-medium-emphasis">—</span>
        </template>
        <template #item.control="{ item }">
          <v-switch
            :model-value="item.status"
            @update:model-value="handleToggle(item)"
            color="success"
            hide-details
            density="compact"
            inset
          />
        </template>
        <template #item.actions="{ item }">
          <v-btn icon="mdi-pencil" size="small" variant="text" color="primary" @click="openEdit(item)" />
          <v-btn icon="mdi-delete" size="small" variant="text" color="error" @click="openDeleteDialog(item)" />
        </template>
      </v-data-table>
    </div>

    <v-dialog v-model="dialog" max-width="600" persistent>
      <DeviceForm
        :device="selectedDevice"
        :rooms="rooms"
        @save="handleSave"
        @cancel="closeDialog"
      />
    </v-dialog>

    <v-dialog v-model="deleteDialog" max-width="400">
      <v-card rounded="lg">
        <v-card-title class="d-flex align-center text-error">
          <v-icon start color="error">mdi-alert-circle</v-icon>
          Delete Device?
        </v-card-title>
        <v-divider />
        <v-card-text class="pt-4">
          <p>Are you sure you want to delete <strong>{{ deviceToDelete?.name }}</strong>?</p>
          <p class="text-caption text-medium-emphasis mt-2">This action cannot be undone.</p>
        </v-card-text>
        <v-divider />
        <v-card-actions>
          <v-spacer />
          <v-btn variant="text" @click="deleteDialog = false" :disabled="deleting">Cancel</v-btn>
          <v-btn color="error" :loading="deleting" @click="handleDelete">
            <v-icon start>mdi-delete</v-icon>
            Delete
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <v-snackbar v-model="isSnackbarOpen" :timeout="3000" color="primary">
      {{ notificationMessage }}
      <template #actions>
        <v-btn variant="text" @click="isSnackbarOpen = false">Close</v-btn>
      </template>
    </v-snackbar>
  </div>
</template>

<style scoped lang="scss">
.devices-view {
  padding: 24px;
}

.devices-view__header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 24px;
  flex-wrap: wrap;
  gap: 16px;
}

.devices-view__actions {
  display: flex;
  gap: 12px;
  align-items: center;
}

.devices-view__stats {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 16px;
  margin-bottom: 24px;
}

.stat-card {
  transition: all 0.3s ease;

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(var(--v-theme-primary), 0.15);
  }
}

.devices-view__filters {
  display: flex;
  gap: 16px;
  margin-bottom: 24px;
  flex-wrap: wrap;
}

.device-card {
  transition: all 0.3s ease;
  border: 1px solid rgba(var(--v-theme-primary), 0.1);

  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 8px 24px rgba(var(--v-theme-primary), 0.15);
    border-color: rgba(var(--v-theme-primary), 0.3);
  }

  &--active {
    border-color: rgba(var(--v-theme-success), 0.3);
    background: linear-gradient(135deg, rgba(var(--v-theme-success), 0.05), transparent);
  }
}

.device-card__header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.device-card__status {
  font-weight: 600;
}

.device-card__name {
  font-size: 1.125rem;
  font-weight: 600;
  margin: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.device-card__type {
  font-size: 0.875rem;
  opacity: 0.7;
  margin: 4px 0 0;
}

.device-card__info {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 0.875rem;
}

.devices-table {
  border: 1px solid rgba(var(--v-theme-primary), 0.1);
  border-radius: 8px;
}
</style>

