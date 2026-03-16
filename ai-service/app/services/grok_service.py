import os
import json
import logging
from typing import Optional
from tenacity import retry, stop_after_attempt, wait_exponential

from langchain_openai import ChatOpenAI
from langchain_core.messages import HumanMessage, AIMessage, SystemMessage

from app.models.schemas import UserProfileContext, MessageHistory

logger = logging.getLogger(__name__)


class GrokService:
    """
    Integração com Grok API (xAI) via LangChain.
    Compatível com o cliente OpenAI.
    """

    def __init__(self):
        self.api_key = os.getenv("GROK_API_KEY")
        self.base_url = os.getenv("GROK_BASE_URL", "https://api.x.ai/v1")
        self.model = os.getenv("GROK_MODEL", "grok-3-mini-fast")

        self.llm = ChatOpenAI(
            model=self.model,
            api_key=self.api_key,
            base_url=self.base_url,
            max_tokens=2048,
            temperature=0.7,
        )

    def build_system_prompt(self, profile: Optional[UserProfileContext]) -> str:
        base = (
            "Você é o MentorIA, um professor particular especializado em concursos públicos brasileiros. "
            "Você é empático, direto, encorajador e extremamente didático. "
            "Suas respostas são claras, organizadas e adaptadas ao nível do aluno. "
            "Use exemplos práticos e sempre mostre o caminho de forma estruturada. "
            "Responda sempre em português brasileiro. "
            "Use markdown quando útil (listas, negrito, cabeçalhos).\n\n"
        )

        if not profile:
            return base + "O aluno ainda não completou o perfil. Seja acolhedor."

        parts = []

        if profile.target_exam:
            parts.append(f"🎯 Concurso alvo: {profile.target_exam}")

        if profile.knowledge_level:
            levels = {
                "BEGINNER": "iniciante",
                "INTERMEDIATE": "intermediário",
                "ADVANCED": "avançado"
            }
            parts.append(f"📊 Nível: {levels.get(profile.knowledge_level, profile.knowledge_level)}")

        if profile.objectives:
            parts.append(f"✅ Objetivos: {', '.join(profile.objectives)}")

        if profile.difficulties:
            parts.append(f"⚠️ Dificuldades: {', '.join(profile.difficulties)}")

        if profile.strengths:
            parts.append(f"💪 Pontos fortes: {', '.join(profile.strengths)}")

        if profile.study_hours_per_day:
            parts.append(f"⏰ Disponibilidade: {profile.study_hours_per_day}h/dia")

        if parts:
            base += "Perfil do aluno:\n" + "\n".join(parts) + "\n\n"
            base += (
                "Use essas informações para PERSONALIZAR cada resposta. "
                "Foque nas dificuldades, reforce os pontos fortes e "
                "sempre direcione para o concurso alvo."
            )

        return base

    @retry(
        stop=stop_after_attempt(3),
        wait=wait_exponential(multiplier=1, min=2, max=10)
    )
    async def generate_chat_response(
        self,
        user_message: str,
        history: list[MessageHistory],
        profile: Optional[UserProfileContext]
    ) -> dict:
        system_prompt = self.build_system_prompt(profile)
        messages = [SystemMessage(content=system_prompt)]

        for msg in history[-20:]:
            if msg.role == "user":
                messages.append(HumanMessage(content=msg.content))
            elif msg.role == "assistant":
                messages.append(AIMessage(content=msg.content))

        messages.append(HumanMessage(content=user_message))

        response = await self.llm.ainvoke(messages)

        tokens_used = 0
        if hasattr(response, "usage_metadata") and response.usage_metadata:
            tokens_used = response.usage_metadata.get("total_tokens", 0)

        return {
            "content": response.content,
            "tokens_used": tokens_used,
            "model": self.model
        }

    @retry(
        stop=stop_after_attempt(3),
        wait=wait_exponential(multiplier=1, min=2, max=10)
    )
    async def generate_study_plan(self, profile: UserProfileContext) -> dict:
        system_prompt = (
            "Você é um especialista em planejamento de estudos para concursos públicos brasileiros. "
            "Gere SEMPRE um JSON válido no formato especificado. "
            "Não adicione texto fora do JSON."
        )

        weeks = 12 if (profile.study_hours_per_day or 2) < 4 else 8

        user_prompt = f"""
Crie um plano de estudos para o aluno:

- Concurso alvo: {profile.target_exam or 'Concurso público geral'}
- Nível: {profile.knowledge_level or 'BEGINNER'}
- Horas por dia: {profile.study_hours_per_day or 2}h
- Objetivos: {', '.join(profile.objectives or ['Aprovação'])}
- Dificuldades: {', '.join(profile.difficulties or [])}
- Pontos fortes: {', '.join(profile.strengths or [])}
- Duração: {weeks} semanas

Retorne APENAS um JSON com esta estrutura:
{{
  "title": "Plano de Estudos - [Concurso]",
  "description": "Descrição motivacional (2-3 frases)",
  "subjects_covered": ["Matéria 1", "Matéria 2"],
  "plan_content": {{
    "duration_weeks": {weeks},
    "weeks": [
      {{
        "week": 1,
        "focus": "Tema principal",
        "days": [
          {{
            "day": "Segunda",
            "topics": ["Tópico 1", "Tópico 2"],
            "duration_minutes": {(profile.study_hours_per_day or 2) * 60},
            "materials": ["Recurso 1"],
            "exercises": "Descrição dos exercícios",
            "priority": "alta"
          }}
        ]
      }}
    ],
    "review_schedule": "Estratégia de revisão",
    "exam_strategy": "Dicas para a prova"
  }}
}}

Gere as primeiras 4 semanas detalhadas.
"""

        plan_llm = ChatOpenAI(
            model=self.model,
            api_key=self.api_key,
            base_url=self.base_url,
            max_tokens=4096,
            temperature=0.3,
        )

        response = await plan_llm.ainvoke([
            SystemMessage(content=system_prompt),
            HumanMessage(content=user_prompt)
        ])

        content = response.content.strip()

        # Remover ```json ``` se existir
        if content.startswith("```"):
            content = content.split("```")[1]
            if content.startswith("json"):
                content = content[4:]

        return json.loads(content.strip())