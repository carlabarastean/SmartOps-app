<script setup lang="ts">
import { ref } from 'vue'
import { loginUser } from '../services/api'
import type { User } from '../types'

const props = defineProps<{ modelValue: boolean }>()
const emit = defineEmits(['update:modelValue', 'logged-in'])

const username = ref('')
const password = ref('')
const loading = ref(false)
const error = ref('')
const showPassword = ref(false)

const close = () => {
  emit('update:modelValue', false)
  username.value = ''
  password.value = ''
  error.value = ''
}

const sanitizeUsername = (value: string) => value.replace(/[^\w .-]/g, '').trim()

const login = async () => {
  const cleanUsername = sanitizeUsername(username.value)
  if (!cleanUsername || !password.value) {
    error.value = 'Username and password are required.'
    return
  }
  loading.value = true
  error.value = ''
  try {
    const response = await loginUser({ username: cleanUsername, password: password.value })
    emit('logged-in', response.data as User)
    close()
  } catch (err) {
    error.value = 'Login failed. Backend unavailable?'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <v-dialog :model-value="props.modelValue" persistent max-width="420" @update:model-value="emit('update:modelValue', $event)">
    <v-card class="login-card">
      <v-card-title class="login-card__title">AUTHENTICATION REQUIRED</v-card-title>
      <v-card-text>
        <p class="login-card__subtitle">Secure access channel</p>
        <v-text-field
          v-model="username"
          label="Username"
          variant="outlined"
          class="mb-3"
          color="#00E5FF"
          autocomplete="off"
        />
        <v-text-field
          v-model="password"
          :type="showPassword ? 'text' : 'password'"
          label="Password"
          variant="outlined"
          class="mb-1"
          color="#00E5FF"
          :append-inner-icon="showPassword ? 'mdi-eye-off' : 'mdi-eye'"
          @click:append-inner="showPassword = !showPassword"
        />
        <small class="login-card__hint">Use any alias to auto-register.</small>
        <v-alert v-if="error" type="error" density="compact" class="mt-3">{{ error }}</v-alert>
      </v-card-text>
      <v-card-actions>
        <v-spacer />
        <v-btn variant="text" color="grey-lighten-1" @click="close">Cancel</v-btn>
        <v-btn color="#00E5FF" :loading="loading" @click="login">Login</v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<style scoped lang="scss">
.login-card {
  background: rgba(20, 20, 20, 0.95);
  border: 1px solid #00e5ff;
  box-shadow: 0 0 20px rgba(0, 229, 255, 0.2);
  color: #f5f5f5;
}

.login-card__title {
  font-family: 'Space Mono', monospace;
  letter-spacing: 0.2em;
}

.login-card__subtitle {
  font-size: 0.85rem;
  opacity: 0.7;
  margin-bottom: 12px;
}

.login-card__hint {
  opacity: 0.6;
}

.login-card :deep(.v-field__outline) {
  border-color: rgba(255, 255, 255, 0.2);
}

.login-card :deep(.v-field--focused .v-field__outline) {
  border-color: #00e5ff;
  box-shadow: 0 0 10px rgba(0, 229, 255, 0.4);
}

.login-card :deep(.v-label) {
  color: rgba(255, 255, 255, 0.8) !important;
}

.login-card :deep(input) {
  color: #f5f5f5;
}
</style>

