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
- `JWT_SECRET`
- `ANTHROPIC_API_KEY`
- (`STRIPE_*` pode ficar placeholder para desenvolvimento local)

Exemplo de URL:

```env
DATABASE_URL=jdbc:postgresql://db.SEUPROJETO.supabase.co:5432/postgres?sslmode=require
SUPABASE_URL=https://SEUPROJETO.supabase.co
SUPABASE_KEY=SUA_SUPABASE_PUBLISHABLE_KEY
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
- Frontend usa Supabase via `useSupabase()` com:
  - `NUXT_PUBLIC_SUPABASE_URL` (ou `SUPABASE_URL`)
  - `NUXT_PUBLIC_SUPABASE_KEY` (ou `SUPABASE_KEY`)

## 4) Se der erro de permissão no Docker

Use temporariamente:

```bash
sudo docker compose -f docker-compose.supabase.yml up --build
```

Depois ajuste definitivamente as permissões do daemon Docker.
