import os
import json
import logging
from typing import Optional, List
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
    Inclui múltiplos Agentes: Planejador, Mentor de Contato e Pesquisador Web.
    """

    def __init__(self):
        self.api_key = os.getenv("ANTHROPIC_API_KEY")
        self.model = os.getenv("CLAUDE_MODEL", "claude-3-5-sonnet-latest")

        # LLM Base
        self.llm = ChatAnthropic(
            model_name=self.model,
            api_key=self.api_key,
            max_tokens=4096,
            temperature=0.7,
        )

        # Web Researcher Tool
        self.search_tool = DuckDuckGoSearchRun()
        
        self.tools = [
            Tool(
                name="web_search",
                func=self.search_tool.run,
                description="Use para pesquisar na internet sobre concursos, editais, o que mais cai nas provas, ou materiais gratuitos em PDF. Insira a query de busca completa."
            )
        ]

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

    @retry(stop=stop_after_attempt(3), wait=wait_exponential(multiplier=1, min=2, max=10))
    async def generate_chat_response(
        self,
        user_message: str,
        history: list[MessageHistory],
        profile: Optional[UserProfileContext]
    ) -> dict:
        
        system_prompt = self.build_system_prompt(profile)
        
        prompt = ChatPromptTemplate.from_messages([
            ("system", system_prompt),
            MessagesPlaceholder(variable_name="chat_history"),
            ("human", "{input}"),
            MessagesPlaceholder(variable_name="agent_scratchpad"),
        ])
        
        agent = create_tool_calling_agent(self.llm, self.tools, prompt)
        agent_executor = AgentExecutor(agent=agent, tools=self.tools, verbose=True)
        
        # Build history for execution
        langchain_history = []
        for msg in history[-20:]: # Últimas 20 msgs
            if msg.role == "user":
                langchain_history.append(HumanMessage(content=msg.content))
            elif msg.role == "assistant":
                langchain_history.append(AIMessage(content=msg.content))

        response = await agent_executor.ainvoke({
            "input": user_message,
            "chat_history": langchain_history
        })
        
        # Anthropic Tool Calling Agent uses tokens
        # However AgentExecutor doesn't bubble usage_metadata easily in ainvoke, we will mock or approximate
        return {
            "content": response.get("output", ""),
            "tokens_used": 0,
            "model": self.model
        }

    @retry(stop=stop_after_attempt(3), wait=wait_exponential(multiplier=1, min=2, max=10))
    async def generate_study_plan(self, profile: UserProfileContext) -> dict:
        
        # 1. Pesquisa prévia (Web Researcher Agent behavior) para embasar o cronograma
        logger.info(f"Iniciando pesquisa prévia de matérias para o concurso: {profile.target_exam}")
        search_query = f"O que mais cai na prova do concurso {profile.target_exam or 'geral'} disciplinas matérias edital"
        try:
            search_context = self.search_tool.run(search_query)
        except Exception as e:
            search_context = "Não foi possível buscar na internet. Basear-se no treinamento padrão."
            
        system_prompt = (
            "Você é o Agente Planejador Estratégico. Seu dever é gerar um cronograma de estudos tático e detalhado.\n"
            "Use o Contexto da Web Pesquisada abaixo para definir as matérias e pesos do cronograma.\n"
            "Gere SEMPRE um JSON válido exato, sem markdown extra ao redor se possível, ou dentro de ```json.\n"
            f"=== CONTEXTO DA WEB PARA O EDITAL ===\n{search_context}\n====================="
        )

        weeks = 12 if (profile.study_hours_per_day or 2) < 4 else 8

        user_prompt = f"""
Crie um plano de estudos focado p/ o aluno:
- Alvo: {profile.target_exam or 'Geral'}
- Nível: {profile.knowledge_level or 'BEGINNER'}
- Horas/dia: {profile.study_hours_per_day or 2}h
- Dificuldades: {', '.join(profile.difficulties or [])}

Retorne APENAS um JSON:
{{
  "title": "Plano Tático - [Certame]",
  "description": "Explique brevemente por que você organizou as matérias dessa forma baseado na pesquisa web.",
  "subjects_covered": ["Matéria A (Pesada)", "Matéria B (Leve)"],
  "plan_content": {{
    "duration_weeks": {weeks},
    "weeks": [
      {{
        "week": 1,
        "focus": "Tema central baseado no edital",
        "days": [
          {{
            "day": "Segunda",
            "topics": ["Tópico específico X", "Tópico Y"],
            "duration_minutes": {(profile.study_hours_per_day or 2) * 60},
            "materials": ["Sugira um tipo de material"],
            "exercises": "Fazer 10 questões da banca",
            "priority": "alta"
          }}
        ]
      }}
    ],
    "review_schedule": "Padrão de revisão",
    "exam_strategy": "Visão geral estratégica da prova"
  }}
}}
Gere 4 semanas de amostra. NÃO retorne nada além do JSON válido.
"""
        plan_llm = ChatAnthropic(
            model_name=self.model,
            api_key=self.api_key,
            max_tokens=4096,
            temperature=0.2, # Mais determinístico p/ JSON
        )

        response = await plan_llm.ainvoke([
            SystemMessage(content=system_prompt),
            HumanMessage(content=user_prompt)
        ])

        content = response.content.strip()

        # Limpar o JSON com Regex robusto
        import re
        fence_match = re.search(r'```(?:json)?\s*\n?(.*?)```', content, re.DOTALL)
        if fence_match:
            content = fence_match.group(1)

        try:
            return json.loads(content.strip())
        except json.JSONDecodeError as decode_err:
            logger.error(f"Erro no Parse JSON do Planejador: {content}")
            raise ValueError("O LLM não retornou um JSON válido") from decode_err
