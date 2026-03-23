// @ts-check
// Nota: o `.nuxt/eslint.config.mjs` é gerado pelo `nuxt prepare`.
// Aqui evitamos essa dependência para que `pnpm lint` rode mesmo quando o
// `nuxt prepare` falhar (ex.: bindings nativos bloqueados no Windows).
import { createConfigForNuxt } from '@nuxt/eslint-config/flat'

export default createConfigForNuxt({
  features: {
    standalone: true,
    stylistic: false,
    // project usa TypeScript, então habilitamos as regras TS
    typescript: true,
    tooling: false,
    import: true,
    // Evita regras extras ligadas a geração/ordenação do Nuxt config
    nuxt: { sortConfigKeys: false }
  }
})
