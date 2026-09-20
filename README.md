# RedisLab

[![Java 26](https://img.shields.io/badge/Java-26-ED8B00?logo=openjdk&logoColor=white)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1.1-6DB33F?logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Docker](https://img.shields.io/badge/Docker-required-2496ED?logo=docker&logoColor=white)](https://www.docker.com/)
[![Redis](https://img.shields.io/badge/Redis-internals-DC382D?logo=redis&logoColor=white)](https://redis.io/)

RedisLab is a practical Redis internals laboratory built with Spring Boot,
Spring Data Redis, Java 26, Maven, and Docker. It is intentionally not a CRUD
business application. Each phase exposes Redis behavior directly through CLI
commands, typed Spring Data Redis APIs, HTTP demonstrations, Docker
topologies, tests, and failure simulations.

The goal is to make Redis behavior observable: run a command, inspect the
result, reproduce a failure, and compare the implementation trade-offs rather
than relying on mocked examples or undocumented assumptions.

**Repository:** [github.com/SunilMaurya-18/redis-lab](https://github.com/SunilMaurya-18/redis-lab)

## Why this project?

Redis is often introduced as a fast key-value store, but production behavior
depends on details such as atomicity, expiry, consumer-group recovery, client
routing, persistence, and failure handling. RedisLab is a focused learning and
portfolio project for exploring those details in a runnable environment.

## What is implemented

- Redis data structures: strings, hashes, lists, sets, sorted sets
- Expiration and TTL behavior
- Transactions, `WATCH`, pipelining, Lua, Pub/Sub, Streams
- Streams consumer groups, PEL inspection, `XCLAIM`, `XAUTOCLAIM`
- Token-based distributed locks with safe Lua release and renewal
- Fixed-window rate limiting with non-atomic and Lua-atomic variants
- HTTP `429` responses and rate-limit headers
- Memory/key diagnostics using `SCAN`, `MEMORY`, `OBJECT`, `TYPE`, and TTLs
- Real Redis benchmarks with warm-up, repeated samples, throughput, p50, p95, and p99
- RDB/AOF persistence Docker demonstrations
- Primary/replica Docker topology
- Six-node Redis Cluster Docker topology
- Validation tests and PowerShell failure simulations
- Development and production-like Docker configurations

The Java application is intentionally single-node by default. Cluster-aware
deployment is demonstrated separately because Redis Cluster changes client
routing, multi-key design, Lua key rules, transactions, and pipelines.

## Browser operations console

RedisLab includes a zero-dependency browser console served by Spring Boot. Start
the application, then open `http://localhost:8081/`. It provides:

- a PowerShell-style command runner (`PING`, `SET`, `GET`, `HSET`, `LPUSH`,
  `SADD`, `ZADD`, Streams, locks, rate limits, diagnostics, pipelines, and
  benchmarks);
- guided forms for data structures, Streams and consumer groups, distributed
  locks, rate limiting, diagnostics, and benchmarks;
- structured JSON responses, command history, quick actions, and connection
  and memory status;
- no frontend build or internet dependency; the files live in
  `src/main/resources/static`.

For a short demonstration, click **Run smoke test** on Overview, then create a
Stream group, add an event, read it, inspect Pending, and acknowledge its ID.

The console is intended for local exploration and demonstrations; it is not a
production administration panel.

For a terminal-only recording, start the interactive short-command console:

```powershell
.\scripts\recording-console.ps1
```

Then type commands such as `ping`, `set demo hello`, `get demo`,
`stream-add events created hello`, `pending events workers`, `burst demo`, and
`bench`. Type `help` inside the console for the full list.

## Architecture

```text
Browser console / HTTP controllers / PowerShell scripts / redis-cli
                         |
                 RedisLab services
                         |
              StringRedisTemplate / RedisConnection
                         |
      Redis standalone, replica topology, or cluster topology
```

See [docs/architecture.md](docs/architecture.md) for the command-to-API-to-
implementation mapping.

## Requirements

- JDK 26 for the declared project configuration
- Docker Desktop with the Linux engine running
- Windows PowerShell

The project is configured for Java 26. Use JDK 26 for a normal build; an older
JDK will fail during compilation because the declared release is 26.

## Quick start

Clone the repository and start the development topology:

```powershell
git clone https://github.com/SunilMaurya-18/redis-lab.git
cd redis-lab
docker compose up -d
.\mvnw.cmd clean compile
.\mvnw.cmd spring-boot:run
```

Open the browser console at [http://localhost:8081](http://localhost:8081).
The API also listens on port `8081`.

The Maven wrapper includes a PowerShell compatibility fix for ordinary
directories under `.m2`. If Docker is unavailable, application-context tests
disable only the Pub/Sub listener; live Redis operations still require Redis.

## Useful endpoints

```text
POST /api/v1/redis/locks/try
POST /api/v1/redis/locks/release
POST /api/v1/redis/locks/renew

POST /api/v1/redis/rate-limits/simple
POST /api/v1/redis/rate-limits/atomic
GET  /api/v1/limited/resource

GET  /api/v1/redis/diagnostics/key
GET  /api/v1/redis/diagnostics/memory
GET  /api/v1/redis/diagnostics/scan
GET  /api/v1/redis/diagnostics/analyze

POST /api/v1/redis/benchmarks/run

POST /api/v1/redis/stream-groups/create
GET  /api/v1/redis/stream-groups/read
POST /api/v1/redis/stream-groups/ack
GET  /api/v1/redis/stream-groups/pending
POST /api/v1/redis/stream-groups/claim
POST /api/v1/redis/stream-groups/autoclaim
```

## Consumer-group quick start

```powershell
$base = "http://localhost:8081/api/v1/redis/stream-groups"
$stream = "redislab:events"
$group = "workers"

Invoke-RestMethod "$base/create?stream=$stream&group=$group&startId=0-0" -Method Post
Invoke-RestMethod "$base/add?stream=$stream&eventType=order.created&payload=order-1" -Method Post
Invoke-RestMethod "$base/read?stream=$stream&group=$group&consumer=consumer-a&offset=%3E&count=10" -Method Get
Invoke-RestMethod "$base/pending/summary?stream=$stream&group=$group" -Method Get
```

Use `offset=0-0` to inspect messages pending for the current consumer and
`XAUTOCLAIM`/the `/autoclaim` endpoint to recover idle messages from a failed
consumer.

## Docker demonstrations

```powershell
docker compose -f docker-compose.persistence.yml up -d
docker compose -f docker-compose.replication.yml up -d
docker compose -f docker-compose.cluster.yml up -d
Copy-Item .env.example .env
docker compose -f docker-compose.production.yml up -d
```

Run the demonstrations:

```powershell
.\scripts\persistence-demo.ps1
.\scripts\replication-demo.ps1
.\scripts\cluster-smoke.ps1
.\scripts\failure-simulation.ps1
```

Stop a topology before starting another if host ports overlap. The replication
setup does not configure Sentinel or automatic failover. The cluster setup is
for local learning and is not a production security configuration.

## Testing

```powershell
.\mvnw.cmd test
```

The test suite includes application-context verification and validation tests
for locks, rate limits, diagnostics, and benchmarks. Live Redis integration
scenarios are reproducible with the Docker and PowerShell scripts so that
consumer crashes, stale locks, replication state, persistence, and cluster
routing are visible rather than mocked away.

## Benchmarking

The benchmark endpoint performs real operations against the configured Redis
instance and reports measured values only:

```powershell
Invoke-RestMethod `
  "http://localhost:8081/api/v1/redis/benchmarks/run?warmupIterations=100&measuredIterations=1000&batchSize=100" `
  -Method Post
```

Compare individual commands, pipelines, MSET/MGET, and Lua using the same host,
payload size, warm-up, batch size, and Redis state. See
[docs/benchmarking.md](docs/benchmarking.md).

## Roadmap

| Phase | Topic | Status |
|---:|---|---|
| 0–12 | Fundamentals through Streams | Complete |
| 13 | Streams Consumer Groups | Complete |
| 14 | Distributed Locks | Implemented |
| 15 | Rate Limiting | Implemented |
| 16 | Memory & Key Analysis | Implemented |
| 17 | Performance Benchmarking | Implemented |
| 18 | Persistence | Docker demonstration implemented |
| 19 | Replication | Docker primary/replica implemented |
| 20 | Redis Cluster | Six-node Docker demonstration implemented |
| 21 | Testing & Failure Simulation | Tests and scripts implemented |
| 22 | Docker & Production Setup | Development and production-like configs implemented |
| 23 | Documentation & Portfolio | This README and docs implemented |

## Limitations stated deliberately

- The default Java client is not cluster-aware; use a cluster-capable client and
  topology configuration before targeting Redis Cluster.
- A single Redis lock is not a consensus system and cannot guarantee safety
  across every network partition. Redlock is discussed as a design trade-off,
  not silently enabled.
- Rate limiting implements fixed windows. Sliding-window and token-bucket
  algorithms are documented as designs to compare, not claimed as code that is
  absent.
- Docker examples are learning environments. Production deployments need
  secret management, ACLs, TLS, backups, resource sizing, monitoring, and an
  explicit failover strategy.
 
