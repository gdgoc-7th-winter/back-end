#!/usr/bin/env bash
set -euo pipefail

# ---- 설정 변수 ----
DEPLOY_DIR="${DEPLOY_DIR:-/opt/hufsdev/backend}"
ECR_REGISTRY="${ECR_REGISTRY:-873135413383.dkr.ecr.ap-northeast-2.amazonaws.com}"
REPO_NAME="${REPO_NAME:-hufsdev-backend}"
AWS_REGION="${AWS_REGION:-ap-northeast-2}"

BLUE_SERVICE="backend-blue"
GREEN_SERVICE="backend-green"
NGINX_SERVICE="nginx"
BLUE_PORT="${BLUE_PORT:-8080}"
GREEN_PORT="${GREEN_PORT:-8081}"

HEALTH_PATH="/actuator/health/readiness"
HEALTH_RETRIES=40
HEALTH_INTERVAL=2
CURL_OPTS=(--connect-timeout 5 --max-time 15 -fsS)

LOCK_FILE="$DEPLOY_DIR/.deploy.lock"
ACTIVE_SLOT_FILE="$DEPLOY_DIR/.active_slot"
NGINX_UPSTREAM_CONF="$DEPLOY_DIR/nginx/conf.d/upstream.conf"

cd "$DEPLOY_DIR"

# ---- 실행 잠금 ----
exec 9>"$LOCK_FILE"
flock -n 9 || { echo "[!] Another deployment is already running"; exit 1; }
mkdir -p logs/blue logs/green nginx/conf.d

IMAGE_TAG="${1:-latest}"
export IMAGE_REF="$ECR_REGISTRY/$REPO_NAME:$IMAGE_TAG"
echo "===== DEPLOY START: $(date -u +"%Y-%m-%dT%H:%M:%SZ") ====="
echo "Image: $IMAGE_REF"

# -----------------------------------------------------------------------------
# (1) 현재 활성 슬롯 감지
# -----------------------------------------------------------------------------
echo "[1/8] Detect active slot"
detect_active_slot() {
  if [[ -f "$ACTIVE_SLOT_FILE" ]]; then
    local slot
    slot=$(cat "$ACTIVE_SLOT_FILE")
    if [[ "$slot" == "blue" || "$slot" == "green" ]]; then
      echo "$slot"
      return
    fi
  fi
  if docker ps -q -f "name=hufsdev-backend-blue" --format '{{.Names}}' 2>/dev/null | grep -q .; then
    echo "blue"
    return
  fi
  if docker ps -q -f "name=hufsdev-backend-green" --format '{{.Names}}' 2>/dev/null | grep -q .; then
    echo "green"
    return
  fi
  # 레거시 마이그레이션: 기존 hufsdev-backend → blue로 간주
  if docker ps -q -f "name=hufsdev-backend" --format '{{.Names}}' 2>/dev/null | grep -q .; then
    echo "blue"
    return
  fi
  echo ""
}

ACTIVE=$(detect_active_slot)
FIRST_DEPLOY=false
if [[ -z "$ACTIVE" ]]; then
  echo "  No active slot found - first deploy"
  ACTIVE="blue"
  FIRST_DEPLOY=true
fi
echo "  Active slot: $ACTIVE"

# -----------------------------------------------------------------------------
# (2) 비활성 슬롯 결정 (배포 타겟)
# -----------------------------------------------------------------------------
echo "[2/8] Determine target slot"
if [[ "$FIRST_DEPLOY" == "true" ]]; then
  TARGET="blue"
  TARGET_SERVICE="$BLUE_SERVICE"
  TARGET_PORT="$BLUE_PORT"
elif [[ "$ACTIVE" == "blue" ]]; then
  TARGET="green"
  TARGET_SERVICE="$GREEN_SERVICE"
  TARGET_PORT="$GREEN_PORT"
else
  TARGET="blue"
  TARGET_SERVICE="$BLUE_SERVICE"
  TARGET_PORT="$BLUE_PORT"
fi
echo "  Target slot: $TARGET ($TARGET_SERVICE, port $TARGET_PORT)"

# -----------------------------------------------------------------------------
# (3) 최신 이미지 pull
# -----------------------------------------------------------------------------
echo "[3/8] ECR login & pull image"
aws ecr get-login-password --region "$AWS_REGION" \
  | docker login --username AWS --password-stdin "$ECR_REGISTRY"
docker compose pull "$TARGET_SERVICE"

# -----------------------------------------------------------------------------
# (4) target 슬롯 컨테이너 실행 (기존 컨테이너 유지)
# -----------------------------------------------------------------------------
echo "[4/8] Start target slot container ($TARGET_SERVICE)"
docker compose up -d --force-recreate "$TARGET_SERVICE"
echo "  Waiting for container to be ready..."
sleep 5

# -----------------------------------------------------------------------------
# (5) 헬스체크 수행
# -----------------------------------------------------------------------------
echo "[5/8] Health check ($TARGET_PORT -> $HEALTH_PATH)"
HEALTH_URL="http://127.0.0.1:$TARGET_PORT$HEALTH_PATH"
HEALTH_OK=false
for i in $(seq 1 "$HEALTH_RETRIES"); do
  if curl "${CURL_OPTS[@]}" "$HEALTH_URL" >/dev/null 2>&1; then
    HEALTH_OK=true
    echo "  Health OK (attempt $i)"
    break
  fi
  echo "  Attempt $i/$HEALTH_RETRIES - waiting ${HEALTH_INTERVAL}s..."
  sleep "$HEALTH_INTERVAL"
done

if [[ "$HEALTH_OK" != "true" ]]; then
  echo "[!] Health check FAILED - rolling back"
  echo "[!] Dump target container logs:"
  docker compose logs --no-color --tail=100 "$TARGET_SERVICE" 2>/dev/null || true
  echo "[!] Stopping and removing failed target container"
  docker compose stop "$TARGET_SERVICE" 2>/dev/null || true
  docker rm -f "hufsdev-backend-$TARGET" 2>/dev/null || true
  echo "[!] Rollback complete - active slot unchanged ($ACTIVE)"
  exit 1
fi

# -----------------------------------------------------------------------------
# (6) 트래픽 전환 (Nginx upstream 변경)
# -----------------------------------------------------------------------------
echo "[6/8] Traffic switch (Nginx upstream -> $TARGET)"
if [[ "$TARGET" == "blue" ]]; then
  echo 'upstream backend { server 127.0.0.1:8080; }' > "$NGINX_UPSTREAM_CONF"
else
  echo 'upstream backend { server 127.0.0.1:8081; }' > "$NGINX_UPSTREAM_CONF"
fi
if docker ps -q -f "name=hufsdev-nginx" --format '{{.Names}}' 2>/dev/null | grep -q .; then
  docker exec hufsdev-nginx nginx -s reload 2>/dev/null || docker compose restart "$NGINX_SERVICE"
else
  echo "  Starting nginx..."
  docker compose up -d "$NGINX_SERVICE"
fi
echo "  Traffic switched to $TARGET"

# -----------------------------------------------------------------------------
# (7) 기존 슬롯 컨테이너 종료
# -----------------------------------------------------------------------------
echo "[7/8] Stop previous slot container"
echo "$TARGET" > "$ACTIVE_SLOT_FILE"
if [[ "$FIRST_DEPLOY" != "true" ]]; then
  if [[ "$ACTIVE" == "blue" ]]; then
    docker compose stop "$BLUE_SERVICE" 2>/dev/null || true
    echo "  Stopped $BLUE_SERVICE"
  else
    docker compose stop "$GREEN_SERVICE" 2>/dev/null || true
    echo "  Stopped $GREEN_SERVICE"
  fi
  # 레거시 마이그레이션: 기존 hufsdev-backend 컨테이너 정리
  if docker ps -a -q -f "name=hufsdev-backend" --format '{{.Names}}' 2>/dev/null | grep -q .; then
    docker stop hufsdev-backend 2>/dev/null || true
    docker rm hufsdev-backend 2>/dev/null || true
    echo "  Stopped legacy hufsdev-backend (migration)"
  fi
else
  echo "  First deploy - no previous container to stop"
fi

# -----------------------------------------------------------------------------
# (8) 정리 및 완료
# -----------------------------------------------------------------------------
echo "[8/8] Cleanup"
docker system prune -f 2>/dev/null || true
docker images "$ECR_REGISTRY/$REPO_NAME" --format "{{.CreatedAt}} {{.Repository}}:{{.Tag}}" \
  | grep -v ":latest" | sort -r | awk '{print $NF}' | tail -n +3 \
  | xargs -r docker rmi 2>/dev/null || true

echo "===== DEPLOY COMPLETE ====="
echo "Active slot: $TARGET"
echo "DEPLOY_SUCCESS"
