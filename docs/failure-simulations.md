# Failure simulations

Run `scripts/failure-simulation.ps1` against the development container.

## Scenarios

- `WATCH` conflict: two clients watch and update the same key; one `EXEC`
  returns a null transaction result.
- Stale lock: a lock expires and another owner acquires it; the old owner must
  not be able to release the new owner's token.
- Stream consumer crash: a consumer reads without `XACK`; `XPENDING` shows the
  owner and delivery count; `XAUTOCLAIM` transfers the idle entry.
- Atomic limiter: Lua increments and expiry happen in one server-side action.
- Redis unavailable: stop the container, observe application connection
  failure, restart it, and verify that the application can reconnect.
- Persistence: write data, restart RDB/AOF containers, and compare survival.
- Replication: write to the primary and observe the read-only replica; no
  automatic promotion is claimed.
- Cluster: use `redis-cli -c`, demonstrate slot routing and hash tags.

These scenarios test behavior and recovery paths, not just HTTP status codes.
