# Rodando com Supabase

Este guia usa o PostgreSQL do Supabase e evita o container local de banco.

## 1) Criar `.env`

No diretório raiz do projeto:

```bash
cp .env.example .env
```

Preencha no `.env`:

- `DATABASE_URL` (use a URL JDBC do Supabase com `sslmode=require`)
- `DATABASE_USER`
- `DATABASE_PASSWORD`
- `SUPABASE_URL`
- `SUPABASE_KEY` (publishable/anon key)
- **`SUPABASE_JWT_SECRET`** — em **Settings → API → JWT Secret** (obrigatório para o Spring validar tokens do Supabase Auth; **não** é a anon/publishable key)
- `JWT_SECRET` (ainda usado para login legado `/auth/login`, se quiser manter)
- `ANTHROPIC_API_KEY`
- (`STRIPE_*` pode ficar placeholder para desenvolvimento local)

**Senha com `$`:** no `.env` usado pelo Docker Compose, cada `$` na `DATABASE_PASSWORD` deve ser escrito como `$$`, senão o Compose tenta expandir como variável.

## 1b) Frontend (Nuxt + `@nuxtjs/supabase`)

O projeto usa o módulo oficial **`@nuxtjs/supabase`** (`redirect: false` para manter login no Spring).

No diretório `frontend/`, em `frontend/.env`, defina **obrigatório**:

- `SUPABASE_URL` — URL do projeto (o módulo lê `process.env.SUPABASE_URL`)
- `SUPABASE_KEY` — chave **publishable/anon** (nunca `service_role` no browser)

Recomendado também:

- `NUXT_PUBLIC_API_BASE` — ex.: `http://localhost:8080/api`
- `NUXT_PUBLIC_SUPABASE_URL` / `NUXT_PUBLIC_SUPABASE_KEY` — aliases opcionais para o `runtimeConfig` legado

Uso no código: `useSupabaseClient()` (auto-import) ou o composable `useSupabase()` (wrapper).

### Fluxo de login (atual)

1. O Nuxt chama `signInWithPassword` / `signUp` no Supabase.
2. O frontend envia `POST /api/auth/supabase/session` com `{ accessToken, refreshToken }`.
3. O backend valida o JWT com `SUPABASE_JWT_SECRET`, cria/atualiza a linha em `users` + `user_profiles` e devolve o mesmo par de tokens + dados do usuário local (onboarding, plano).
4. As próximas chamadas à API usam `Authorization: Bearer <access_token do Supabase>`.

**Confirmação de email:** se o projeto Supabase exigir confirmação, após `signUp` pode não haver `session` até o usuário abrir o link do email (o app mostra um aviso).

Exemplo de URL:

```env
DATABASE_URL=jdbc:postgresql://db.SEUPROJETO.supabase.co:5432/postgres?sslmode=require
SUPABASE_URL=https://SEUPROJETO.supabase.co
SUPABASE_KEY=SUA_SUPABASE_PUBLISHABLE_KEY
```

## 1c) Backend Spring — propriedades Supabase

Além do JDBC (`DATABASE_*`), o backend expõe:

- `supabase.url` ← `SUPABASE_URL`
- `supabase.key` ← `SUPABASE_KEY` (publishable/anon, para chamadas que respeitam RLS)
- `supabase.service-role-key` ← `SUPABASE_SERVICE_ROLE_KEY` (opcional; **segredo de servidor**)

Injete `SupabaseProperties` quando for chamar a API REST do Supabase (PostgREST) a partir do Java.

## 1d) Se o backend cair com `Network unreachable` / `UnknownHostException`

Em algumas redes/containers, o host direto `db.<project-ref>.supabase.co` pode resolver apenas para IPv6.
Se isso acontecer, use a **Connection pooling URI (IPv4)** do painel Supabase:

- Host estilo `aws-0-<regiao>.pooler.supabase.com`
- Porta `6543`
- Usuário no formato indicado pelo Supabase (geralmente `postgres.<project-ref>`)

Exemplo:

```env
DATABASE_URL=jdbc:postgresql://aws-0-REGIAO.pooler.supabase.com:6543/postgres?sslmode=require
DATABASE_USER=postgres.SEUPROJECTREF
DATABASE_PASSWORD=SUA_SENHA
```

## 2) Subir com Compose (arquivo Supabase)

```bash
docker compose -f docker-compose.supabase.yml up --build
```

Esse comando:

- mantém `backend` e `ai-service`
- não usa `db` local
- faz o backend conectar no Supabase via variáveis do `.env`

## 3) Endpoints

- Backend: `http://localhost:8080/api`
- AI Service: `http://localhost:8000/health`
- Frontend: `@nuxtjs/supabase` com `SUPABASE_URL` + `SUPABASE_KEY` em `frontend/.env`

## 4) Se der erro de permissão no Docker

Use temporariamente:

```bash
sudo docker compose -f docker-compose.supabase.yml up --build
```

Depois ajuste definitivamente as permissões do daemon Docker.
