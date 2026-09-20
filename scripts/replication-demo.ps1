$ErrorActionPreference = "Stop"

docker compose -f docker-compose.replication.yml up -d
docker exec redis-lab-primary redis-cli SET replication:demo primary-value
Start-Sleep -Seconds 2
docker exec redis-lab-replica redis-cli GET replication:demo
docker exec redis-lab-primary redis-cli INFO replication
docker exec redis-lab-replica redis-cli INFO replication
Write-Host "This topology has no automatic failover; stopping the primary only demonstrates replica state, not promotion."
