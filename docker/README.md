# RedisLab Docker topologies

The default `docker-compose.yml` is the development single-node setup.

```powershell
docker compose up -d
docker compose -f docker-compose.persistence.yml up -d
docker compose -f docker-compose.replication.yml up -d
docker compose -f docker-compose.cluster.yml up -d
Copy-Item .env.example .env
docker compose -f docker-compose.production.yml up -d
```

Use separate Docker projects or stop the previous topology before reusing the
same host ports. The replication topology is primary/replica only; it does not
provide automatic failover. The cluster topology uses six nodes with three
primaries and three replicas and requires a Redis version supporting hostname
announcement.
