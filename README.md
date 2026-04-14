# MentorIA - O seu mentor de estudos com Inteligência Artificial 🚀

Bem-vindo ao repositório oficial do **MentorIA**, uma plataforma SaaS completa focada em otimizar a preparação de estudantes para concursos públicos utilizando Inteligência Artificial de última geração.

## 🌟 Visão Geral

O MentorIA atua como um preceptor particular disponível 24/7. Ele não apenas responde a dúvidas, mas cria planos de estudo adaptativos, aplica técnicas de memorização espaçada e acompanha a evolução do aluno através de gamificação e metas diárias.

**Principais Tecnologias e Arquitetura:**
- **Frontend (Web):** Vue 3 + Nuxt 4, Tailwind CSS, Nuxt UI, Pinia, Markdown IT.
- **Backend (Core API):** Java 21 + Spring Boot 3, Spring Security, JPA/Hibernate.
- **AI Service (Microserviço):** Python 3.12 + FastAPI, integração direta com a API da Anthropic (Claude 3.5 Sonnet).
- **Banco de Dados:** PostgreSQL 16.
- **Autenticação:** Supabase Auth + JWT assinado pelo Backend Spring.
- **Pagamentos:** Integração completa com Stripe (Checkout e Customer Portal).
- **Infraestrutura:** Docker e Docker Compose nativos.

---

## 🏗️ Estrutura do Projeto (Microsserviços)

A aplicação foi desenhada visando alta escalabilidade, separando a inteligência artificial do motor de regras de negócio tradicionais:

```text
mentoria/
├── frontend/        # Interface do usuário (Nuxt/Vue)
├── backend/         # Core API em Java/Spring (Usuários, Planos, Pagamentos)
├── ai-service/      # Bot de IA e processamento LLM (FastAPI/Python)
└── docker-compose   # Orquestração do ambiente local
```

### 1. Frontend (`/frontend`)
O portal do aluno foi construído utilizando as melhores práticas modernas:
- **Design System Premium:** Nuxt UI + Tailwind CSS para uma experiência limpa (Glassmorphism, gradientes e micro-animações).
- **Responsivo e Dinâmico:** Interfaces state-of-the-art para onboarding, chat em tempo real e dashboard.
- **SSR e SEO:** Nuxt configurado para renderizar o _Landing Page_ com grande indexação orgânica.

### 2. Core API (`/backend`)
O coração do SaaS. Responsável por garantir segurança e integridade transacional:
- **Segurança Dupla:** O usuário autentica no Supabase, envia o token pro Backend que o valida e gera um JWT seguro de curto prazo.
- **Integração Stripe:** Webhooks estruturados para atualizar o status `PlanType` (FREE, BASIC, PREMIUM) dinamicamente.
- **Alta Performance:** Spring Data JPA otimizado com campos JSONB nativos no PostgreSQL.

### 3. AI Service (`/ai-service`)
Isolamos a IA num microsserviço Python para aproveitar o melhor ecossistema de LLMs:
- **FastAPI:** Extremamente rápido e otimizado para concorrência (I/O Bound).
- **Prompt Engineering System:** Injeção sistemática do perfil do aluno (nível de conhecimento, tempo de estudo, estilo de aprendizagem) no contexto do modelo Claude.
- **Geração de Markdown:** Produz aulas ricamente formatadas de forma autônoma.

---

## ⚡ Implementações SaaS Inclusas

Para garantir a maturidade de um produto "Pronto para o Mercado" (Production-Ready), construímos:

- ✅ **Autenticação robusta (B2C):** Login/Registro, _magic links_ e recuperação de senha.
- ✅ **Monetização Automatizada:** Gateway de pagamentos nativo (Stripe).
- ✅ **Soft Deletion:** Exclusão de contas obedecendo à LGPD (o dado é mascarado, mantendo a integridade do Analytics).
- ✅ **Multi-Planos e Feature Flags:** Bloqueio de funcionalidades com base na assinatura (rate-limiting no backend para contas 'FREE').
- ✅ **Configurações Dinâmicas:** Edição flexível de propriedades do aluno persistidas num banco híbrido (Colunas Relacionais + JSONB Auxiliar).

---

## 🚀 Como Executar Localmente

### Pré-requisitos
- Docker e Docker Compose instalados.
- Node.js (v20+) e PNPM.
- JDK 21+ e Maven (caso queira rodar o back-end fora do Docker).
- Contas/Chaves de API ativas: Supabase, Stripe, e Anthropic (Claude).

### 1. Configurar Variáveis de Ambiente
Copie o arquivo `.env.example` para `.env` na raiz do projeto e preencha as chaves reais:

```bash
cp .env.example .env
```

No diretório `frontend`, crie também o seu arquivo `.env`:
```text
NUXT_PUBLIC_API_BASE=http://localhost:8080/api
NUXT_PUBLIC_SUPABASE_URL=sua-url-aqui
NUXT_PUBLIC_SUPABASE_KEY=sua-key-anon-aqui
```

### 2. Iniciar Banco e Backend com Docker
Na pasta raiz, execute:
```bash
docker-compose up -d --build
```
Isso levantará:
- PostgreSQL (na porta `5432`)
- Spring Boot Backend (na porta `8080`)
- FastAPI AI Service (na porta `8000`)

### 3. Executar o Frontend (Modo Dev)
Como o desenvolvimento da UI costuma exigir _Hot-Reload_, rodamos o frontend separadamente:
```bash
cd frontend
pnpm install
pnpm run dev
```
Acesse: [http://localhost:3001](http://localhost:3001)

---

## 🗺️ Roadmap de Evolução Tecnológica (Próximos Passos)

1. **Streaming de IA (Server-Sent Events - SSE):** Implementar efeito "máquina de escrever" nas respostas da IA usando SSE no FastAPI sendo canalizado via Spring Boot para o Nuxt.
2. **Sistema de Cache (Redis):** Cache global de contexto de sessões de chat para economizar tokens do lado da LLM.
3. **Logs/Monitoramento Centralizados:** Instalação do stack ELK (Elasticsearch/Kibana) ou Grafana/Prometheus/Loki.
4. **Deploy Automático (CI/CD):** Actions do Github rodando linting, Testes Unitários e criando containers pro Google Cloud Run ou AWS ECS.
5. **Aplicação Mobile Nativa (PWA/Capacitor):** Gerar o bundle de iOS/Android utilizando o motor Nuxt.
