FROM python:3.12-slim

WORKDIR /app

RUN apt-get update && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*

# Dependências primeiro (melhor cache de layers)
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# Código-fonte
COPY app/ ./app/

# Non-root user
RUN addgroup --gid 1001 --system mentoria && \
    adduser --uid 1001 --system --ingroup mentoria mentoria
USER mentoria

ENV PYTHONUNBUFFERED=1
ENV HOST=0.0.0.0
ENV PORT=8000

EXPOSE 8000

HEALTHCHECK --interval=30s --timeout=10s --start-period=5s --retries=3 \
    CMD curl -f http://localhost:8000/health || exit 1

CMD ["uvicorn", "app.main:app", "--host", "0.0.0.0", "--port", "8000", "--workers", "2"]
