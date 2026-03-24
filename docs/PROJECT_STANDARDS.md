# Padrões do projeto MentorIA

## Monorepo

| Pasta | Responsabilidade |
|-------|------------------|
| `backend/` | Spring Boot 3 — API REST, segurança, JPA, integrações (Stripe, Supabase JWT, AI service). |
| `frontend/` | Nuxt 4 — UI, Pinia, `@nuxtjs/supabase` (Auth no cliente + sync com Spring). |
| `ai-service/` | Python/FastAPI — chamadas ao modelo (Claude). |
| Raiz `.env` | Variáveis compartilhadas; o Nuxt carrega este arquivo automaticamente (`nuxt.config.ts`). |

## Backend (Java)

- **Camadas:** `controller` → `service` → `repository` → `model`.
- **DTOs:** requests em `dto/request`, responses em `dto/response` (incluindo subpacotes como `admin`).
- **Exceções:** usar `BusinessException`, `ApiUnauthorizedException`, etc.; respostas padronizadas em `ApiResponse` via `GlobalExceptionHandler`.
- **Segurança:** `SecurityConfig` + `JwtAuthenticationFilter`; papéis `ROLE_USER`, `ROLE_ADMIN`, `ROLE_{PLAN}`.
- **Context path:** todas as rotas da API ficam sob `/api` (`server.servlet.context-path`).
- **Perfis:** `application-prod.yml` desativa seed de usuário de teste (`app.seed.test-user.enabled=false`).

## Frontend (Nuxt)

- **Páginas:** `app/pages/`; layouts em `app/layouts/`; componentes em `app/components/`.
- **Estado global:** Pinia em `app/stores/` (ex.: `auth`).
- **HTTP:** composable `useApi()` — base URL em `runtimeConfig.public.apiBase` (`NUXT_PUBLIC_API_BASE`).
- **Middleware:** `auth` (rotas logadas), `admin` (só `role === 'ADMIN'`).
- **Porta dev:** `3001` por padrão (`nuxt.config.ts`) para evitar conflito com outros apps na `3000`.

## Agentes de IA (visão de produto)

Três papéis documentados no painel admin (orquestração futura):

1. **Pesquisa** — curadoria e validação de materiais.
2. **Orquestração** — ponte usuário ↔ backend ↔ agentes.
3. **Planos de aula** — geração de cronogramas didáticos.

Status e integrações reais evoluem com filas, WebSocket e o `ai-service`.

## Commits e branches

- Mensagens claras em português ou inglês, no imperativo (`feat: painel admin`, `fix: CORS`).
- Não commitar `.env` nem segredos.

## Testes manuais rápidos

- API: `GET /api/actuator/health`
- Conta local: `POST /api/auth/login` com usuário de seed (dev).
- Admin: `GET /api/admin/overview` com token de usuário `ADMIN`.
