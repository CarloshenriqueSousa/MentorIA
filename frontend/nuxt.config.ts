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

  modules: [
    '@nuxt/ui',
    '@pinia/nuxt',
  ],

  css: ['~/assets/css/main.css'],

  runtimeConfig: {
    public: {
      apiBase: process.env.NUXT_PUBLIC_API_BASE || 'http://localhost:8080/api',
      supabaseUrl: process.env.NUXT_PUBLIC_SUPABASE_URL || process.env.SUPABASE_URL || '',
      supabaseKey: process.env.NUXT_PUBLIC_SUPABASE_KEY || process.env.SUPABASE_KEY || '',
    }
  },

  routeRules: {
    '/dashboard/**': { ssr: false },
    '/chat/**': { ssr: false },
    '/study-plan/**': { ssr: false },
    '/auth/**': { ssr: false },
  },

  app: {
    head: {
      title: 'MentorIA',
      meta: [
        { name: 'description', content: 'Seu mentor de estudos com Inteligência Artificial' },
        { name: 'viewport', content: 'width=device-width, initial-scale=1' },
      ],
      link: [
        { rel: 'icon', type: 'image/x-icon', href: '/favicon.ico' }
      ]
    }
  },

  colorMode: {
    preference: 'light',
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