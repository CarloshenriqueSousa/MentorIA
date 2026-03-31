import { config as loadEnv } from 'dotenv'
import { dirname, resolve } from 'node:path'
import { fileURLToPath } from 'node:url'

// Usa o `.env` da raiz do repositório (mesmo do Docker/Spring) + `frontend/.env` opcional
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
  devtools: { enabled: true },

  // 3000 costuma estar ocupado (ex.: Obsidian); use sempre http://localhost:3001 para o app
  devServer: {
    port: 3001,
    host: 'localhost',
  },

  modules: [
    '@nuxt/ui',
    '@pinia/nuxt',
    '@pinia/nuxt',
    '@nuxtjs/supabase',
    '@vite-pwa/nuxt',
  ],

  // Login continua no backend Spring; Supabase fica disponível para Auth/Storage/Realtime quando quiser.
  supabase: {
    redirect: false,
    // Gere `app/types/database.types.ts` com `supabase gen types` se quiser tipagem do schema.
    types: false,
  },

  css: ['~/assets/css/main.css'],

  pwa: {
    registerType: 'autoUpdate',
    manifest: {
      name: 'MentorIA',
      short_name: 'MentorIA',
      description: 'Seu mentor de estudos Pessoal com IA',
      theme_color: '#faf8f5',
      background_color: '#faf8f5',
      icons: [
        {
          src: '/favicon.ico',
          sizes: '64x64',
          type: 'image/x-icon'
        }
      ]
    },
    workbox: {
      navigateFallback: '/',
      globPatterns: ['**/*.{js,css,html,png,svg,ico}']
    },
    client: {
      installPrompt: true,
      periodicSyncForUpdates: 3600
    },
    devOptions: {
      enabled: false,
      suppressWarnings: true,
      navigateFallbackAllowlist: [/^\/$/],
      type: 'module',
    },
  },

  runtimeConfig: {
    public: {
      apiBase: process.env.NUXT_PUBLIC_API_BASE || 'http://localhost:8080/api',
      // Aliases legados (o módulo @nuxtjs/supabase usa runtimeConfig.public.supabase.* via SUPABASE_URL / SUPABASE_KEY)
      supabaseUrl: process.env.NUXT_PUBLIC_SUPABASE_URL || process.env.SUPABASE_URL || '',
      supabaseKey: process.env.NUXT_PUBLIC_SUPABASE_KEY || process.env.SUPABASE_KEY || '',
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
        { rel: 'preconnect', href: 'https://fonts.gstatic.com', crossorigin: '' },
        { rel: 'stylesheet', href: 'https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&family=Outfit:wght@600;700;800&display=swap' },
      ]
    }
  },

  colorMode: {
    preference: 'system',
    fallback: 'light',
  },

  vite: {
    build: {
      // Ajuda a evitar falhas por bindings nativos bloqueados no Windows durante o build.
      // (Mantém o build funcional, ainda que sem minificação.)
      minify: false,
    },
  },
})