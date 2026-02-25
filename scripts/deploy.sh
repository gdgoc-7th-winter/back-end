#!/usr/bin/env bash
set -euo pipefail

cd /opt/hufsdev/backend
mkdir -p logs

IMAGE_TAG="${1:-latest}"
export IMAGE_REF="873135413383.dkr.ecr.ap-northeast-2.amazonaws.com/hufsdev-backend:$IMAGE_TAG"
echo "Deploying image: $IMAGE_REF"

echo "===== DEPLOY START: $(date -u +"%Y-%m-%dT%H:%M:%SZ") ====="

# ECR login - IAM Role 기반
echo "[1/8] ECR login"
aws ecr get-login-password --region ap-northeast-2 \
| docker login --username AWS --password-stdin 873135413383.dkr.ecr.ap-northeast-2.amazonaws.com

echo "[2/8] Save rollback target (current running image)"
CURRENT_IMAGE=$(docker inspect hufsdev-backend --format '{{.Config.Image}}' 2>/dev/null || true)
if [[ -n "$CURRENT_IMAGE" ]]; then
  echo "$CURRENT_IMAGE" > .prev_ref
  echo "Rollback target: $CURRENT_IMAGE"
else
  echo "No running container - skip rollback target"
fi

echo "[debug] IMAGE_REF=$IMAGE_REF"
echo "[debug] Resolved image (ECR login 후, pull 직전):"
docker compose config | sed -n '/image:/p'

echo "[3/8] Pull image ($IMAGE_TAG)"
docker compose pull

echo "[4/8] Restart container"
docker compose down
docker compose up -d --remove-orphans

echo "[5/8] Health check"
for i in {1..40}; do
  if curl -fsS http://localhost:8080/actuator/health >/dev/null; then
    echo "Health OK"
    break
  fi
  sleep 2
done

if ! curl -fsS http://localhost:8080/actuator/health >/dev/null; then
  echo "[!] Health FAILED - attempting rollback"
  echo "[!] Dump recent logs"
  docker compose logs --no-color --tail=200 backend || true
  if [[ -f .prev_ref ]]; then
    ROLLBACK_REF=$(cat .prev_ref)
    echo "Rolling back to: $ROLLBACK_REF"
    export IMAGE_REF="$ROLLBACK_REF"
    docker compose pull
    docker compose down
    docker compose up -d --remove-orphans
    sleep 5
    if curl -fsS http://localhost:8080/actuator/health >/dev/null; then
      echo "Rollback OK - previous version restored"
    else
      echo "Rollback FAILED - manual intervention required"
    fi
  else
    echo "No .prev_ref - cannot rollback"
  fi
  exit 1
fi

echo "[6/8] Show status"
docker ps --filter "name=hufsdev-backend"

echo "[7/8] Cleanup dangling images"
docker image prune -f

echo "[8/8] Remove old images (keep last 5)"
docker images 873135413383.dkr.ecr.ap-northeast-2.amazonaws.com/hufsdev-backend \
  --format "{{.CreatedAt}} {{.Repository}}:{{.Tag}}" \
  | grep -v ":latest" \
  | sort -r \
  | awk '{print $NF}' \
  | tail -n +6 \
  | xargs -r docker rmi || true

echo "===== DEPLOY COMPLETE ====="
echo "DEPLOY_SUCCESS"
