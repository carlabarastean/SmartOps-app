import 'vuetify/lib/styles/main.sass'
import { createVuetify, type ThemeDefinition } from 'vuetify'
import { aliases, mdi } from 'vuetify/iconsets/mdi'

// Dark theme configuration (Cyberpunk/Industrial)
const darkTheme: ThemeDefinition = {
  dark: true,
  colors: {
    primary: '#00E5FF',
    secondary: '#00B8D4',
    accent: '#FF9800',
    error: '#F44336',
    warning: '#FF9800',
    info: '#00E5FF',
    success: '#4CAF50',
    background: '#0A0E27',
    surface: '#1E1E1E',
    'on-background': '#FFFFFF',
    'on-surface': '#FFFFFF',
    'on-primary': '#000000',
    'surface-variant': '#2D2D2D',
    'on-surface-variant': '#E0E0E0',
  },
}

// Light theme configuration (Clean/Professional)
const lightTheme: ThemeDefinition = {
  dark: false,
  colors: {
    primary: '#00ACC1',
    secondary: '#0097A7',
    accent: '#FF6F00',
    error: '#D32F2F',
    warning: '#F57C00',
    info: '#0288D1',
    success: '#388E3C',
    background: '#F5F5F5',
    surface: '#FFFFFF',
    'on-background': '#1A1A1A',
    'on-surface': '#1A1A1A',
    'on-primary': '#FFFFFF',
    'surface-variant': '#E0E0E0',
    'on-surface-variant': '#424242',
  },
}

const vuetify = createVuetify({
  icons: {
    defaultSet: 'mdi',
    aliases,
    sets: {
      mdi,
    },
  },
  theme: {
    defaultTheme: 'dark',
    themes: {
      dark: darkTheme,
      light: lightTheme,
    },
  },
})

// Make vuetify instance globally available for theme switching
;(window as any).vuetify = vuetify

export default vuetify

