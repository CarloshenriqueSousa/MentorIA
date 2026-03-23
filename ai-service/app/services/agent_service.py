import os
import json
import logging
import re
from typing import Optional, List, Dict, Any
from tenacity import retry, stop_after_attempt, wait_exponential

from langchain_anthropic import ChatAnthropic
from langchain_core.messages import HumanMessage, AIMessage, SystemMessage
from langchain_community.tools import DuckDuckGoSearchRun
from langchain.agents import AgentExecutor, create_tool_calling_agent
from langchain_core.prompts import ChatPromptTemplate, MessagesPlaceholder
from langchain_core.tools import Tool

from app.models.schemas import UserProfileContext, MessageHistory

logger = logging.getLogger(__name__)

class AgentService:
    """
    Integração com Claude API (Anthropic) via LangChain.

    Esta classe organiza 3 agentes lógicos (todos usando o Claude via LangChain):
    1) Agente de Contato (mentor em conversa)
    2) Agente de Materiais (pesquisa e seleção de recursos)
    3) Agente Planejador (monta cronogramas/curso com base nos materiais)
    """

    def __init__(self):
        self.api_key = (os.getenv("ANTHROPIC_API_KEY") or "").strip()
        self.model = os.getenv("CLAUDE_MODEL", "claude-3-5-sonnet-latest")

        # IMPORTANTE: não instanciar o ChatAnthropic se a key não existir,
        # para não derrubar o container durante o startup.
        self.llm_available = bool(self.api_key)

        # Web Researcher Tool
        self.search_tool = DuckDuckGoSearchRun()
        
        self.tools = [
            Tool(
                name="web_search",
                func=self.search_tool.run,
                description="Use para pesquisar na internet sobre concursos, editais, o que mais cai nas provas, ou materiais gratuitos em PDF. Insira a query de busca completa."
            )
        ]

    def _get_llm(self, *, temperature: float) -> ChatAnthropic:
        if not self.llm_available:
            raise RuntimeError("ANTHROPIC_API_KEY não configurada. Não foi possível iniciar o Claude.")

        return ChatAnthropic(
            model_name=self.model,
            api_key=self.api_key,
            max_tokens=4096,
            temperature=temperature,
        )

    @staticmethod
    def _extract_json(content: str) -> dict:
        # Aceita JSON puro ou JSON dentro de ```json ... ```
        fence_match = re.search(r'```(?:json)?\s*\n?(.*?)```', content, re.DOTALL)
        if fence_match:
            content = fence_match.group(1)

        return json.loads(content.strip())

    def build_system_prompt(self, profile: Optional[UserProfileContext]) -> str:
        base = (
            "Você é o MentorIA v2, um professor particular, pesquisador e mentor focado em provas e concursos do Brasil.\n"
            "Sua comunicação é extremamente empática, encorajadora, direta e didática.\n"
            "MUITO IMPORTANTE: Como Mentor, você TEM ACESSO a uma ferramenta de Pesquisa na Internet (web_search).\n"
            "SEMPRE que o aluno perguntar algo que exige dados ATUAIS (ex: 'o que cai no edital X', 'materiais de estudo para Y', 'última prova do Z'), "
            "USE a ferramenta de pesquisa para embasar sua resposta com links e dados reais.\n"
            "Responda sempre em português do Brasil e use markdown rico (listas, bold, emojis).\n\n"
        )

        if not profile:
            return base + "O aluno ainda não completou o perfil. Convide-o a completar ou tire suas dúvidas gerais."

        parts = []
        if profile.target_exam: parts.append(f"🎯 Concurso alvo: {profile.target_exam}")
        if profile.knowledge_level: parts.append(f"📊 Nível: {profile.knowledge_level}")
        if profile.objectives: parts.append(f"✅ Objetivos: {', '.join(profile.objectives)}")
        if profile.difficulties: parts.append(f"⚠️ Dificuldades: {', '.join(profile.difficulties)}")
        if profile.strengths: parts.append(f"💪 Pontos fortes: {', '.join(profile.strengths)}")
        if profile.study_hours_per_day: parts.append(f"⏰ Disponibilidade: {profile.study_hours_per_day}h/dia")

        if parts:
            base += "Contexto do Aluno Atual:\n" + "\n".join(parts) + "\n\n"
            base += "Lembre-se SEMPRE dessas características ao desenhar respostas ou sugerir assuntos."

        return base

    def _format_chat_history(self, history: list[MessageHistory]) -> list:
        # LangChain espera objetos Message (HumanMessage/AIMessage).
        langchain_history: list = []
        for msg in history[-20:]:  # Últimas 20 msgs
            if msg.role == "user":
                langchain_history.append(HumanMessage(content=msg.content))
            elif msg.role == "assistant":
                langchain_history.append(AIMessage(content=msg.content))
        return langchain_history

    async def _contact_agent_reply_with_tools(
        self,
        user_message: str,
        history: list[MessageHistory],
        profile: Optional[UserProfileContext],
    ) -> dict:
        system_prompt = self.build_system_prompt(profile)
        prompt = ChatPromptTemplate.from_messages([
            ("system", system_prompt),
            MessagesPlaceholder(variable_name="chat_history"),
            ("human", "{input}"),
            MessagesPlaceholder(variable_name="agent_scratchpad"),
        ])

        llm = self._get_llm(temperature=0.7)
        agent = create_tool_calling_agent(llm, self.tools, prompt)
        agent_executor = AgentExecutor(agent=agent, tools=self.tools, verbose=True)

        langchain_history = self._format_chat_history(history)
        response = await agent_executor.ainvoke({
            "input": user_message,
            "chat_history": langchain_history,
        })

        return {
            "content": response.get("output", ""),
            "tokens_used": 0,
            "model": self.model,
        }

    async def _materials_agent(self, profile: UserProfileContext) -> Dict[str, Any]:
        target = profile.target_exam or "Geral"
        hours_per_day = profile.study_hours_per_day or 2

        # Contexto de web (não dependemos do tool-calling agent aqui para reduzir variabilidade).
        queries = [
            f"materiais de estudo concurso {target} PDF apostila editais",
            f"conteúdo programático concurso {target} matérias principais",
            f"questões comentadas concurso {target} banca gabarito"
        ]

        search_context_parts: list[str] = []
        for q in queries:
            try:
                search_context_parts.append(self.search_tool.run(q))
            except Exception:
                # Sem quebrar: materiais ainda podem ser gerados com contexto parcial.
                continue

        search_context = "\n\n".join(search_context_parts).strip() or (
            "Não foi possível buscar materiais com segurança agora. Use conhecimento geral e proponha fontes comuns."
        )

        system_prompt = (
            "Você é o Agente Pesquisador de Materiais de Estudo.\n"
            "Você receberá um CONTEXTO com trechos e links de uma busca na internet.\n"
            "Sua tarefa é organizar e selecionar materiais úteis (ex: edital/conteúdo, PDF/apostila, caderno de exercícios/questões, sites de estudo) para o aluno.\n"
            "RETORNE APENAS um JSON válido, sem markdown fora do JSON.\n\n"
            "Formato esperado:\n"
            "{\n"
            '  "target_exam": string,\n'
            '  "subjects_covered": string[],\n'
            '  "materials_by_subject": [\n'
            "    {\n"
            '      "subject": string,\n'
            '      "resources": [\n'
            "        {\n"
            '          "type": string,\n'
            '          "title": string,\n'
            '          "url": string\n'
            "        }\n"
            "      ]\n"
            "    }\n"
            "  ],\n"
            '  "recommended_course_models": [string]\n'
            "}\n"
        )

        user_prompt = (
            f"Perfil do aluno:\n"
            f"- Alvo: {target}\n"
            f"- Nível: {profile.knowledge_level or 'BEGINNER'}\n"
            f"- Horas/dia: {hours_per_day}h\n"
            f"- Objetivos: {', '.join(profile.objectives or [])}\n"
            f"- Dificuldades: {', '.join(profile.difficulties or [])}\n\n"
            "CONTEXT:\n"
            f"{search_context}\n"
            "\nAgora produza o JSON no formato exato."
        )

        llm = self._get_llm(temperature=0.2)
        response = await llm.ainvoke([
            SystemMessage(content=system_prompt),
            HumanMessage(content=user_prompt),
        ])

        try:
            return self._extract_json(response.content or "")
        except Exception as exc:
            logger.error("Erro ao parsear JSON do Agente de Materiais: %s", exc)
            raise ValueError("O LLM não retornou um JSON válido para materiais") from exc

    async def _planner_agent(
        self,
        profile: UserProfileContext,
        materials: Dict[str, Any],
    ) -> Dict[str, Any]:
        target = profile.target_exam or "Geral"
        hours_per_day = profile.study_hours_per_day or 2

        # Para manter o payload leve e estável com a UI atual, geramos 4 semanas.
        duration_weeks = 4
        subjects = materials.get("subjects_covered") or []
        if not subjects:
            subjects = ["Matéria A", "Matéria B"]

        system_prompt = (
            "Você é o Agente Planejador Estratégico.\n"
            "Seu dever é gerar um cronograma de estudos tático e detalhado com base no PERFIL e nos MATERIAIS.\n"
            "RETORNE APENAS um JSON válido (sem markdown fora do JSON).\n\n"
            "O JSON final precisa ter exatamente as chaves:\n"
            "{\n"
            '  "title": string,\n'
            '  "description": string,\n'
            '  "subjects_covered": string[],\n'
            '  "plan_content": {\n'
            '     "duration_weeks": number,\n'
            '     "courses": [ { "name": string, "focus_subjects": string[] } ],\n'
            '     "weeks": [\n'
            "        {\n"
            '          "week": number,\n'
            '          "focus": string,\n'
            '          "days": [\n'
            "            {\n"
            '              "day": string,\n'
            '              "topics": string[],\n'
            '              "duration_minutes": number,\n'
            '              "materials": [ { "type": string, "title": string, "url": string } ],\n'
            '              "exercises": string,\n'
            '              "priority": string\n'
            "            }\n"
            "          ]\n"
            "        }\n"
            "     ],\n"
            '     "review_schedule": string,\n'
            '     "exam_strategy": string\n'
            "  }\n"
            "}\n"
        )

        user_prompt = (
            f"Perfil do aluno:\n"
            f"- Alvo: {target}\n"
            f"- Nível: {profile.knowledge_level or 'BEGINNER'}\n"
            f"- Horas/dia: {hours_per_day}h\n"
            f"- Objetivos: {', '.join(profile.objectives or [])}\n"
            f"- Dificuldades: {', '.join(profile.difficulties or [])}\n"
            f"- Forças: {', '.join(profile.strengths or [])}\n\n"
            "MATERIAIS (JSON):\n"
            f"{json.dumps(materials, ensure_ascii=False)}\n\n"
            f"Gere um cronograma para {duration_weeks} semanas (4 semanas):\n"
            "- Para cada semana, gere 5 dias (Segunda a Sexta)\n"
            "- Duração por dia: {hours_per_day * 60} minutos\n"
            f"- Use os materiais (type/title/url) sugeridos em MATERIAIS\n"
            "- Ajuste a complexidade para o nível do aluno\n"
            "- subjects_covered deve refletir os principais assuntos/disciplinas\n"
            "NÃO inclua texto extra fora do JSON."
        )

        llm = self._get_llm(temperature=0.25)
        response = await llm.ainvoke([
            SystemMessage(content=system_prompt),
            HumanMessage(content=user_prompt),
        ])

        try:
            parsed = self._extract_json(response.content or "")
            # Garantia mínima pra não quebrar a UI:
            parsed.setdefault("subjects_covered", subjects)
            parsed.setdefault("plan_content", {})
            parsed["plan_content"].setdefault("duration_weeks", duration_weeks)
            parsed["plan_content"].setdefault("weeks", [])
            return parsed
        except Exception as exc:
            logger.error("Erro ao parsear JSON do Agente Planejador: %s", exc)
            raise ValueError("O LLM não retornou um JSON válido para plano") from exc

    @retry(stop=stop_after_attempt(3), wait=wait_exponential(multiplier=1, min=2, max=10))
    async def generate_chat_response(
        self,
        user_message: str,
        history: list[MessageHistory],
        profile: Optional[UserProfileContext]
    ) -> dict:
        # Heurística simples: se o usuário pedir materiais, usamos o Agente de Materiais
        # e depois formatamos a resposta no Agente de Contato.
        user_lower = (user_message or "").lower()
        wants_materials = any(
            k in user_lower
            for k in ["material", "apostila", "pdf", "link", "questão", "questoes", "bibliografia", "referência", "referencia"]
        )

        if wants_materials and profile and profile.target_exam:
            materials = await self._materials_agent(profile)

            system_prompt = (
                "Você é o Agente de Contato (MentorIA) e vai responder de forma extremamente empática e didática.\n"
                "Quando eu pedir materiais, você deve listar os recursos com links.\n"
                "Responda em português do Brasil, com markdown rico (listas e links), e mantenha o foco no concurso alvo.\n"
            )

            reply_llm = self._get_llm(temperature=0.4)
            reply = await reply_llm.ainvoke([
                SystemMessage(content=system_prompt),
                HumanMessage(content=(
                    f"Pedido do aluno: {user_message}\n\n"
                    f"MATERIAIS (JSON): {json.dumps(materials, ensure_ascii=False)}\n"
                    "Escreva uma resposta curta com 1) quais assuntos ele deve focar, 2) uma lista de materiais com links, 3) um próximo passo prático."
                )),
            ])

            return {
                "content": reply.content or "",
                "tokens_used": 0,
                "model": self.model,
            }

        # Caso padrão: conversa normal (com tool-calling para dados atuais).
        return await self._contact_agent_reply_with_tools(
            user_message=user_message,
            history=history,
            profile=profile,
        )

    @retry(stop=stop_after_attempt(3), wait=wait_exponential(multiplier=1, min=2, max=10))
    async def generate_study_plan(self, profile: UserProfileContext) -> dict:
        logger.info("Agente de Materiais iniciando para: %s", profile.target_exam or "Geral")
        materials = await self._materials_agent(profile)

        logger.info("Agente Planejador iniciando (cronograma) para: %s", profile.target_exam or "Geral")
        return await self._planner_agent(profile=profile, materials=materials)
