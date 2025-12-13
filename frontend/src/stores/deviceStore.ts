import { ref, computed } from 'vue'
import type { Device, DevicePayload, Room } from '../types'
import { deviceApi, roomApi } from '../services/apiClient'

export default {
  setup() {
    const devices = ref<Device[]>([])
    const rooms = ref<Room[]>([])
    const loading = ref(false)
    const error = ref<string | null>(null)
    const roomFilter = ref<number | null>(null)
    const statusFilter = ref<boolean | null>(null)

    const filteredDevices = computed(() => {
      return devices.value.filter((device) => {
        const matchesRoom = roomFilter.value ? device.room.id === roomFilter.value : true
        const matchesStatus = statusFilter.value !== null ? device.status === statusFilter.value : true
        return matchesRoom && matchesStatus
      })
    })

    const fetchDevices = async () => {
      loading.value = true
      error.value = null
      try {
        const [deviceRes, roomRes] = await Promise.all([deviceApi.list(), roomApi.list()])
        devices.value = deviceRes.data
        rooms.value = roomRes.data
      } catch (err) {
        error.value = 'Failed to load devices'
      } finally {
        loading.value = false
      }
    }

    const createDevice = async (payload: DevicePayload) => {
      const response = await deviceApi.create(payload)
      await fetchDevices()
      return response.data
    }

    const updateDevice = async (id: number, payload: Partial<DevicePayload>) => {
      const response = await deviceApi.update(id, payload)
      await fetchDevices()
      return response.data
    }

    const deleteDevice = async (id: number) => {
      await deviceApi.delete(id)
      await fetchDevices()
    }

    return {
      devices,
      rooms,
      filteredDevices,
      loading,
      error,
      roomFilter,
      statusFilter,
      fetchDevices,
      createDevice,
      updateDevice,
      deleteDevice,
    }
  },
}
