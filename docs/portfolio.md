# Portfolio material

## GitHub description

RedisLab is a Redis internals laboratory covering data structures, transactions,
Lua atomicity, Pub/Sub, Streams, consumer groups, distributed locks, rate
limiting, memory diagnostics, benchmarking, persistence, replication, Redis
Cluster, Docker, and failure simulation with Spring Data Redis.

## Resume bullets

- Built a Redis-focused Spring Boot laboratory demonstrating typed data-structure
  APIs, Streams consumer groups, PEL recovery with XCLAIM/XAUTOCLAIM, and
  at-least-once processing.
- Implemented token-owned Redis locks with Lua-safe release/renewal and atomic
  fixed-window rate limiting with HTTP 429 headers.
- Added SCAN-based key/memory diagnostics and real benchmark comparisons for
  individual commands, pipelining, MSET/MGET, and Lua using latency percentiles.
- Created Docker demonstrations for RDB/AOF persistence, primary/replica
  replication, a six-node Redis Cluster, production-like configuration, and
  failure scenarios.

## LinkedIn project description

RedisLab is a hands-on technical project for understanding Redis beyond basic
CRUD usage. It connects Redis commands to Spring Data Redis APIs and shows the
internal behavior of consumer groups, pending messages, locks, rate-limit
atomicity, memory usage, persistence, replication, cluster slots, and failure
recovery. Claims are limited to the features actually implemented in the Java
services and Docker demonstrations.
