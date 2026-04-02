# MentorIA — Kubernetes Deployment

## Pré-requisitos

- Cluster Kubernetes (GKE, EKS, AKS, DigitalOcean ou similar)
- `kubectl` configurado e apontando para o cluster
- Ingress Controller **nginx** instalado (`kubectl apply -f https://raw.githubusercontent.com/kubernetes/ingress-nginx/main/deploy/static/provider/cloud/deploy.yaml`)
- Container registry (Docker Hub, GCR, ECR, GHCR, etc.)

## 1. Build e push das imagens

```bash
# Backend (Spring Boot)
docker build -t <REGISTRY>/mentoria-backend:latest ./backend
docker push <REGISTRY>/mentoria-backend:latest

# AI Service (Python/FastAPI)
docker build -t <REGISTRY>/mentoria-ai:latest ./ai-service
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

> ⚠️ **NUNCA** faça commit de secrets reais. Use [Sealed Secrets](https://github.com/bitnami-labs/sealed-secrets), SOPS ou um vault externo em produção.

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

# Network Policies (isolamento entre microsserviços)
kubectl apply -f k8s/network-policy.yaml

# Serviços (o banco usa Supabase gerenciado — sem postgres local)
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
kubectl get hpa -n mentoria
kubectl get pdb -n mentoria
kubectl get networkpolicy -n mentoria
kubectl logs -n mentoria -l app=backend --tail=50
```

## 7. Escalar manualmente (se necessário)

```bash
kubectl scale deployment backend -n mentoria --replicas=4
kubectl scale deployment ai-service -n mentoria --replicas=3
```

O HPA escala automaticamente com base no uso de CPU (70%).
PodDisruptionBudgets garantem pelo menos 1 pod disponível durante rolling updates.

## Arquitetura

```
Internet → Ingress (nginx) + TLS + Rate Limiting + Security Headers
            ├── /api/*    → Backend (Spring Boot)  ×2-8  (HPA + PDB)
            │                 └── AI Service (FastAPI) ×2-6  (HPA + PDB)
            │                 └── Supabase PostgreSQL (gerenciado)
            ├── /api/ws   → Backend WebSocket (SockJS/STOMP)
            └── /*        → Frontend (Nuxt.js)     ×2-4  (HPA)

NetworkPolicies:
  AI Service  ← só aceita tráfego do Backend
  Backend     ← aceita do Ingress + Frontend
  Frontend    ← aceita do Ingress
```

## Manifests incluídos

| Arquivo | Recursos |
|---------|----------|
| `namespace.yaml` | Namespace `mentoria` |
| `configmap.yaml` | ConfigMap (URLs, CORS, Java opts, Spring profile) |
| `secrets.yaml` | Secret (DB, JWT, Anthropic, Stripe, Supabase) |
| `backend.yaml` | Deployment + Service + HPA + PDB |
| `ai-service.yaml` | Deployment + Service + HPA + PDB |
| `frontend.yaml` | Deployment + Service + HPA |
| `ingress.yaml` | Ingress nginx (API + WebSocket + Frontend) |
| `network-policy.yaml` | 3 NetworkPolicies (isolamento) |

## TLS / HTTPS

1. Instale o [cert-manager](https://cert-manager.io/)
2. Crie um `ClusterIssuer` (Let's Encrypt)
3. Descomente as seções `tls` e `cert-manager.io/cluster-issuer` no `ingress.yaml`
