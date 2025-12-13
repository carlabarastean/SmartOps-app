<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import Dashboard from './components/Dashboard.vue'
import RoomList from './components/RoomList.vue'
import DeviceList from './components/DeviceList.vue'
import AISuggestions from './components/AISuggestions.vue'
import Settings from './components/Settings.vue'
import type { User } from './types'
import { getAllUsers, createUser } from './services/api'
import { useCurrentUser } from './composables/useCurrentUser'
import { initializeTheme } from './composables/useTheme'

const { currentUser, setCurrentUser } = useCurrentUser()

type ViewName = 'dashboard' | 'rooms' | 'devices' | 'ai' | 'settings'

const drawer = ref(true)
const systemOnline = ref(true)
const currentView = ref<ViewName>('dashboard')
const navItems: { icon: string; label: string; view?: ViewName; divider?: boolean }[] = [
  { icon: 'mdi-view-dashboard', label: 'Dashboard', view: 'dashboard' },
  { icon: 'mdi-home-automation', label: 'Rooms', view: 'rooms' },
  { icon: 'mdi-lightbulb-on-outline', label: 'Devices', view: 'devices' },
  { icon: 'mdi-robot', label: 'AI Insights', view: 'ai' },
  { icon: 'mdi-cog', label: 'Settings', view: 'settings', divider: true },
]

const statusColor = computed(() => (systemOnline.value ? 'green-accent-3' : 'orange-accent-3'))
const statusLabel = computed(() => (systemOnline.value ? 'ONLINE' : 'MAINTENANCE'))

const setView = (view: ViewName | undefined) => {
  if (view) {
    currentView.value = view
  }
}

const users = ref<User[]>([])
const userMenu = ref(false)
const createUserDialog = ref(false)
const newUser = ref({ username: '', email: '' })
const userError = ref('')
const saveUserLoading = ref(false)

const loadUsers = async () => {
  try {
    const response = await getAllUsers()
    users.value = response.data
    const storedId = typeof window !== 'undefined' ? window.localStorage.getItem('currentUserId') : null
    if (storedId) {
      const found = response.data.find((user) => user.id === Number(storedId))
      if (found) {
        setCurrentUser(found)
      }
    }
  } catch (err) {
    userError.value = 'Unable to load users.'
  }
}

const selectUser = (user: User) => {
  setCurrentUser(user)
  if (typeof window !== 'undefined') {
    window.localStorage.setItem('currentUserId', String(user.id))
  }
  userMenu.value = false
}

const openCreateUser = () => {
  newUser.value = { username: '', email: '' }
  createUserDialog.value = true
}

const saveNewUser = async () => {
  if (!newUser.value.username || !newUser.value.email) {
    userError.value = 'Username and email are required.'
    return
  }
  saveUserLoading.value = true
  userError.value = ''
  try {
    const response = await createUser(newUser.value)
    createUserDialog.value = false
    await loadUsers()
    selectUser(response.data)
  } catch (err) {
    userError.value = 'Failed to create user.'
  } finally {
    saveUserLoading.value = false
  }
}

onMounted(() => {
  loadUsers()
  initializeTheme()
})
</script>

<template>
  <v-app class="app-shell">
    <v-navigation-drawer
      v-model="drawer"
      class="app-shell__drawer"
      :permanent="$vuetify.display.mdAndUp"
      elevation="8"
    >
      <div class="drawer__logo">
        <v-icon size="34">mdi-home-analytics</v-icon>
        <div>
          <p class="drawer__title">SmartOps</p>
          <small class="drawer__subtitle">Control Suite</small>
        </div>
      </div>
      <v-list density="comfortable">
        <template v-for="item in navItems" :key="item.label">
          <v-divider v-if="item.divider" class="my-2" />
          <v-list-item
            :prepend-icon="item.icon"
            class="drawer__item"
            :class="{ 'drawer__item--active': item.view && currentView === item.view }"
            @click="setView(item.view)"
          >
            <v-list-item-title>{{ item.label }}</v-list-item-title>
          </v-list-item>
        </template>
      </v-list>
      <v-spacer />
      <div class="drawer__footer">
        <p class="text-caption">Firmware 2.7.4</p>
        <p class="text-caption">Last sync 2m ago</p>
      </div>
    </v-navigation-drawer>

    <v-app-bar
      flat
      class="app-shell__appbar"
      :elevation="0"
      density="comfortable"
    >
      <v-btn icon variant="text" class="mr-2" @click="drawer = !drawer">
        <v-icon>mdi-menu</v-icon>
      </v-btn>
      <v-chip :color="statusColor" size="large" class="text-uppercase" label>
        <v-icon start>mdi-lan-connect</v-icon>
        {{ statusLabel }}
      </v-chip>
      <v-spacer />
      <v-menu v-model="userMenu" offset-y>
        <template #activator="{ props }">
          <v-avatar v-bind="props" :color="currentUser ? 'green-darken-2' : 'blue-grey-darken-3'" size="38">
            <v-icon>mdi-account</v-icon>
          </v-avatar>
        </template>
        <v-card class="user-menu">
          <v-card-title>User Profile</v-card-title>
          <v-card-text>
            <v-alert v-if="userError" type="error" density="compact" class="mb-2">{{ userError }}</v-alert>
            <v-list v-if="users.length">
              <v-list-item
                v-for="user in users"
                :key="user.id"
                @click="selectUser(user)"
                :class="{ 'user-menu__item--active': currentUser?.id === user.id }"
              >
                <v-list-item-title>{{ user.username }}</v-list-item-title>
                <v-list-item-subtitle>{{ user.email }}</v-list-item-subtitle>
              </v-list-item>
            </v-list>
            <v-alert v-else type="info" density="compact">No users yet. Create one below.</v-alert>
          </v-card-text>
          <v-card-actions>
            <v-btn color="#00E5FF" variant="tonal" size="small" @click="openCreateUser">
              <v-icon start>mdi-account-plus</v-icon>
              New User
            </v-btn>
          </v-card-actions>
        </v-card>
      </v-menu>
    </v-app-bar>

    <v-main class="app-shell__main">
      <Dashboard v-if="currentView === 'dashboard'" :current-user="currentUser" />
      <RoomList v-else-if="currentView === 'rooms'" :current-user="currentUser" />
      <DeviceList v-else-if="currentView === 'devices'" />
      <AISuggestions v-else-if="currentView === 'ai'" :devices="[]" :current-user="currentUser" />
      <Settings v-else-if="currentView === 'settings'" />
      <div v-else class="coming-soon">
        <v-icon size="64" color="primary">mdi-construction</v-icon>
        <h2 class="text-h4 mt-4">Coming Soon</h2>
        <p class="text-subtitle-1 text-medium-emphasis">This feature is under development</p>
      </div>
    </v-main>

    <v-dialog v-model="createUserDialog" max-width="420">
      <v-card class="user-form">
        <v-card-title>Create User</v-card-title>
        <v-card-text>
          <v-text-field v-model="newUser.username" label="Username" variant="outlined" class="mb-3" />
          <v-text-field v-model="newUser.email" label="Email" variant="outlined" type="email" />
        </v-card-text>
        <v-card-actions>
          <v-spacer />
          <v-btn variant="text" @click="createUserDialog = false">Cancel</v-btn>
          <v-btn color="#00E5FF" :loading="saveUserLoading" @click="saveNewUser">Save</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </v-app>
</template>

<style scoped lang="scss">
// Base app styles using CSS variables for theme support
.app-shell {
  background: rgb(var(--v-theme-background));
  color: rgb(var(--v-theme-on-background));
}

.app-shell__drawer {
  background: rgb(var(--v-theme-surface)) !important;
  color: rgb(var(--v-theme-on-surface));
  backdrop-filter: blur(20px);
}

.drawer__logo {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 24px 16px 12px;
}

.drawer__title {
  margin: 0;
  font-weight: 600;
  letter-spacing: 0.08em;
}

.drawer__subtitle {
  opacity: 0.6;
}

.drawer__item {
  border-radius: 12px;
  margin: 4px 12px;
  transition: all 0.3s;
}

.drawer__item:hover {
  background: rgba(var(--v-theme-primary), 0.08);
}

.drawer__item--active {
  border-left: 3px solid rgb(var(--v-theme-primary));
  background: rgba(var(--v-theme-primary), 0.12);
  box-shadow: 0 0 12px rgba(var(--v-theme-primary), 0.3);
}

.drawer__item--active .v-icon {
  color: rgb(var(--v-theme-primary));
}

.drawer__footer {
  padding: 16px;
  opacity: 0.6;
}

.app-shell__appbar {
  background: rgba(var(--v-theme-surface), 0.85) !important;
  backdrop-filter: blur(18px);
}

.app-shell__main {
  min-height: 100vh;
}

.user-menu {
  min-width: 260px;
}

.user-menu__item--active {
  background: rgba(var(--v-theme-primary), 0.15);
}

.user-form {
  background: rgb(var(--v-theme-surface));
}

.coming-soon {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 60vh;
  padding: 40px;
}
</style>
