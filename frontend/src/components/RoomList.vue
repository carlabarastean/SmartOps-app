<script setup lang="ts">
import { onMounted, ref, computed } from 'vue'
import type { Room, RoomPayload, User } from '../types'
import { getRooms, createRoom } from '../services/api'
import axios from 'axios'

const props = defineProps<{ currentUser: User | null }>()

const rooms = ref<Room[]>([])
const loading = ref(false)
const error = ref<string | null>(null)


const dialog = ref(false)
const deleteDialog = ref(false)
const editMode = ref(false)
const roomToEdit = ref<Room | null>(null)
const roomToDelete = ref<Room | null>(null)


const formData = ref<RoomPayload>({ name: '', floor: 0, ownerId: 0 })
const formErrors = ref<{ name?: string; floor?: string }>({})


const saving = ref(false)
const deleting = ref(false)
const notificationMessage = ref('')
const showNotification = ref(false)
const expandedRooms = ref<Set<number>>(new Set())


const getIconForRoom = (roomName: string) => {
  const name = roomName.toLowerCase()

  if (name.includes('bath') || name.includes('baie') || name.includes('toaleta')) {
    return { icon: 'mdi-shower', label: 'Bathroom', color: 'cyan' }
  }
  if (name.includes('bed') || name.includes('dorm') || name.includes('camera')) {
    return { icon: 'mdi-bed', label: 'Bedroom', color: 'purple' }
  }
  if (name.includes('kitchen') || name.includes('bucatarie') || name.includes('dining')) {
    return { icon: 'mdi-silverware-fork-knife', label: 'Kitchen', color: 'orange' }
  }
  if (name.includes('living') || name.includes('salon') || name.includes('sufragerie')) {
    return { icon: 'mdi-sofa', label: 'Living Room', color: 'blue' }
  }
  if (name.includes('office') || name.includes('birou') || name.includes('study')) {
    return { icon: 'mdi-laptop', label: 'Office', color: 'green' }
  }
  if (name.includes('garage') || name.includes('garaj')) {
    return { icon: 'mdi-garage', label: 'Garage', color: 'grey' }
  }
  if (name.includes('hall') || name.includes('coridor') || name.includes('entry')) {
    return { icon: 'mdi-door', label: 'Hallway', color: 'blue-grey' }
  }
  if (name.includes('garden') || name.includes('gradina') || name.includes('yard')) {
    return { icon: 'mdi-flower', label: 'Garden', color: 'green' }
  }
  if (name.includes('balcon') || name.includes('balcony') || name.includes('terrace')) {
    return { icon: 'mdi-balcony', label: 'Balcony', color: 'teal' }
  }

  const iconPool = [
    { icon: 'mdi-home', label: 'Room', color: 'blue' },
    { icon: 'mdi-bed', label: 'Bedroom', color: 'purple' },
    { icon: 'mdi-sofa', label: 'Living', color: 'indigo' },
    { icon: 'mdi-door', label: 'Space', color: 'grey' },
  ]
  return iconPool[Math.floor(Math.random() * iconPool.length)]
}

const getRoomIcon = (room: Room) => {
  return getIconForRoom(room.name)
}

const getFloorLabel = (floor: number) => {
  if (floor === 0) return 'Ground Floor'
  if (floor === 1) return '1st Floor'
  if (floor === 2) return '2nd Floor'
  if (floor === 3) return '3rd Floor'
  return `${floor}th Floor`
}

const fetchRooms = async () => {
  loading.value = true
  error.value = null
  try {
    const roomRes = await getRooms()
    rooms.value = roomRes.data
  } catch (err) {
    error.value = 'Unable to load rooms. Is the backend offline?'
  } finally {
    loading.value = false
  }
}

const canCreateRoom = computed(() => Boolean(props.currentUser))

const validateForm = (): boolean => {
  formErrors.value = {}
  let isValid = true

  if (!formData.value.name || formData.value.name.trim().length === 0) {
    formErrors.value.name = 'Room name is required'
    isValid = false
  } else if (formData.value.name.trim().length < 2) {
    formErrors.value.name = 'Room name must be at least 2 characters'
    isValid = false
  } else if (formData.value.name.trim().length > 50) {
    formErrors.value.name = 'Room name must be less than 50 characters'
    isValid = false
  }

  if (formData.value.floor < -5 || formData.value.floor > 100) {
    formErrors.value.floor = 'Floor must be between -5 and 100'
    isValid = false
  }

  return isValid
}

const openCreateDialog = () => {
  if (!props.currentUser) {
    notificationMessage.value = 'Please select a user from the profile icon first.'
    showNotification.value = true
    return
  }

  editMode.value = false
  roomToEdit.value = null
  formData.value = {
    name: '',
    floor: 0,
    ownerId: props.currentUser.id,
  }
  formErrors.value = {}
  dialog.value = true
}

const openEditDialog = (room: Room) => {
  if (!props.currentUser) {
    notificationMessage.value = 'Please select a user first.'
    showNotification.value = true
    return
  }

  editMode.value = true
  roomToEdit.value = room
  formData.value = {
    name: room.name,
    floor: room.floor,
    ownerId: props.currentUser.id,
  }
  formErrors.value = {}
  dialog.value = true
}

const openDeleteDialog = (room: Room) => {
  roomToDelete.value = room
  deleteDialog.value = true
}

const saveRoom = async () => {
  if (!validateForm()) {
    return
  }

  if (!props.currentUser) {
    notificationMessage.value = 'Please select a user first.'
    showNotification.value = true
    return
  }

  formData.value.ownerId = props.currentUser.id
  saving.value = true

  try {
    if (editMode.value && roomToEdit.value) {
      await axios.put(`http://localhost:8083/api/rooms/${roomToEdit.value.id}`, {
        name: formData.value.name,
        floor: formData.value.floor,
        user: { id: formData.value.ownerId }
      })
      notificationMessage.value = 'Room updated successfully!'
    } else {
      await createRoom(formData.value)
      notificationMessage.value = 'Room created successfully!'
    }

    dialog.value = false
    showNotification.value = true
    await fetchRooms()
  } catch (err) {
    notificationMessage.value = `Failed to ${editMode.value ? 'update' : 'create'} room. Please check the backend.`
    showNotification.value = true
  } finally {
    saving.value = false
  }
}

const deleteRoom = async () => {
  if (!roomToDelete.value) return

  deleting.value = true
  try {
    await axios.delete(`http://localhost:8083/api/rooms/${roomToDelete.value.id}`)
    notificationMessage.value = 'Room deleted successfully!'
    showNotification.value = true
    deleteDialog.value = false
    await fetchRooms()
  } catch (err: any) {
    if (err.response?.status === 409) {
      notificationMessage.value = 'Cannot delete room with active devices. Remove devices first.'
    } else {
      notificationMessage.value = 'Failed to delete room. Please try again.'
    }
    showNotification.value = true
  } finally {
    deleting.value = false
  }
}

const toggleExpand = (roomId: number) => {
  if (expandedRooms.value.has(roomId)) {
    expandedRooms.value.delete(roomId)
  } else {
    expandedRooms.value.add(roomId)
  }
}

const isExpanded = (roomId: number) => expandedRooms.value.has(roomId)

onMounted(fetchRooms)
</script>

<template>
  <div class="rooms-view">
    <div class="rooms-view__header">
      <div>
        <h1 class="text-h4 mb-2">Spaces</h1>
        <p class="text-subtitle-1 text-medium-emphasis">
          Configure rooms to organize your devices.
        </p>
      </div>
      <v-btn color="primary" class="rooms-view__add" :disabled="!canCreateRoom" @click="openCreateDialog">
        <v-icon start>mdi-plus</v-icon>
        CREATE ROOM
      </v-btn>
    </div>

    <v-alert v-if="error" type="error" variant="tonal" class="mb-4">
      {{ error }}
    </v-alert>

    <v-progress-linear v-if="loading" indeterminate color="primary" class="mb-4" />

    <v-alert v-if="!loading && rooms.length === 0" type="info" variant="tonal" class="mb-4">
      <v-icon start>mdi-information</v-icon>
      No rooms yet. Add the first one!
    </v-alert>

    <v-row v-if="!loading && rooms.length > 0">
      <v-col v-for="room in rooms" :key="room.id" cols="12" sm="6" md="4">
        <v-card class="room-card" variant="outlined" rounded="lg" elevation="2">
          <v-card-text>
            <div class="room-card__header">
              <v-avatar :color="getRoomIcon(room).color" size="56" class="room-card__avatar">
                <v-icon size="32" color="white">{{ getRoomIcon(room).icon }}</v-icon>
              </v-avatar>
              <div class="room-card__info">
                <h3 class="room-card__name">{{ room.name }}</h3>
                <p class="room-card__floor">{{ getFloorLabel(room.floor) }}</p>
              </div>
            </div>

            <v-divider class="my-3" />
            <div class="room-card__stats">
              <div class="stat-item">
                <v-icon size="small" class="mr-1">mdi-lightbulb-on-outline</v-icon>
                <span class="text-caption">{{ room.devices?.length || 0 }} devices</span>
              </div>
              <div class="stat-item">
                <v-icon size="small" class="mr-1">mdi-account</v-icon>
                <span class="text-caption">{{ room.user?.username || 'Unknown' }}</span>
              </div>
            </div>


            <v-expand-transition>
              <div v-if="isExpanded(room.id)" class="room-card__details mt-3">
                <v-divider class="mb-2" />
                <div class="text-caption">
                  <p><strong>Owner:</strong> {{ room.user?.email || 'N/A' }}</p>
                  <p><strong>Room ID:</strong> #{{ room.id }}</p>
                  <p v-if="room.devices && room.devices.length > 0">
                    <strong>Devices:</strong>
                    {{ room.devices.map((d: any) => d.name).join(', ') }}
                  </p>
                </div>
              </div>
            </v-expand-transition>
          </v-card-text>


          <v-card-actions>
            <v-btn
              size="small"
              variant="text"
              @click="toggleExpand(room.id)"
            >
              {{ isExpanded(room.id) ? 'Less' : 'Details' }}
              <v-icon end>{{ isExpanded(room.id) ? 'mdi-chevron-up' : 'mdi-chevron-down' }}</v-icon>
            </v-btn>
            <v-spacer />
            <v-btn
              icon="mdi-pencil"
              size="small"
              variant="text"
              color="primary"
              @click="openEditDialog(room)"
            />
            <v-btn
              icon="mdi-delete"
              size="small"
              variant="text"
              color="error"
              @click="openDeleteDialog(room)"
            />
          </v-card-actions>
        </v-card>
      </v-col>
    </v-row>


    <v-dialog v-model="dialog" max-width="500" persistent>
      <v-card class="room-form" rounded="lg">
        <v-card-title class="d-flex align-center">
          <v-icon start>{{ editMode ? 'mdi-pencil' : 'mdi-plus-circle' }}</v-icon>
          {{ editMode ? 'Edit Room' : 'Create New Room' }}
        </v-card-title>
        <v-divider />
        <v-card-text class="pt-4">

          <v-text-field
            v-model="formData.name"
            label="Room Name"
            placeholder="e.g., Living Room, Bedroom"
            variant="outlined"
            prepend-inner-icon="mdi-home"
            :error-messages="formErrors.name"
            class="mb-3"
            autofocus
          />


          <v-text-field
            v-model.number="formData.floor"
            label="Floor"
            type="number"
            variant="outlined"
            prepend-inner-icon="mdi-stairs"
            :error-messages="formErrors.floor"
            hint="Ground floor = 0, Basement = -1, etc."
            persistent-hint
          />


          <div class="mt-4">
            <p class="text-caption text-medium-emphasis mb-2">Quick Select:</p>
            <v-chip-group v-model="formData.floor" column>
              <v-chip :value="-1" size="small" variant="outlined">Basement</v-chip>
              <v-chip :value="0" size="small" variant="outlined">Ground</v-chip>
              <v-chip :value="1" size="small" variant="outlined">1st Floor</v-chip>
              <v-chip :value="2" size="small" variant="outlined">2nd Floor</v-chip>
              <v-chip :value="3" size="small" variant="outlined">3rd Floor</v-chip>
            </v-chip-group>
          </div>
        </v-card-text>
        <v-divider />
        <v-card-actions>
          <v-spacer />
          <v-btn variant="text" @click="dialog = false" :disabled="saving">
            Cancel
          </v-btn>
          <v-btn color="primary" :loading="saving" @click="saveRoom">
            <v-icon start>{{ editMode ? 'mdi-content-save' : 'mdi-plus' }}</v-icon>
            {{ editMode ? 'Save Changes' : 'Create' }}
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>


    <v-dialog v-model="deleteDialog" max-width="400">
      <v-card rounded="lg">
        <v-card-title class="d-flex align-center text-error">
          <v-icon start color="error">mdi-alert-circle</v-icon>
          Delete Room?
        </v-card-title>
        <v-divider />
        <v-card-text class="pt-4">
          <p>Are you sure you want to delete <strong>{{ roomToDelete?.name }}</strong>?</p>
          <p class="text-caption text-medium-emphasis mt-2">
            This action cannot be undone. All devices in this room will need to be reassigned.
          </p>
        </v-card-text>
        <v-divider />
        <v-card-actions>
          <v-spacer />
          <v-btn variant="text" @click="deleteDialog = false" :disabled="deleting">
            Cancel
          </v-btn>
          <v-btn color="error" :loading="deleting" @click="deleteRoom">
            <v-icon start>mdi-delete</v-icon>
            Delete
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>


    <v-snackbar v-model="showNotification" :timeout="3000" color="primary">
      {{ notificationMessage }}
      <template #actions>
        <v-btn variant="text" @click="showNotification = false">Close</v-btn>
      </template>
    </v-snackbar>
  </div>
</template>

<style scoped lang="scss">
.rooms-view {
  padding: 24px;
}

.rooms-view__header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  flex-wrap: wrap;
  gap: 16px;
}

.rooms-view__add {
  background: rgba(var(--v-theme-primary), 0.15) !important;
  border: 1px solid rgb(var(--v-theme-primary));
  box-shadow: 0 0 12px rgba(var(--v-theme-primary), 0.3);
  transition: all 0.3s ease;

  &:hover {
    background: rgba(var(--v-theme-primary), 0.25) !important;
    box-shadow: 0 0 20px rgba(var(--v-theme-primary), 0.5);
    transform: translateY(-2px);
  }
}

.room-card {
  backdrop-filter: blur(12px);
  transition: all 0.3s ease;
  border: 1px solid rgba(var(--v-theme-primary), 0.1);

  &:hover {
    border-color: rgba(var(--v-theme-primary), 0.3);
    transform: translateY(-4px);
    box-shadow: 0 8px 24px rgba(var(--v-theme-primary), 0.15);
  }
}

.room-card__header {
  display: flex;
  align-items: center;
  gap: 16px;
}

.room-card__avatar {
  flex-shrink: 0;
}

.room-card__info {
  flex: 1;
  min-width: 0;
}

.room-card__name {
  font-size: 1.25rem;
  font-weight: 600;
  margin: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.room-card__floor {
  margin: 4px 0 0;
  opacity: 0.7;
  font-size: 0.875rem;
}

.room-card__stats {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 4px;
}

.room-card__details {
  padding: 12px;
  background: rgba(var(--v-theme-surface-variant), 0.3);
  border-radius: 8px;

  p {
    margin: 4px 0;
  }
}

.room-form {
  border: 1px solid rgb(var(--v-theme-primary));
  box-shadow: 0 0 20px rgba(var(--v-theme-primary), 0.2);
}
</style>

