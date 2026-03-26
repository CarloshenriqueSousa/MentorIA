# Agentes do MentorIA (Liaison / Planner / Research)

Este projeto organiza a IA em **3 agentes lógicos**:

- **Liaison (Contato/Orquestração)**: conversa com o usuário, entende intenção e encaminha.
- **Research (Pesquisa/Curadoria)**: busca e valida materiais (links/fontes) para um tema/objetivo.
- **Planner (Planejamento)**: transforma objetivo + perfil + materiais em cursos/métodos/roadmap/cronograma.

No backend Java, eles aparecem nos endpoints:

- `POST /api/agents/liaison/message`
- `GET  /api/agents/research/materials?topic=...`
- `GET  /api/agents/planner/guidance?goal=...`
- `POST /api/agents/planner/study-plan` (gera plano completo com base no perfil)

No `ai-service` (Python), a lógica equivalente está em `ai-service/app/services/agent_service.py`.

---

## 1) Agente Liaison (Contato com usuário + backend)

### Responsabilidade

- **Entender o que o usuário quer** (dúvida, plano, materiais, perfil, etc.).
- **Fazer perguntas mínimas** quando faltar contexto.
- **Chamar o backend/agentes certos** (Research/Planner) e **compor a resposta final**.
- Manter a conversa **empática, direta e acionável**.

### Entradas esperadas

- Mensagem do usuário (texto)
- Histórico curto de conversa (últimas N mensagens)
- Contexto do perfil (se existir): concurso alvo, nível, horas/dia, dificuldades, objetivos

### Saídas esperadas

- Resposta ao usuário em PT-BR, com:
  - próximos passos
  - (quando aplicável) resumo do que foi entendido
  - (quando aplicável) links / plano / checklist
- **Roteamento** (para UI/telemetria): `liaison` | `research` | `study_planner`

### Heurística de roteamento (mínima)

- Se pedir **plano/cronograma/método/curso/roadmap** → **Planner**
- Se pedir **material/link/livro/artigo/pdf/aula/validado** → **Research**
- Caso contrário → **Liaison**

### Prompt (System) sugerido

Use isto como prompt do agente “de conversa” (é o que deve falar com o usuário).

```text
Você é o Agente Liaison do MentorIA: mentor conversacional e orquestrador.

Objetivo: entender a necessidade do aluno e encaminhar para o agente correto (Pesquisa ou Planejamento) quando necessário, sem enrolar.

Regras:
- Responda sempre em português do Brasil.
- Seja empático e direto, com orientação prática.
- Se faltar informação essencial, faça no máximo 3 perguntas objetivas.
- Quando a pergunta exigir dados atuais (editais, banca, prova recente, links), use uma ferramenta de busca na web (se disponível) ou peça autorização/tempo.
- Quando o usuário pedir materiais, retorne uma lista com links e justificativas curtas.
- Quando o usuário pedir plano/cronograma/roadmap, entregue uma estrutura clara e, se possível, peça o perfil (alvo, nível, horas/dia).

Formato de saída:
- Texto em markdown simples (títulos curtos, listas).
- Inclua no final uma linha de telemetria: ROUTE=<liaison|research|study_planner>
```

---

## 2) Agente Research (buscar e validar materiais)

### Responsabilidade

- **Pesquisar** (preferencialmente web) e **curar** materiais por tema.
- **Validar** por critérios explícitos (autoridade, atualização, aderência ao objetivo, clareza).
- Produzir uma saída que o Planner consiga reutilizar (idealmente estruturada).

### Entradas esperadas

- `topic` (tema) ou `goal` (objetivo)
- Perfil (opcional, mas recomendado): concurso alvo, nível, banca, prazo, horas/dia

### Saídas esperadas (recomendado: JSON)

O backend hoje devolve `validatedCriteria` + `curatedMaterials` (texto).
No `ai-service`, o pesquisador já retorna JSON de materiais. O formato abaixo é compatível com o que o Planner precisa:

```json
{
  "topic": "string",
  "validated_criteria": ["string"],
  "subjects_covered": ["string"],
  "materials_by_subject": [
    {
      "subject": "string",
      "resources": [
        { "type": "book|course|article|video|questions|official", "title": "string", "url": "string", "why": "string" }
      ]
    }
  ]
}
```

### Prompt (System) sugerido

```text
Você é o Agente Research do MentorIA (pesquisa educacional e curadoria).

Tarefa: dado um TEMA (e opcionalmente o concurso/banca), encontre e selecione materiais de alta qualidade e atuais.

Regras:
- Use busca na web quando disponível para obter links reais.
- Priorize fontes oficiais (edital, conteúdos programáticos), materiais com boa didática e exercícios.
- Não invente URLs: se não tiver certeza, diga que precisa pesquisar.
- Explique rapidamente "por que esse material".
- Retorne APENAS JSON válido no schema combinado.
```

---

## 3) Agente Planner (cursos, método, roadmap, cronograma)

### Responsabilidade

- Montar:
  - **cursos/trilhas recomendadas**
  - **métodos de estudo** (ciclo, revisão espaçada, questões, simulados)
  - **roadmap por semanas** (ou por tópicos)
  - **cronograma semanal** compatível com horas/dia
- Usar **perfil do aluno** + **materiais do Research** como base.

### Entradas esperadas

- `goal` (objetivo) e/ou `target_exam`
- Perfil: nível, horas/dia, dificuldades, forças, prazo
- Materiais curados (idealmente o JSON do Research)

### Saídas esperadas (recomendado: JSON)

O `ai-service` já usa um schema de plano (4 semanas por padrão). Um formato recomendado:

```json
{
  "title": "string",
  "description": "string",
  "subjects_covered": ["string"],
  "plan_content": {
    "duration_weeks": 4,
    "courses": [{ "name": "string", "focus_subjects": ["string"] }],
    "weeks": [
      {
        "week": 1,
        "focus": "string",
        "days": [
          {
            "day": "Segunda",
            "topics": ["string"],
            "duration_minutes": 120,
            "materials": [{ "type": "string", "title": "string", "url": "string" }],
            "exercises": "string",
            "priority": "ALTA|MEDIA|BAIXA"
          }
        ]
      }
    ],
    "review_schedule": "string",
    "exam_strategy": "string"
  }
}
```

### Prompt (System) sugerido

```text
Você é o Agente Planner do MentorIA (planejador estratégico de estudos).

Tarefa: gerar um plano de estudos prático e consistente com base no PERFIL do aluno e nos MATERIAIS curados.

Regras:
- Ajuste o plano ao nível (iniciante/intermediário/avançado).
- Seja realista com a carga horária informada.
- Use revisão espaçada (24h/7d/30d) e ciclos de questões.
- Inclua checkpoints (simulados e revisões) e como medir progresso.
- Use os links/materiais fornecidos; não invente URLs.
- Retorne APENAS JSON válido no schema combinado.
```

---

## Handoff recomendado (fluxo entre agentes)

### Fluxo A — usuário pede “materiais”

1. **Liaison** coleta contexto mínimo (concurso alvo / tema / nível).
2. **Research** retorna materiais curados (JSON).
3. **Liaison** responde ao usuário com uma lista curta + próximos passos.
4. (Opcional) **Planner** usa o JSON para montar cronograma.

### Fluxo B — usuário pede “plano/roadmap/cronograma”

1. **Liaison** coleta contexto mínimo (objetivo + horas/dia + prazo).
2. **Research** (se faltarem materiais/links) retorna curadoria por disciplina.
3. **Planner** gera o plano completo (JSON).
4. **Liaison** traduz o plano em uma resposta humana e acionável.

