import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import vuetify from 'vite-plugin-vuetify'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [
    vue(),
    vuetify({ autoImport: true }),
  ],
  css: {
    preprocessorOptions: {
      sass: {
        // @ts-ignore
        api: 'modern-compiler',
        // @ts-ignore
        quietDeps: true,
        // @ts-ignore
        silenceDeprecations: ['legacy-js-api', 'import', 'global-builtin', 'color-functions', 'if-function'],
      },
      scss: {
        // @ts-ignore
        api: 'modern-compiler',
        // @ts-ignore
        quietDeps: true,
        // @ts-ignore
        silenceDeprecations: ['legacy-js-api', 'import', 'global-builtin', 'color-functions', 'if-function'],
      }
    }
  },
  server: {
    proxy: {
      '/api': {
        target: 'http://localhost:8083', // Portul backend-ului tau
        changeOrigin: true,
        secure: false,
      },
    },
  },
})