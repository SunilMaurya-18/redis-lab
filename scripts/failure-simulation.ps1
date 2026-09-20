param(
    [string]$Container = "redis-lab"
)

$ErrorActionPreference = "Stop"

function Invoke-Redis {
    param([Parameter(Mandatory)][string[]]$Arguments)
    & docker exec $Container redis-cli @Arguments
    if ($LASTEXITCODE -ne 0) {
        throw "redis-cli failed with exit code $LASTEXITCODE"
    }
}

Write-Host "[1/5] WATCH conflict"
Invoke-Redis @("SET", "failure:counter", "0")
Write-Host "Open two redis-cli sessions and run WATCH failure:counter / GET / MULTI / INCRBY failure:counter 1 / EXEC in both. One EXEC must return nil after the other changes the key."

Write-Host "[2/5] stale lock"
Invoke-Redis @("SET", "failure:lock", "owner-a", "NX", "PX", "1000")
Start-Sleep -Milliseconds 1200
Invoke-Redis @("SET", "failure:lock", "owner-b", "NX", "PX", "1000")

Write-Host "[3/5] consumer crash and recovery"
Invoke-Redis @("DEL", "failure:stream")
Invoke-Redis @("XGROUP", "CREATE", "failure:stream", "workers", "0-0", "MKSTREAM")
$id = Invoke-Redis @("XADD", "failure:stream", "*", "event", "crash-demo") | Select-Object -Last 1
Invoke-Redis @("XREADGROUP", "GROUP", "workers", "crashed-consumer", "COUNT", "1", "STREAMS", "failure:stream", ">")
Start-Sleep -Milliseconds 1500
Invoke-Redis @("XAUTOCLAIM", "failure:stream", "workers", "recovery-consumer", "1000", "0-0", "COUNT", "10")

Write-Host "[4/5] rate-limit atomicity"
Invoke-Redis @("DEL", "redislab:ratelimit:failure-test")
1..5 | ForEach-Object { Invoke-Redis @("EVAL", "local c=redis.call('incr',KEYS[1]); if c==1 then redis.call('pexpire',KEYS[1],60000) end; return c", "1", "redislab:ratelimit:failure-test") }

Write-Host "[5/5] unavailable Redis"
Write-Host "Stop the container, call the application, record the connection exception, restart Redis, and verify recovery. This script intentionally does not stop the shared container automatically."
