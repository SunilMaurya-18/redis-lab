# Benchmarking methodology

RedisLab benchmarks are deliberately modest and reproducible rather than
marketing claims.

Each run includes a configurable warm-up, a measured iteration count, a batch
size, elapsed time, operations per second, and p50/p95/p99 iteration latency.

Compared operations:

- individual SET/GET commands: exposes network round trips
- one pipeline containing SET/GET commands: reduces round trips
- MSET/MGET: compares native batching with pipelining
- Lua increment: measures one atomic server-side script call

For useful comparisons:

1. Keep Redis version, host, payload size, connection pool, and batch size fixed.
2. Run several repetitions and record variance.
3. Warm the connection and server before measuring.
4. Compare percentiles, not only average latency.
5. Separate client serialization and network time from server execution time.
6. Do not compare a local Docker Redis result with a remote production result.

No benchmark results are checked into the repository because they would become
false precision when the environment changes.
