#!/bin/sh
set -eu

# Render injects DB_HOST / DB_USER / DB_PASSWORD / DB_PORT from the Postgres addon.
# Each service sets DB_NAME (or fromDatabase property: database).
if [ -n "${DB_HOST:-}" ] && [ -n "${DB_NAME:-}" ]; then
  export SPRING_DATASOURCE_URL="jdbc:postgresql://${DB_HOST}:${DB_PORT:-5432}/${DB_NAME}"
  export SPRING_DATASOURCE_USERNAME="${DB_USER:?DB_USER required when DB_HOST is set}"
  export SPRING_DATASOURCE_PASSWORD="${DB_PASSWORD:?DB_PASSWORD required when DB_HOST is set}"
fi

# Ensure resume upload dir exists (candidate-service; harmless elsewhere).
if [ -n "${TALENTPULSE_RESUME_DIR:-}" ]; then
  mkdir -p "${TALENTPULSE_RESUME_DIR}" || true
fi

# Render free outbound often has no IPv6 route → CloudAMQP "Network is unreachable"
exec java -XX:+UseContainerSupport -Djava.net.preferIPv4Stack=true -jar /app/app.jar
