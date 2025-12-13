export type DeviceType = 'LIGHT' | 'THERMOSTAT' | 'LOCK'

export interface Room {
  id: number
  name: string
  floor: number
}

export interface Device {
  id: number
  name: string
  type: DeviceType
  status: boolean
  value: number | null
  room: Room
}

export interface DevicePayload {
  name: string
  type: DeviceType
  status: boolean
  value: number | null
  roomId: number
}

export interface RoomPayload {
  name: string
  floor: number
  ownerId: number
}

export interface User {
  id: number
  username: string
  email: string
}

export interface AiSuggestion {
  suggestion: string
}
