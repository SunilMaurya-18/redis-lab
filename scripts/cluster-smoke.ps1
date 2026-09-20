$ErrorActionPreference = "Stop"

docker compose -f docker-compose.cluster.yml up -d
Start-Sleep -Seconds 8
docker exec redis-lab-node-7000 redis-cli -c SET cluster:demo value
docker exec redis-lab-node-7000 redis-cli -c GET cluster:demo
docker exec redis-lab-node-7000 redis-cli -c SET "user:{42}:name" Sunil
docker exec redis-lab-node-7000 redis-cli -c SET "user:{42}:role" developer
docker exec redis-lab-node-7000 redis-cli -c CLUSTER INFO
docker exec redis-lab-node-7000 redis-cli -c CLUSTER NODES
