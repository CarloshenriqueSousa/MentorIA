# MentorIA

## Por que o “front” não aparece no `localhost:8080`?

- **8080** = só o **backend Java (API)** no Docker. Não existe interface HTML aí; é normal ver **404** em `http://localhost:8080/`.
- O **Nuxt (interface)** é **outro processo** e roda em **outra porta** (neste projeto: **3001**).

## Subir tudo no desenvolvimento

1. **API + banco + IA (Docker)** — na raiz do repositório:

   ```bash
   docker compose up -d --build
   ```

   - API: `http://localhost:8080/api` (health: `/api/actuator/health`)
   - IA: `http://localhost:8000/health`

2. **Frontend (Nuxt)** — no diretório `frontend/`:

   ```bash
   cd frontend
   pnpm install
   pnpm dev
   ```

   Abra **`http://localhost:3001`** (porta fixa no `nuxt.config` para não conflitar com Obsidian/outros na 3000).

3. **CORS** — no `.env` da raiz, inclua a origem do Nuxt, por exemplo:

   `CORS_ORIGINS=http://localhost:3000,http://localhost:3001`

   Depois recrie o backend: `docker compose up -d --force-recreate backend`.

O Nuxt carrega o **`.env` da raiz** automaticamente (e `frontend/.env` se existir), para `SUPABASE_*` e `NUXT_PUBLIC_*` baterem com o backend.

## Conta de teste (local)

Com o seed ativo (`app.seed.test-user.enabled` no `application.yml` do backend):

- **Email:** `teste@gmail.com`
- **Senha:** `teste123`
- **Papel:** `ADMIN` (acessa **`/admin`** no front após **login local** — card “Conta local” na tela de login).

Use **Login local** no Nuxt (não o Supabase) para essa conta. Em produção, defina `SPRING_PROFILES_ACTIVE=prod` ou `app.seed.test-user.enabled=false`.

## Painel admin

Após login local com o usuário de teste: **`http://localhost:3001/admin`** — visão dos três agentes (pesquisa, orquestração, planos) e `GET /api/admin/overview`.

## Documentação extra

- [SUPABASE_SETUP.md](./SUPABASE_SETUP.md) — Supabase remoto, JWT, etc.
- [docs/PROJECT_STANDARDS.md](./docs/PROJECT_STANDARDS.md) — padrões de código e arquitetura.
