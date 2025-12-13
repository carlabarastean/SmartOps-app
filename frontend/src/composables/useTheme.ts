import { ref, watch } from 'vue'

export type Theme = 'dark' | 'light'

const STORAGE_KEY = 'smart-home-theme'

// Load theme from localStorage or default to dark
const loadTheme = (): Theme => {
  const stored = localStorage.getItem(STORAGE_KEY)
  return (stored === 'light' || stored === 'dark') ? stored : 'dark'
}

// Theme state
export const currentTheme = ref<Theme>(loadTheme())

// Watch for theme changes and persist
watch(currentTheme, (newTheme) => {
  localStorage.setItem(STORAGE_KEY, newTheme)
  applyTheme(newTheme)
})

// Apply theme to Vuetify
export const applyTheme = (theme: Theme) => {
  const vuetifyTheme = (window as any).vuetify?.theme
  if (vuetifyTheme) {
    vuetifyTheme.global.name.value = theme
  }
}

// Toggle theme
export const toggleTheme = () => {
  currentTheme.value = currentTheme.value === 'dark' ? 'light' : 'dark'
}

// Set specific theme
export const setTheme = (theme: Theme) => {
  currentTheme.value = theme
}

// Initialize theme on app start
export const initializeTheme = () => {
  applyTheme(currentTheme.value)
}

