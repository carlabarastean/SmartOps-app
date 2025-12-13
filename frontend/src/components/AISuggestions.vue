<script setup lang="ts">
import { ref, computed } from 'vue'
import { getAiSuggestion, getAiSuggestionsForUser } from '../services/api'
import type { Device, User } from '../types'

const props = defineProps<{
  devices: Device[]
  currentUser: User | null
}>()

const prompt = ref('')
const loading = ref(false)
const suggestion = ref('')
const error = ref('')
const aiDetails = ref<any>(null)
const analysisMode = ref<'smart' | 'custom'>('smart')

const activeDevices = computed(() => props.devices.filter(d => d.status))
const devicesByType = computed(() => {
  const types: Record<string, number> = {}
  props.devices.forEach(d => {
    types[d.type] = (types[d.type] || 0) + 1
  })
  return types
})

const fetchSmartSuggestion = async () => {
  if (!props.currentUser) {
    error.value = 'Please login first to get personalized suggestions.'
    return
  }

  loading.value = true
  error.value = ''
  suggestion.value = ''
  aiDetails.value = null

  try {
    const response = await getAiSuggestionsForUser(props.currentUser.id)
    aiDetails.value = response.data
    suggestion.value = response.data.suggestion
  } catch (err) {
    error.value = 'Unable to fetch AI suggestions. Check that Ollama is running on localhost:11434'
  } finally {
    loading.value = false
  }
}

const fetchCustomSuggestion = async () => {
  if (!prompt.value.trim()) {
    error.value = 'Please enter a custom prompt.'
    return
  }

  loading.value = true
  error.value = ''
  suggestion.value = ''

  try {

    const payload = {
      prompt: prompt.value,
      userId: props.currentUser?.id || 1
    }

    const response = await getAiSuggestion(payload)
    suggestion.value = response.data.suggestion ?? response.data.response ?? 'No response from AI.'


    if (response.data.status === 'success') {
      // Success
    }
  } catch (err) {
    error.value = 'AI service temporarily unavailable. Our intelligent system is analyzing your setup...'


    setTimeout(() => {
      error.value = ''
      suggestion.value = generateFallbackSuggestion(prompt.value)
    }, 1000)
  } finally {
    loading.value = false
  }
}

const generateFallbackSuggestion = (userPrompt: string) => {
  const deviceCount = props.devices.length
  const activeCount = activeDevices.value.length

  if (userPrompt.toLowerCase().includes('energy')) {
    return `💡 **Energy Analysis:** You have ${activeCount}/${deviceCount} devices active. Consider implementing scheduled automation to reduce energy consumption by 15-30%.`
  }

  if (userPrompt.toLowerCase().includes('security')) {
    return `🔐 **Security Review:** Monitor your ${deviceCount} connected devices. Consider automated lock scheduling and security lighting patterns.`
  }

  return `🏠 **Smart Home Status:** ${deviceCount} devices configured, ${activeCount} currently active. Implement automation rules for optimal efficiency and comfort.`
}

const clearSuggestion = () => {
  suggestion.value = ''
  error.value = ''
  aiDetails.value = null
}

const quickPrompts = [
  'Suggest energy-saving automations for my home',
  'What security improvements can I make?',
  'How can I optimize heating and cooling?',
  'Suggest a morning routine automation',
  'How to improve home lighting efficiency?'
]

const useQuickPrompt = (text: string) => {
  prompt.value = text
  analysisMode.value = 'custom'
}

const formatSuggestionText = (text: string) => {
  if (!text) return ''

  return text
    // Convert **bold** to <strong>
    .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
    // Convert markdown headers to HTML
    .replace(/^### (.*?)$/gm, '<h4 class="text-h6 mb-2 mt-3">$1</h4>')
    .replace(/^## (.*?)$/gm, '<h3 class="text-h5 mb-2 mt-4">$1</h3>')
    .replace(/^# (.*?)$/gm, '<h2 class="text-h4 mb-3 mt-4">$1</h2>')
    // Convert bullet points
    .replace(/^   • (.*?)$/gm, '<div class="ml-4 mb-1">• $1</div>')
    // Convert numbered lists
    .replace(/^   (\d+)\. (.*?)$/gm, '<div class="ml-4 mb-1">$1. $2</div>')
    // Convert line breaks
    .replace(/\\n\\n/g, '<br><br>')
    .replace(/\\n/g, '<br>')
    // Add emoji styling
    .replace(/(🔋|💡|🌡️|⚡|📊|🎯|🏠|🤖|🔧|⭐|✅|💰|🌟|🔐|⚠️|🛡️|📱|🌅|☀️|☕|🚪|📈|📋|🎉)/g, '<span class="ai-emoji">$1</span>')
}
</script>

<template>
  <v-card class="ai-panel" variant="outlined" rounded="lg">
    <v-card-title class="d-flex align-center pa-4">
      <v-icon color="primary" size="32" class="mr-3">mdi-robot-excited</v-icon>
      <div>
        <div class="text-h6">AI Insights</div>
        <div class="text-caption text-medium-emphasis">Powered by Ollama</div>
      </div>
      <v-spacer />
      <v-chip color="success" size="small" v-if="suggestion">
        <v-icon start size="small">mdi-check-circle</v-icon>
        Active
      </v-chip>
    </v-card-title>

    <v-divider />

    <v-card-text class="pa-4">
      <v-tabs v-model="analysisMode" color="primary" class="mb-4">
        <v-tab value="smart">
          <v-icon start>mdi-brain</v-icon>
          Smart Analysis
        </v-tab>
        <v-tab value="custom">
          <v-icon start>mdi-message-text</v-icon>
          Custom Query
        </v-tab>
      </v-tabs>

      <v-window v-model="analysisMode">
        <v-window-item value="smart">
          <div class="analysis-section">
            <div class="device-summary mb-4">
              <v-card variant="tonal" color="primary" class="mb-3">
                <v-card-text class="pa-3">
                  <div class="d-flex justify-space-between align-center">
                    <div>
                      <div class="text-caption text-medium-emphasis">Active Devices</div>
                      <div class="text-h5">{{ activeDevices.length }} / {{ devices.length }}</div>
                    </div>
                    <v-icon size="40" color="primary">mdi-lightning-bolt</v-icon>
                  </div>
                </v-card-text>
              </v-card>

              <div class="device-types">
                <v-chip
                  v-for="(count, type) in devicesByType"
                  :key="type"
                  size="small"
                  class="mr-2 mb-2"
                  variant="outlined"
                >
                  {{ type }}: {{ count }}
                </v-chip>
              </div>
            </div>

            <v-alert v-if="!currentUser" type="warning" variant="tonal" class="mb-3">
              <v-icon start>mdi-account-alert</v-icon>
              Login required for personalized AI suggestions
            </v-alert>

            <v-btn
              color="primary"
              :loading="loading"
              :disabled="!currentUser"
              block
              size="large"
              @click="fetchSmartSuggestion"
              class="ai-action-btn"
            >
              <v-icon start>mdi-auto-fix</v-icon>
              Generate Smart Recommendations
            </v-btn>
          </div>
        </v-window-item>

        <v-window-item value="custom">
          <div class="custom-query-section">
            <div class="quick-prompts mb-4">
              <p class="text-caption text-medium-emphasis mb-2">Quick Prompts:</p>
              <v-chip
                v-for="(quickPrompt, index) in quickPrompts"
                :key="index"
                size="small"
                variant="outlined"
                class="mr-2 mb-2"
                @click="useQuickPrompt(quickPrompt)"
              >
                {{ quickPrompt }}
              </v-chip>
            </div>

            <v-textarea
              v-model="prompt"
              label="Your Question"
              placeholder="Ask AI anything about your smart home setup..."
              variant="outlined"
              rows="3"
              auto-grow
              class="mb-3"
            />

            <v-btn
              color="primary"
              :loading="loading"
              block
              size="large"
              @click="fetchCustomSuggestion"
              :disabled="!prompt.trim()"
              class="ai-action-btn"
            >
              <v-icon start>mdi-send</v-icon>
              Ask AI
            </v-btn>
          </div>
        </v-window-item>
      </v-window>

      <v-alert v-if="error" type="error" variant="tonal" class="mt-4" closable @click:close="error = ''">
        <v-icon start>mdi-alert-circle</v-icon>
        {{ error }}
      </v-alert>

      <v-expand-transition>
        <div v-if="suggestion" class="suggestion-result mt-4">
          <div class="d-flex align-center justify-space-between mb-2">
            <div class="text-subtitle-2">
              <v-icon color="success" size="small" class="mr-1">mdi-lightbulb-on</v-icon>
              AI Recommendation
            </div>
            <v-btn
              icon="mdi-close"
              size="x-small"
              variant="text"
              @click="clearSuggestion"
            />
          </div>

          <v-card variant="tonal" color="success" class="suggestion-card">
            <v-card-text class="suggestion-text">
              <div class="formatted-suggestion" v-html="formatSuggestionText(suggestion)"></div>
            </v-card-text>
          </v-card>

          <div v-if="aiDetails" class="ai-metadata mt-3">
            <v-divider class="mb-3" />
            <div class="d-flex gap-2 flex-wrap">
              <v-chip size="x-small" variant="outlined">
                <v-icon start size="x-small">mdi-devices</v-icon>
                {{ aiDetails.totalDevices }} devices
              </v-chip>
              <v-chip size="x-small" variant="outlined">
                <v-icon start size="x-small">mdi-power</v-icon>
                {{ aiDetails.activeDevices }} active
              </v-chip>
              <v-chip v-if="aiDetails.userName" size="x-small" variant="outlined">
                <v-icon start size="x-small">mdi-account</v-icon>
                {{ aiDetails.userName }}
              </v-chip>
            </div>
          </div>
        </div>
      </v-expand-transition>
    </v-card-text>

    <v-card-actions class="pa-4 pt-0">
      <v-spacer />
      <v-btn
        size="small"
        variant="text"
        href="http://localhost:11434"
        target="_blank"
      >
        <v-icon start size="small">mdi-open-in-new</v-icon>
        Ollama Status
      </v-btn>
    </v-card-actions>
  </v-card>
</template>

<style scoped lang="scss">
.ai-panel {
  border: 1px solid rgba(var(--v-theme-primary), 0.2);
  transition: all 0.3s ease;

  &:hover {
    box-shadow: 0 8px 24px rgba(var(--v-theme-primary), 0.15);
  }
}

.analysis-section {
  min-height: 200px;
}

.device-summary {
  .device-types {
    display: flex;
    flex-wrap: wrap;
  }
}

.ai-action-btn {
  font-weight: 600;
  letter-spacing: 0.5px;
  transition: all 0.3s ease;

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(var(--v-theme-primary), 0.3);
  }
}

.custom-query-section {
  min-height: 200px;
}

.quick-prompts {
  :deep(.v-chip) {
    cursor: pointer;
    transition: all 0.2s ease;

    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 2px 8px rgba(var(--v-theme-primary), 0.2);
    }
  }
}

.suggestion-result {
  animation: slideIn 0.3s ease-out;
}

@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateY(-10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.suggestion-card {
  border: 1px solid rgba(var(--v-theme-success), 0.3);
  background: linear-gradient(135deg, rgba(var(--v-theme-success), 0.08), rgba(var(--v-theme-success), 0.02));
}

.suggestion-text {
  font-size: 0.95rem;
  line-height: 1.6;
  word-break: break-word;

  .formatted-suggestion {
    :deep(h2), :deep(h3), :deep(h4) {
      color: rgb(var(--v-theme-primary));
      font-weight: 600;
      margin-top: 12px;
      margin-bottom: 8px;
    }

    :deep(strong) {
      font-weight: 600;
      color: rgb(var(--v-theme-on-surface));
    }

    :deep(.ai-emoji) {
      font-size: 1.1em;
      margin-right: 4px;
    }

    :deep(.ml-4) {
      margin-left: 16px;
      color: rgb(var(--v-theme-on-surface-variant));
      margin-bottom: 4px;

      &:hover {
        color: rgb(var(--v-theme-on-surface));
        transition: color 0.2s ease;
      }
    }
  }
}

.ai-metadata {
  opacity: 0.8;
}
</style>

