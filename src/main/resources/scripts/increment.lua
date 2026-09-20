local current = redis.call('GET', KEYS[1])
if not current then
    current = 0
end

local amount = tonumber(ARGV[1])

if not amount then
    return redis.error_reply(
            'amount must be a number'
    )
end

local updated = tonumber(current) + amount

redis.call(
        'SET',
        KEYS[1],
        updated
)

return updated