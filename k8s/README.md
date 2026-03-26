# MentorIA — Kubernetes Deployment

## Pré-requisitos

- Cluster Kubernetes (minikube, GKE, EKS, AKS, DigitalOcean, etc.)
- `kubectl` configurado
- Ingress Controller (nginx) instalado
- Container registry (Docker Hub, GCR, ECR, etc.)

## 1. Build e push das imagens

```bash
# Backend (Spring Boot)
docker build -t <REGISTRY>/mentoria-backend:latest ./backend
docker push <REGISTRY>/mentoria-backend:latest

# AI Service (Python/FastAPI)
docker build -t <REGISTRY>/mentoria-ai:latest -f ./ai-service/app/dockerfile ./ai-service
docker push <REGISTRY>/mentoria-ai:latest

# Frontend (Nuxt.js)
docker build -t <REGISTRY>/mentoria-frontend:latest ./frontend
docker push <REGISTRY>/mentoria-frontend:latest
```

> Substitua `<REGISTRY>` pelo seu registry (ex: `ghcr.io/seuuser`, `docker.io/seuuser`).

## 2. Configurar secrets

Edite `k8s/secrets.yaml` e substitua cada `REPLACE_WITH_BASE64` pelo valor real codificado em base64:

```bash
echo -n 'seu-jwt-secret' | base64
# Copie a saída e cole no secrets.yaml
```

> ⚠️ **NUNCA** faça commit de secrets reais. Use [Sealed Secrets](https://github.com/bitnami-labs/sealed-secrets), SOPS ou um vault em produção.

## 3. Atualizar imagens nos manifests

Edite os seguintes arquivos e substitua as imagens:
- `k8s/backend.yaml` → `image: <REGISTRY>/mentoria-backend:latest`
- `k8s/ai-service.yaml` → `image: <REGISTRY>/mentoria-ai:latest`
- `k8s/frontend.yaml` → `image: <REGISTRY>/mentoria-frontend:latest`

## 4. Atualizar domínio

Edite `k8s/ingress.yaml` e troque `mentoria.example.com` pelo seu domínio real.

## 5. Deploy

```bash
# Criar namespace
kubectl apply -f k8s/namespace.yaml

# ConfigMap e Secrets
kubectl apply -f k8s/configmap.yaml
kubectl apply -f k8s/secrets.yaml

# Banco de dados (espere ficar ready antes dos demais)
kubectl apply -f k8s/postgres.yaml
kubectl wait --namespace=mentoria --for=condition=ready pod -l app=postgres --timeout=120s

# Serviços
kubectl apply -f k8s/ai-service.yaml
kubectl apply -f k8s/backend.yaml
kubectl apply -f k8s/frontend.yaml

# Ingress
kubectl apply -f k8s/ingress.yaml
```

## 6. Verificar

```bash
kubectl get all -n mentoria
kubectl get ingress -n mentoria
kubectl logs -n mentoria -l app=backend --tail=50
```

## 7. Escalar manualmente (se necessário)

```bash
kubectl scale deployment backend -n mentoria --replicas=4
kubectl scale deployment ai-service -n mentoria --replicas=3
```

O HPA escala automaticamente com base no uso de CPU (70%).

## Arquitetura

```
Internet → Ingress (nginx)
            ├── /api/*  → Backend (Spring Boot) ×2-8
            │                 └── AI Service (FastAPI) ×2-6
            │                 └── PostgreSQL ×1
            └── /*      → Frontend (Nuxt.js) ×2
```

## TLS / HTTPS

Descomente as seções `tls` no `ingress.yaml` e instale o [cert-manager](https://cert-manager.io/) para certificados Let's Encrypt automáticos.
