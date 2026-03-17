import { defineStore } from 'pinia'

interface User {
  id: string
  name: string
  email: string
  planType: 'FREE' | 'BASIC' | 'PREMIUM'
  completedOnboarding: boolean
}

interface AuthState {
  user: User | null
  accessToken: string | null
  refreshToken: string | null
}

export const useAuthStore = defineStore('auth', {
  state: (): AuthState => ({
    user: null,
    accessToken: null,
    refreshToken: null,
  }),

  getters: {
    isLoggedIn: (state) => !!state.accessToken,
    isPaid: (state) => state.user?.planType !== 'FREE',
    needsOnboarding: (state) => state.user && !state.user.completedOnboarding,
  },

  actions: {
    setAuth(data: { accessToken: string; refreshToken: string; user: User }) {
      this.accessToken = data.accessToken
      this.refreshToken = data.refreshToken
      this.user = data.user

      // Persistir no localStorage
      if (process.client) {
        localStorage.setItem('access_token', data.accessToken)
        localStorage.setItem('refresh_token', data.refreshToken)
        localStorage.setItem('user', JSON.stringify(data.user))
      }
    },

    loadFromStorage() {
      if (process.client) {
        const token = localStorage.getItem('access_token')
        const refresh = localStorage.getItem('refresh_token')
        const user = localStorage.getItem('user')

        if (token && user) {
          this.accessToken = token
          this.refreshToken = refresh
          this.user = JSON.parse(user)
        }
      }
    },

    logout() {
      this.user = null
      this.accessToken = null
      this.refreshToken = null

      if (process.client) {
        localStorage.removeItem('access_token')
        localStorage.removeItem('refresh_token')
        localStorage.removeItem('user')
      }
    },

    updateUser(updates: Partial<User>) {
      if (this.user) {
        this.user = { ...this.user, ...updates }
        if (process.client) {
          localStorage.setItem('user', JSON.stringify(this.user))
        }
      }
    }
  }
})