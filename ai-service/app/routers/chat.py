from fastapi import APIRouter, HTTPException
from app.models.schemas import ChatRequest, ChatResponseSchema
from app.services.agent_service import AgentService
import logging

router = APIRouter()
logger = logging.getLogger(__name__)
agent_service = AgentService()


@router.post("/generate", response_model=ChatResponseSchema)
async def generate_response(request: ChatRequest):
    try:
        result = await agent_service.generate_chat_response(
            user_message=request.message,
            history=request.history,
            profile=request.profile
        )
        return ChatResponseSchema(**result)

    except Exception as e:
        logger.error(f"Erro ao gerar resposta para user {request.user_id}: {e}")
        raise HTTPException(status_code=503, detail="Serviço de IA temporariamente indisponível")