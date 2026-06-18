import { config as loadEnv } from 'dotenv'
import { dirname, resolve } from 'node:path'
import { fileURLToPath } from 'node:url'

const __dirname = dirname(fileURLToPath(import.meta.url))
loadEnv({ path: resolve(__dirname, '..', '.env') })
loadEnv({ path: resolve(__dirname, '.env'), override: true })

export default defineNuxtConfig({
  typescript: {
    tsConfig: {
      compilerOptions: {
        types: ['node']
      }
    }
  },
  compatibilityDate: '2025-01-01',
  devtools: { enabled: false },

  devServer: {
    port: 3001,
    host: 'localhost',
  },

  modules: [
    '@nuxt/ui',
    '@pinia/nuxt',
  ],

  css: ['~/assets/css/main.css'],

  runtimeConfig: {
    public: {
      apiBase: process.env.NUXT_PUBLIC_API_BASE || 'http://localhost:8080/api',
    },
  },

  routeRules: {
    '/dashboard/**': { ssr: false },
    '/chat/**': { ssr: false },
    '/study-plan/**': { ssr: false },
    '/auth/**': { ssr: false },
    '/admin/**': { ssr: false },
  },

  app: {
    head: {
      title: 'MentorIA',
      meta: [
        { name: 'description', content: 'Seu mentor de estudos com Inteligência Artificial' },
        { name: 'viewport', content: 'width=device-width, initial-scale=1' },
      ],
      link: [
        { rel: 'icon', type: 'image/x-icon', href: '/favicon.ico' },
        { rel: 'preconnect', href: 'https://fonts.googleapis.com' },
        { rel: 'stylesheet', href: 'https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap' },
      ]
    }
  },

  colorMode: {
    preference: 'light',
    fallback: 'light',
  },

  vite: {
    build: {
      minify: false,
    },
  },
})
