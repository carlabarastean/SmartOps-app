import { apiClient } from './apiClient'
import type { Device, DevicePayload, Room, RoomPayload, User } from '../types'

const mapPayload = (payload: DevicePayload) => ({
  name: payload.name,
  type: payload.type,
  status: payload.status,
  value: payload.value,
  room: { id: payload.roomId },
})

export const getAllDevices = () => apiClient.get<Device[]>('/devices')
export const getRooms = () => apiClient.get<Room[]>('/rooms')
export const createRoom = (payload: RoomPayload) =>
  apiClient.post<Room>('/rooms', {
    name: payload.name,
    floor: payload.floor,
    user: { id: payload.ownerId },
  })

export const getAllUsers = () => apiClient.get<User[]>('/users')
export const createUser = (user: Pick<User, 'username' | 'email'> & { password?: string }) =>
  apiClient.post<User>('/users', user)

export const loginUser = (credentials: { username: string; password: string }) =>
  apiClient.post<User>('/users/login', credentials)

export const saveDevice = (payload: DevicePayload) =>
  apiClient.post<Device>('/devices', mapPayload(payload))

export const updateDevice = (id: number, payload: DevicePayload) =>
  apiClient.put<Device>(`/devices/${id}`, mapPayload(payload))

export const deleteDevice = (id: number) => apiClient.delete(`/devices/${id}`)

export const updateDeviceStatus = (device: Device, status: boolean) =>
  apiClient.put<Device>(`/devices/${device.id}`, {
    name: device.name,
    type: device.type,
    status,
    value: device.value,
    room: { id: device.room.id },
  })

export const getAiSuggestion = (payload: string | { prompt: string; userId?: number }) =>
  apiClient.post<{ suggestion?: string; response?: string; status?: string; model?: string }>(
    '/ai/generate',
    typeof payload === 'string' ? { prompt: payload } : payload
  )

export const getAiSuggestionsForUser = (userId: number) =>
  apiClient.get<{
    success: boolean
    userId: number
    preferredTemperature: number | null
    totalDevices: number
    thermostats: number
    suggestion: string
  }>(`/ai/suggestions/${userId}`)

export const getTemperatureTrend = () =>
  apiClient.get<{
    id: number
    deviceId: number
    temperature: number
    timestamp: number[]
    roomName: string
  }[]>('/temperature/trend')

