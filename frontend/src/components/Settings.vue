<script setup lang="ts">
import { computed } from 'vue'
import { currentTheme, toggleTheme, type Theme } from '../composables/useTheme'

const themeOptions = [
  { title: 'Dark Mode', value: 'dark' as Theme, icon: 'mdi-weather-night' },
  { title: 'Light Mode', value: 'light' as Theme, icon: 'mdi-weather-sunny' }
]

const selectedTheme = computed({
  get: () => currentTheme.value,
  set: (value: Theme) => {
    currentTheme.value = value
  }
})

const isDarkMode = computed(() => currentTheme.value === 'dark')
</script>

<template>
  <div class="settings-container">
    <v-container>
      <v-row>
        <v-col cols="12">
          <h1 class="text-h4 mb-2">
            <v-icon icon="mdi-cog" class="mr-2" />
            Settings
          </h1>
          <p class="text-subtitle-1 text-medium-emphasis mb-6">
            Customize your Smart Home dashboard experience
          </p>
        </v-col>
      </v-row>


      <v-row>
        <v-col cols="12">
          <v-card variant="outlined" rounded="lg">
            <v-card-title class="d-flex align-center">
              <v-icon icon="mdi-palette" class="mr-2" />
              Appearance
            </v-card-title>
            <v-divider />
            <v-card-text>
              <v-row>
                <v-col cols="12" md="6">
                  <div class="mb-4">
                    <h3 class="text-h6 mb-2">Theme</h3>
                    <p class="text-body-2 text-medium-emphasis mb-4">
                      Choose between light and dark mode
                    </p>

                    <v-radio-group v-model="selectedTheme" inline>
                      <v-radio
                        v-for="option in themeOptions"
                        :key="option.value"
                        :label="option.title"
                        :value="option.value"
                      >
                        <template #label>
                          <div class="d-flex align-center">
                            <v-icon :icon="option.icon" class="mr-2" />
                            {{ option.title }}
                          </div>
                        </template>
                      </v-radio>
                    </v-radio-group>
                  </div>


                  <v-btn
                    :prepend-icon="isDarkMode ? 'mdi-weather-sunny' : 'mdi-weather-night'"
                    :color="isDarkMode ? 'amber' : 'blue'"
                    variant="tonal"
                    @click="toggleTheme"
                  >
                    Switch to {{ isDarkMode ? 'Light' : 'Dark' }} Mode
                  </v-btn>
                </v-col>

                <v-col cols="12" md="6">

                  <div class="theme-preview" :class="{ 'theme-preview--light': !isDarkMode }">
                    <div class="theme-preview__header">
                      <div class="theme-preview__dot" />
                      <div class="theme-preview__dot" />
                      <div class="theme-preview__dot" />
                    </div>
                    <div class="theme-preview__body">
                      <div class="theme-preview__sidebar" />
                      <div class="theme-preview__content">
                        <div class="theme-preview__card" />
                        <div class="theme-preview__card theme-preview__card--small" />
                      </div>
                    </div>
                  </div>
                  <p class="text-center text-caption text-medium-emphasis mt-2">
                    Preview
                  </p>
                </v-col>
              </v-row>
            </v-card-text>
          </v-card>
        </v-col>
      </v-row>


      <v-row class="mt-4">
        <v-col cols="12">
          <v-card variant="outlined" rounded="lg">
            <v-card-title class="d-flex align-center">
              <v-icon icon="mdi-information" class="mr-2" />
              System Information
            </v-card-title>
            <v-divider />
            <v-card-text>
              <v-list lines="two">
                <v-list-item>
                  <template #prepend>
                    <v-icon icon="mdi-application" color="primary" />
                  </template>
                  <v-list-item-title>Application Version</v-list-item-title>
                  <v-list-item-subtitle>1.0.0</v-list-item-subtitle>
                </v-list-item>

                <v-list-item>
                  <template #prepend>
                    <v-icon icon="mdi-server" color="success" />
                  </template>
                  <v-list-item-title>Backend API</v-list-item-title>
                  <v-list-item-subtitle>http://localhost:8083/api</v-list-item-subtitle>
                </v-list-item>

                <v-list-item>
                  <template #prepend>
                    <v-icon icon="mdi-vuejs" color="success" />
                  </template>
                  <v-list-item-title>Framework</v-list-item-title>
                  <v-list-item-subtitle>Vue 3 + Vuetify 3</v-list-item-subtitle>
                </v-list-item>

                <v-list-item>
                  <template #prepend>
                    <v-icon icon="mdi-palette" :color="isDarkMode ? 'blue' : 'amber'" />
                  </template>
                  <v-list-item-title>Current Theme</v-list-item-title>
                  <v-list-item-subtitle>
                    {{ isDarkMode ? 'Dark Mode' : 'Light Mode' }}
                  </v-list-item-subtitle>
                </v-list-item>
              </v-list>
            </v-card-text>
          </v-card>
        </v-col>
      </v-row>


      <v-row class="mt-4">
        <v-col cols="12">
          <v-card variant="outlined" rounded="lg">
            <v-card-title class="d-flex align-center">
              <v-icon icon="mdi-home-automation" class="mr-2" />
              About Smart Home Dashboard
            </v-card-title>
            <v-divider />
            <v-card-text>
              <p class="text-body-2 mb-4">
                A comprehensive IoT management system for smart home devices with AI-powered automation suggestions.
              </p>
              <v-chip-group>
                <v-chip variant="outlined" prepend-icon="mdi-check-circle">17 API Endpoints</v-chip>
                <v-chip variant="outlined" prepend-icon="mdi-check-circle">AI Integration</v-chip>
                <v-chip variant="outlined" prepend-icon="mdi-check-circle">Real-time Control</v-chip>
                <v-chip variant="outlined" prepend-icon="mdi-check-circle">Dark/Light Mode</v-chip>
              </v-chip-group>
            </v-card-text>
          </v-card>
        </v-col>
      </v-row>
    </v-container>
  </div>
</template>

<style scoped lang="scss">
.settings-container {
  padding: 20px 0;
}

.theme-preview {
  width: 100%;
  height: 200px;
  border-radius: 12px;
  overflow: hidden;
  border: 2px solid rgba(255, 255, 255, 0.1);
  background: #1a1a1a;
  transition: all 0.3s ease;

  &--light {
    background: #f5f5f5;
    border-color: rgba(0, 0, 0, 0.1);

    .theme-preview__header {
      background: #e0e0e0;
    }

    .theme-preview__sidebar {
      background: #eeeeee;
    }

    .theme-preview__content {
      background: #fafafa;
    }

    .theme-preview__card {
      background: #ffffff;
      border-color: rgba(0, 0, 0, 0.1);
    }

    .theme-preview__dot {
      background: #bdbdbd;
    }
  }

  &__header {
    height: 30px;
    background: #2d2d2d;
    display: flex;
    align-items: center;
    padding: 0 12px;
    gap: 6px;
  }

  &__dot {
    width: 10px;
    height: 10px;
    border-radius: 50%;
    background: #616161;
  }

  &__body {
    display: flex;
    height: calc(100% - 30px);
  }

  &__sidebar {
    width: 60px;
    background: #252525;
    border-right: 1px solid rgba(255, 255, 255, 0.05);
  }

  &__content {
    flex: 1;
    padding: 12px;
    background: #1e1e1e;
  }

  &__card {
    background: #2d2d2d;
    border-radius: 6px;
    height: 50px;
    margin-bottom: 8px;
    border: 1px solid rgba(255, 255, 255, 0.05);

    &--small {
      height: 30px;
    }
  }
}
</style>

