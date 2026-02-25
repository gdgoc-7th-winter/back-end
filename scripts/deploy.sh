#!/usr/bin/env bash
set -euo pipefail

# ---- 설정 (변수화) ----
DEPLOY_DIR="/opt/hufsdev/backend"
ECR_REGISTRY="873135413383.dkr.ecr.ap-northeast-2.amazonaws.com"
REPO_NAME="hufsdev-backend"
CONTAINER_NAME="hufsdev-backend"
HEALTH_URL="http://localhost:8080/actuator/health"
CURL_OPTS=(--connect-timeout 2 --max-time 3 -fsS)
LOCK_FILE="$DEPLOY_DIR/.deploy.lock"

cd "$DEPLOY_DIR"

# ---- 실행 잠금 ----
exec 9>"$LOCK_FILE"
flock -n 9 || { echo "Another deployment is already running"; exit 1; }
mkdir -p logs

IMAGE_TAG="${1:-latest}"
export IMAGE_REF="$ECR_REGISTRY/$REPO_NAME:$IMAGE_TAG"
echo "Deploying image: $IMAGE_REF"

echo "===== DEPLOY START: $(date -u +"%Y-%m-%dT%H:%M:%SZ") ====="

# ECR login - IAM Role 기반
echo "[1/8] ECR login"
aws ecr get-login-password --region ap-northeast-2 \
| docker login --username AWS --password-stdin "$ECR_REGISTRY"

echo "[2/8] Save rollback target (current running image)"
CURRENT_IMAGE=$(docker inspect "$CONTAINER_NAME" --format '{{.Config.Image}}' 2>/dev/null || true)
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
HEALTH_OK=false
for i in {1..40}; do
  if curl "${CURL_OPTS[@]}" "$HEALTH_URL" >/dev/null; then
    HEALTH_OK=true
    echo "Health OK"
    break
  fi
  sleep 2
done

if [[ "$HEALTH_OK" != "true" ]]; then
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
    echo "Waiting for rollback container to be healthy..."
    ROLLBACK_HEALTH_OK=false
    for i in {1..20}; do
      if curl "${CURL_OPTS[@]}" "$HEALTH_URL" >/dev/null; then
        ROLLBACK_HEALTH_OK=true
        break
      fi
      sleep 2
    done
    if [[ "$ROLLBACK_HEALTH_OK" == "true" ]]; then
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
docker ps --filter "name=$CONTAINER_NAME"

echo "[7/8] Cleanup dangling images"
docker image prune -f

echo "[8/8] Remove old images (keep last 5)"
docker images "$ECR_REGISTRY/$REPO_NAME" \
  --format "{{.CreatedAt}} {{.Repository}}:{{.Tag}}" \
  | grep -v ":latest" \
  | sort -r \
  | awk '{print $NF}' \
  | tail -n +6 \
  | xargs -r docker rmi || true

echo "===== DEPLOY COMPLETE ====="
echo "DEPLOY_SUCCESS"
