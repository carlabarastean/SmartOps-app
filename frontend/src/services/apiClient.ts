import axios from 'axios'
import type { Device, DevicePayload, Room } from '../types'

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8083/api'

export const apiClient = axios.create({
  baseURL: API_BASE_URL,
})

export const deviceApi = {
  list: () => apiClient.get<Device[]>('/devices'),
  create: (payload: DevicePayload) => apiClient.post<Device>('/devices', payload),
  update: (id: number, payload: Partial<DevicePayload>) => apiClient.put<Device>(`/devices/${id}`, payload),
  delete: (id: number) => apiClient.delete(`/devices/${id}`),
  byRoom: (roomId: number) => apiClient.get<Device[]>(`/devices/room/${roomId}`),
  byStatus: (status: boolean) => apiClient.get<Device[]>(`/devices/active`, { params: { status } }),
}

export const roomApi = {
  list: () => apiClient.get<Room[]>('/rooms'),
}

export const aiApi = {
  suggest: (prompt: string) => apiClient.post<{ suggestion: string }>('/chatbot/analyze', { prompt }),
}
