from pydantic import BaseModel, Field
from typing import Optional, List, Dict, Any


class MessageHistory(BaseModel):
    role: str  # "user" | "assistant"
    content: str


class UserProfileContext(BaseModel):
    target_exam: Optional[str] = None
    objectives: Optional[List[str]] = None
    difficulties: Optional[List[str]] = None
    strengths: Optional[List[str]] = None
    knowledge_level: Optional[str] = "BEGINNER"
    study_hours_per_day: Optional[int] = 2


class ChatRequest(BaseModel):
    user_id: str
    message: str
    history: List[MessageHistory] = Field(default_factory=list)
    profile: Optional[UserProfileContext] = None


class ChatResponseSchema(BaseModel):
    content: str
    tokens_used: int
    model: str


class StudyPlanRequest(BaseModel):
    user_id: str
    profile: UserProfileContext


class StudyPlanResponse(BaseModel):
    title: str
    description: str
    plan_content: Dict[str, Any]
    subjects_covered: List[str]