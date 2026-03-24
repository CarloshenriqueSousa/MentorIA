import type { SupabaseClient } from '@supabase/supabase-js'

/**
 * Cliente Supabase via módulo oficial `@nuxtjs/supabase`.
 *
 * Defina no `frontend/.env`:
 * - `SUPABASE_URL`
 * - `SUPABASE_KEY` (chave publishable / anon — nunca service_role no browser)
 *
 * Em código novo, pode usar diretamente `useSupabaseClient()` (auto-import).
 */
export const useSupabase = (): SupabaseClient => {
  return useSupabaseClient()
}
