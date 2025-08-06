set -euo pipefail

docker compose -f docker-compose.build.yml up --abort-on-container-exit
docker compose -f docker-compose.build.yml down -v

docker compose build backend
docker compose up -d