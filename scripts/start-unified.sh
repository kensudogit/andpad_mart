#!/bin/sh
# Railway 統合デプロイ: Spring Boot API (8081) + Next.js (PORT) を同一コンテナで起動。
# Go 版と同様、API 未起動でも Next.js を起動し /api/status で診断できるようにする。
set -e

WEB_PORT="${PORT:-3000}"
API_PORT="${API_INTERNAL_PORT:-8081}"

env_state() {
  if [ -n "${1:-}" ]; then
    echo set
  else
    echo empty
  fi
}

ref_unresolved() {
  case "${1:-}" in
    *'${{'*) return 0 ;;
    *) return 1 ;;
  esac
}

external_api_healthy() {
  case "${API_URL:-}" in
    *127.0.0.1*|*localhost*)
      return 1
      ;;
    http://*|https://*)
      api_base="${API_URL%/}"
      body="$(curl -sf --max-time 8 "${api_base}/health" 2>/dev/null)" || return 1
      echo "$body" | grep -qE '"ok"\s*:\s*true|"service"\s*:\s*"(andpad-api|dental-video-api)"' || return 1
      return 0
      ;;
    *)
      return 1
      ;;
  esac
}

if external_api_healthy; then
  echo "[web] external API_URL=${API_URL} — Next.js only (separate api service)"
  unset UNIFIED_DEPLOY
  cd /app/frontend
  PORT="${WEB_PORT}" HOSTNAME=0.0.0.0 exec npm start
fi

export API_INTERNAL_PORT="${API_PORT}"
export API_URL="http://127.0.0.1:${API_PORT}"
export UNIFIED_DEPLOY=1

if [ -z "${DATABASE_URL:-}" ] && [ -n "${DATABASE_PRIVATE_URL:-}" ]; then
  if ! ref_unresolved "${DATABASE_PRIVATE_URL}"; then
    export DATABASE_URL="${DATABASE_PRIVATE_URL}"
    echo "[unified] using DATABASE_PRIVATE_URL as DATABASE_URL"
  fi
fi

echo "[unified] web=${WEB_PORT} api=${API_PORT}"
echo "[unified] DATABASE_URL=$(env_state "${DATABASE_URL:-}")"
echo "[unified] DATABASE_PRIVATE_URL=$(env_state "${DATABASE_PRIVATE_URL:-}")"
echo "[unified] PGHOST=$(env_state "${PGHOST:-}")"
echo "[unified] JWT_SECRET=$(env_state "${JWT_SECRET:-}")"

db_configured=0
if [ -n "${DATABASE_URL:-}" ] && ! ref_unresolved "${DATABASE_URL}"; then
  db_configured=1
elif [ -n "${DATABASE_PRIVATE_URL:-}" ] && ! ref_unresolved "${DATABASE_PRIVATE_URL}"; then
  export DATABASE_URL="${DATABASE_PRIVATE_URL}"
  db_configured=1
elif [ -n "${PGHOST:-}" ] && ! ref_unresolved "${PGHOST}"; then
  db_configured=1
fi

if [ "$db_configured" -eq 0 ]; then
  echo "[unified] WARNING: DATABASE_URL is not configured — API will not start"
  echo "[unified]   andpad_j service → Variables → Reference → Postgres → DATABASE_URL"
elif ref_unresolved "${DATABASE_URL:-}"; then
  echo "[unified] WARNING: DATABASE_URL looks like an unresolved Railway reference (\${{...}})"
  echo "[unified]   Fix the variable reference on the app service, then Redeploy"
else
  if [ -z "${JWT_SECRET:-}" ] || [ "${JWT_SECRET}" = "dev-only-change-in-production-min-32-chars" ]; then
    echo "[unified] WARNING: set a strong JWT_SECRET (32+ chars) on Railway"
  fi

  echo "[unified] starting Spring Boot API in background..."
  API_LOG="/tmp/api.log"
  : >"${API_LOG}"
  JAVA_OPTS="${JAVA_OPTS:--XX:+UseContainerSupport -Xmx256m -Xms128m -XX:MaxMetaspaceSize=128m}"

  (
    export DATABASE_URL="${DATABASE_URL:-}"
    export DATABASE_PRIVATE_URL="${DATABASE_PRIVATE_URL:-}"
    export JWT_SECRET="${JWT_SECRET:-}"
    export OPENAI_API_KEY="${OPENAI_API_KEY:-}"
    export PGHOST="${PGHOST:-}"
    export PGUSER="${PGUSER:-}"
    export PGPASSWORD="${PGPASSWORD:-}"
    export PGDATABASE="${PGDATABASE:-}"
    export PGPORT="${PGPORT:-}"
    SERVER_PORT="${API_PORT}" PORT="${API_PORT}" \
      exec java ${JAVA_OPTS} \
        -Dserver.port="${API_PORT}" \
        -Dserver.address=0.0.0.0 \
        -Dspring.main.cloud-platform=NONE \
        -jar /app/app.jar
  ) >>"${API_LOG}" 2>&1 &
  API_PID=$!
  echo "[unified] Java API pid=${API_PID}"

  (
    i=0
    while [ "$i" -lt 360 ]; do
      if curl -sf "http://127.0.0.1:${API_PORT}/health" >/dev/null 2>&1; then
        echo "[unified] Java API ready on 127.0.0.1:${API_PORT}"
        exit 0
      fi
      if ! kill -0 "$API_PID" 2>/dev/null; then
        echo "[unified] ERROR: Java API exited before becoming ready"
        tail -n 80 "${API_LOG}" 2>/dev/null || true
        exit 1
      fi
      i=$((i + 1))
      sleep 0.5
    done
    echo "[unified] WARNING: Java API not ready after 180s (login/GraphQL may return 502 until ready)"
    tail -n 40 "${API_LOG}" 2>/dev/null || true
  ) &
fi

echo "[unified] starting Next.js on ${WEB_PORT} (Railway healthcheck)"
cd /app/frontend
PORT="${WEB_PORT}" HOSTNAME=0.0.0.0 exec npm start
