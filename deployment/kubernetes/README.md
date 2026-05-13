# FTGO Platform — Kubernetes Deployment

## Overview

Kubernetes manifests for deploying the FTGO microservices platform using
[Kustomize](https://kustomize.io/) with environment-specific overlays.

## Architecture

```
deployment/kubernetes/
├── base/                          # Shared manifests (all environments)
│   ├── mysql/                     # MySQL 8.0 (apps/v1, Sealed Secrets)
│   ├── keycloak/                  # Keycloak 21.x identity provider
│   ├── ftgo-consumer-service/     # Consumer Service (port 8081)
│   ├── ftgo-restaurant-service/   # Restaurant Service (port 8082)
│   ├── ftgo-order-service/        # Order Service (port 8083)
│   ├── ftgo-courier-service/      # Courier Service (port 8084)
│   └── ingress/                   # NGINX Ingress routing
└── overlays/
    ├── dev/                       # ftgo-dev namespace (1 replica, low resources)
    ├── staging/                   # ftgo-staging namespace (2 replicas, medium)
    └── prod/                      # ftgo-prod namespace (3 replicas, high + PDB + TLS)
```

## Quick Start

### Preview manifests

```bash
# Render dev manifests
kustomize build deployment/kubernetes/overlays/dev

# Render staging manifests
kustomize build deployment/kubernetes/overlays/staging

# Render prod manifests
kustomize build deployment/kubernetes/overlays/prod
```

### Deploy to an environment

```bash
# Deploy to dev
kustomize build deployment/kubernetes/overlays/dev | kubectl apply -f -

# Deploy to staging
kustomize build deployment/kubernetes/overlays/staging | kubectl apply -f -
```

### Deploy with a specific image tag

```bash
cd deployment/kubernetes/overlays/dev
kustomize edit set image ghcr.io/mbatchelor81/ftgo-consumer-service:0.0.1-a1b2c3d
kustomize build . | kubectl apply -f -
```

## Environment Strategy

| Environment | Namespace     | Replicas | HPA Max | Resources        | PDB |
|-------------|---------------|----------|---------|------------------|-----|
| dev         | `ftgo-dev`    | 1        | 3       | 100m/256Mi req   | No  |
| staging     | `ftgo-staging`| 2        | 6       | 250m/384Mi req   | No  |
| prod        | `ftgo-prod`   | 3        | 15      | 500m/512Mi req   | Yes |

## Secrets Management

Secrets are managed via [Sealed Secrets](https://github.com/bitnami-labs/sealed-secrets).
The `sealed-secret.yaml` files contain placeholder encrypted values that must be
re-sealed with `kubeseal` for each target cluster:

```bash
# Seal a secret for your cluster
echo -n 'my-db-password' \
  | kubectl create secret generic ftgo-mysql-credentials \
      --dry-run=client --from-file=MYSQL_PASSWORD=/dev/stdin -o yaml \
  | kubeseal --format yaml > deployment/kubernetes/base/mysql/sealed-secret.yaml
```

## CI/CD Pipelines

| Workflow | Trigger | Description |
|----------|---------|-------------|
| `cd-deploy.yml` | Push to `master`, or manual | Deploys to target environment |
| `cd-promote.yml` | Manual (workflow_dispatch) | Promotes dev→staging or staging→prod |

### Promotion Flow

```
merge to master → auto-deploy to dev
                       ↓
              manual promote (1 approval)
                       ↓
                    staging
                       ↓
              manual promote (2 approvals)
                       ↓
                     prod
```

### GitHub Environments Setup

Configure these GitHub Environments with protection rules:

- **dev**: No approval required
- **staging**: Require 1 reviewer approval
- **prod**: Require 2 reviewer approvals

Each environment needs a `KUBE_CONFIG` secret containing base64-encoded kubeconfig.

## Rolling Updates

All service deployments use a zero-downtime rolling update strategy:

- `maxUnavailable: 0` — no pods taken down before new ones are ready
- `maxSurge: 1` — one extra pod created during rollout
- Startup, liveness, and readiness probes configured
- `terminationGracePeriodSeconds: 30` for graceful shutdown

## Health Probes

| Probe     | Path                          | Initial Delay | Period |
|-----------|-------------------------------|---------------|--------|
| Startup   | `/actuator/health`            | 20s           | 10s    |
| Liveness  | `/actuator/health/liveness`   | 60s           | 15s    |
| Readiness | `/actuator/health/readiness`  | 30s           | 10s    |
