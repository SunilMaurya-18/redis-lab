# Remaining phase notes

## Phase 14 — Distributed locks

**What / why.** A distributed lock coordinates work across application
instances. Redis stores a random ownership token with a finite lease.

**Command flow.** `SET lock token NX PX 10000` atomically acquires the lease.
The owner releases it with a Lua script that deletes only when `GET lock`
matches the token. A plain `DEL lock` is unsafe because an expired lock may
already belong to another owner.

**Spring mapping.** `ValueOperations.setIfAbsent(key, token, Duration)` maps to
`SET NX PX`; `RedisScript<Long>` maps to the compare-and-delete and renewal
scripts in `RedisDistributedLockService`.

**Failure behavior.** A process crash leaves a bounded stale lease. Renewal is
token-checked. A long critical section needs renewal or a larger lease and must
still be idempotent. A lock does not make a network partition safe, does not
provide fencing tokens automatically, and does not turn Redis into a consensus
system.

**Redlock.** Redlock acquires a majority across independent Redis masters. It
has a different failure model and operational cost than this single-instance
lock. RedisLab does not claim Redlock or automatic failover; choose it only
after reviewing clock, pause, quorum, and partition assumptions.

Interview prompts: why `NX` and `PX` must be one command; why ownership tokens
are required; what happens when GC pauses outlast the lease; when a database
constraint or queue may be better than a lock.

## Phase 15 — Rate limiting

**Fixed window.** Redis keys represent a scope and time bucket. `INCR` counts
requests and `EXPIRE` defines the reset. The simple implementation intentionally
shows the race between those commands; the Lua implementation performs both
atomically.

**Algorithms.** Fixed windows are cheap but bursty at boundaries. Sliding
windows use sorted-set timestamps and are more precise but use more memory.
Token buckets model refill and bursts. Leaky buckets smooth output but may
queue work. RedisLab implements fixed-window variants and documents the other
trade-offs rather than pretending they are present.

**HTTP behavior.** `/api/v1/limited/resource` returns `429` after the limit and
sets `X-RateLimit-Limit`, `X-RateLimit-Remaining`, and
`X-RateLimit-Reset-After` headers. Scopes can be per user or per forwarded IP.
Trust proxy headers only behind a trusted proxy.

Interview prompts: why Lua prevents a missing-expiry race; how to choose a
window; how to avoid untrusted client IP spoofing; what a distributed limiter
does when Redis is unavailable.

## Phase 16 — Memory and key analysis

`SCAN` is incremental and avoids the blocking behavior of production `KEYS`.
`MEMORY USAGE` measures an individual key, `MEMORY STATS` reports allocator and
dataset statistics, `MEMORY DOCTOR` gives Redis heuristics, `OBJECT` exposes
encoding and idle time, and `INFO MEMORY` provides fragmentation and memory
limits. `TYPE` and TTL inspection complete the key view.

The diagnostics service returns a key inspector, memory report, scan results,
and largest-key candidates. It does not claim that a scan is a perfect
point-in-time snapshot: keys can change while the cursor walks the database.

Production concerns include big keys, hot keys, key naming, serialized value
size, allocator fragmentation, `maxmemory`, eviction policy, and sampling
limits. Never run an unbounded key inspection on a busy production instance.

Interview prompts: why `SCAN` can return duplicates; difference between key
memory and dataset memory; why fragmentation can remain after deletion; how
eviction policy affects correctness.

## Phase 17 — Performance benchmarking

The benchmark performs real individual commands, pipelines, MSET/MGET, and Lua
operations. It warms up, measures repeated iterations, computes throughput,
and reports p50/p95/p99 iteration latency. The benchmark intentionally avoids
hardcoded claims and does not check in environment-specific numbers.

Network round trips, batch size, connection setup, serialization, Redis CPU,
payload size, warm-up, and background persistence all influence results. A
pipeline is not automatically faster for every workload, and Lua is useful
when server-side atomicity or reduced round trips justify it.

Interview prompts: why p99 matters; how pipeline batch size affects latency;
when MGET is preferable; why a local benchmark cannot predict a WAN result.

## Phase 18 — Persistence

The persistence topology runs an RDB-only Redis and an AOF Redis on ports 6380
and 6381. RDB snapshots are compact point-in-time files with a loss window
between snapshots. AOF records writes and can use `appendfsync always`,
`everysec`, or `no`, trading durability for throughput. AOF rewrite compacts
history; `BGSAVE` creates a background RDB snapshot; `SAVE` blocks the server.

Use `INFO persistence`, `BGSAVE`, `BGREWRITEAOF`, and container restart to
observe recovery. Both RDB and AOF can be enabled when startup recovery and
operational trade-offs justify it. Volumes and backup verification matter more
than merely setting a flag.

## Phase 19 — Replication

The replication topology has a primary on 6379 and a read-only replica on
6382. Redis performs a handshake, attempts partial synchronization using the
replication offset and backlog, and falls back to a full synchronization when
the backlog is insufficient. Replication is asynchronous, so reads from a
replica can lag.

`REPLICAOF`, `INFO replication`, and `WAIT` are useful demonstrations. The
topology does not include Sentinel and therefore does not claim automatic
failover. A replica is not automatically a linearizable read source.

## Phase 20 — Redis Cluster

The cluster topology starts six nodes, assigns 16384 hash slots, and creates
three replica pairs. Cluster clients follow `MOVED` redirects and temporary
`ASK` redirects during resharding. Keys with the same hash tag, such as
`user:{42}:name` and `user:{42}:role`, share a slot and can participate in
multi-key operations.

Cluster changes application design: multi-key commands must share a slot,
transactions and Lua scripts must use co-located keys, and pipelines may need
per-node routing. The default Spring template in this project is not silently
treated as cluster-aware; configure a cluster connection factory before using
the Java application against the cluster.

## Phase 21 — Testing and failure simulation

Unit validation tests run without Redis. Docker/PowerShell scripts exercise
WATCH conflicts, stale locks, consumer crashes, persistence recovery,
replication state, cluster routing, Redis unavailability, and rate-limit
atomicity. Testcontainers is intentionally not added as a mandatory dependency
so the project remains runnable with the existing Maven/Docker workflow.

## Phase 22 — Docker and production setup

The production-like compose file uses an environment-provided password, a
read-only Redis config mount, persistence, a health check, a memory limit, and
an explicit eviction policy. Do not commit `.env`; use a secret manager or
Docker/Kubernetes secrets for real deployments. Add ACLs, TLS, network policy,
monitoring, logs, backups, restore drills, resource sizing, and graceful
shutdown before calling a deployment production-ready.
