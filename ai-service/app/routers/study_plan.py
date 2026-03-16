from fastapi import APIRouter, HTTPException
from app.models.schemas import StudyPlanRequest, StudyPlanResponse
from app.services.grok_service import GrokService
import logging

router = APIRouter()
logger = logging.getLogger(__name__)
grok_service = GrokService()


@router.post("/study-plan", response_model=StudyPlanResponse)
async def generate_study_plan(request: StudyPlanRequest):
    try:
        plan = await grok_service.generate_study_plan(request.profile)
        return StudyPlanResponse(
            title=plan.get("title", f"Plano - {request.profile.target_exam}"),
            description=plan.get("description", ""),
            plan_content=plan.get("plan_content", {}),
            subjects_covered=plan.get("subjects_covered", [])
        )

    except ValueError as e:
        logger.error(f"Erro de JSON para user {request.user_id}: {e}")
        raise HTTPException(status_code=422, detail="Erro ao processar plano. Tente novamente.")

    except Exception as e:
        logger.error(f"Erro ao gerar plano para user {request.user_id}: {e}")
        raise HTTPException(status_code=503, detail="Erro ao gerar plano de estudos")