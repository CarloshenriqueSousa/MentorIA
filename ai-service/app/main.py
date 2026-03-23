import os
import logging
from contextlib import asynccontextmanager

from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from dotenv import load_dotenv

from app.routers import chat, study_plan

load_dotenv()

logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s [%(levelname)s] %(name)s: %(message)s"
)
logger = logging.getLogger(__name__)


@asynccontextmanager
async def lifespan(app: FastAPI):
    logger.info("🤖 MentorIA AI Service iniciando...")
    logger.info(f"Modelo: {os.getenv('CLAUDE_MODEL', 'claude-3-5-sonnet-latest')}")
    yield
    logger.info("AI Service encerrando")


app = FastAPI(
    title="MentorIA AI Service",
    description="Serviço de IA para mentor personalizado",
    version="1.0.0",
    lifespan=lifespan
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=[os.getenv("ALLOWED_ORIGINS", "http://localhost:8080")],
    allow_credentials=True,
    allow_methods=["POST"],
    allow_headers=["Content-Type", "Authorization"],
)


@app.get("/health")
async def health():
    return {
        "status": "healthy",
        "service": "mentoria-ai",
        "model": os.getenv("CLAUDE_MODEL", "claude-3-5-sonnet-latest")
    }


app.include_router(chat.router, tags=["Chat"])
app.include_router(study_plan.router, tags=["Study Plan"])