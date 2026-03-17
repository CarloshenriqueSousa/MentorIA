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
    }
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
})