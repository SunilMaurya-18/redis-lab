$ErrorActionPreference = "Stop"

docker compose -f docker-compose.persistence.yml up -d
docker exec redis-lab-rdb redis-cli SET persistence:rdb survived
docker exec redis-lab-aof redis-cli SET persistence:aof survived
docker compose -f docker-compose.persistence.yml restart redis-rdb redis-aof
docker exec redis-lab-rdb redis-cli GET persistence:rdb
docker exec redis-lab-aof redis-cli GET persistence:aof
docker exec redis-lab-rdb redis-cli INFO persistence
docker exec redis-lab-aof redis-cli INFO persistence
