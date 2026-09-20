# RedisLab architecture notes

## Command flow

```text
Redis command
    ↓
Redis server behavior / data structure / durability rule
    ↓
Spring Data Redis abstraction
    ↓
RedisLab service and HTTP/CLI demonstration
```

Examples:

| Redis feature | Command | Spring API | RedisLab implementation |
|---|---|---|---|
| Lock acquire | `SET key token NX PX ttl` | `ValueOperations.setIfAbsent` | `RedisDistributedLockService.tryLock` |
| Safe unlock | `GET` + conditional `DEL` | `RedisScript<Long>` | token-checked Lua script |
| Stream group read | `XREADGROUP` | `StreamOperations.read` | `RedisStreamConsumerGroupService.read` |
| Pending recovery | `XAUTOCLAIM` | `RedisConnection.execute` | RESP decoder in consumer-group service |
| Atomic limiter | `INCR` + `PEXPIRE` | `RedisScript<Long>` | `RedisRateLimitService.atomicFixedWindow` |
| Key analysis | `SCAN`, `MEMORY`, `OBJECT` | `RedisConnection`/`StringRedisTemplate` | `RedisDiagnosticsService` |

## Failure-aware design

- Acknowledge stream messages only after the business side effect completes.
- Use a token for lock ownership; never release with unconditional `DEL`.
- Set lock TTLs and renew only while the owner still holds the token.
- Use atomic Lua for rate-limit increment plus expiry.
- Use `SCAN`, not `KEYS`, for broad key inspection.
- Treat benchmark output as measurements tied to one environment.
- Keep replication and cluster topology in Docker where Redis actually performs
  synchronization, slot routing, and failover behavior.
