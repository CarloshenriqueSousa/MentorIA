import { createClient } from '@supabase/supabase-js'

let cachedClient: ReturnType<typeof createClient> | null = null

export const useSupabase = () => {
  const config = useRuntimeConfig()
  const supabaseUrl = config.public.supabaseUrl as string
  const supabaseKey = config.public.supabaseKey as string

  if (!supabaseUrl || !supabaseKey) {
    throw new Error('Supabase não configurado. Defina NUXT_PUBLIC_SUPABASE_URL e NUXT_PUBLIC_SUPABASE_KEY.')
  }

  if (!cachedClient) {
    cachedClient = createClient(supabaseUrl, supabaseKey)
  }

  return cachedClient
}
