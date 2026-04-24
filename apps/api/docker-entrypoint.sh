#!/usr/bin/env sh
set -eu

if [ -n "${DATABASE_URL:-}" ] && [ -z "${SPRING_DATASOURCE_URL:-}" ]; then
  case "$DATABASE_URL" in
    postgresql://*)
      database_without_scheme="${DATABASE_URL#postgresql://}"
      database_credentials="${database_without_scheme%%@*}"
      database_host_and_path="${database_without_scheme#*@}"
      export SPRING_DATASOURCE_USERNAME="${SPRING_DATASOURCE_USERNAME:-${database_credentials%%:*}}"
      export SPRING_DATASOURCE_PASSWORD="${SPRING_DATASOURCE_PASSWORD:-${database_credentials#*:}}"
      export SPRING_DATASOURCE_URL="jdbc:postgresql://${database_host_and_path}"
      ;;
    jdbc:postgresql://*) export SPRING_DATASOURCE_URL="$DATABASE_URL" ;;
  esac
fi

if [ -n "${REDIS_URL:-}" ] && [ -z "${SPRING_DATA_REDIS_URL:-}" ]; then
  export SPRING_DATA_REDIS_URL="$REDIS_URL"
fi

exec java -Dserver.port="${PORT:-8080}" -jar /app/app.jar
