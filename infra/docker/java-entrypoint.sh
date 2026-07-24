#!/bin/sh
set -eu

# Render injects DB_HOST / DB_USER / DB_PASSWORD / DB_PORT from the Postgres addon.
# Each service sets DB_NAME (talentpulse_auth, talentpulse_job, ...).
if [ -n "${DB_HOST:-}" ] && [ -n "${DB_NAME:-}" ]; then
  export SPRING_DATASOURCE_URL="jdbc:postgresql://${DB_HOST}:${DB_PORT:-5432}/${DB_NAME}"
  export SPRING_DATASOURCE_USERNAME="${DB_USER:?DB_USER required when DB_HOST is set}"
  export SPRING_DATASOURCE_PASSWORD="${DB_PASSWORD:?DB_PASSWORD required when DB_HOST is set}"
fi

exec java -XX:+UseContainerSupport -jar /app/app.jar
