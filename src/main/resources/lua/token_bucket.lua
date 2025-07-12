-- KEYS[1] = Redis key (rate:<client>:<api>)
-- ARGV[1] = max_tokens (e.g., 10)
-- ARGV[2] = refill_rate (tokens added per interval, e.g., 1)
-- ARGV[3] = refill_interval_ms (e.g., 1000ms = 1 second)
-- ARGV[4] = current_timestamp in ms (from Java)
-- ARGV[5] = grace_limit (additional burst tokens that don't refill)

local key = KEYS[1]
local max_tokens = tonumber(ARGV[1])
local refill_rate = tonumber(ARGV[2])
local interval_ms = tonumber(ARGV[3])
local now = tonumber(ARGV[4]) -- Use the passed timestamp!
local grace_limit = tonumber(ARGV[5]) or 0

redis.log(redis.LOG_NOTICE, "[TokenBucket] Start: key=" .. tostring(key) .. ", max_tokens=" .. tostring(max_tokens) .. ", refill_rate=" .. tostring(refill_rate) .. ", interval_ms=" .. tostring(interval_ms) .. ", now=" .. tostring(now) .. ", grace_limit=" .. tostring(grace_limit))

-- Defensive check (Optional)
if not interval_ms then
  redis.log(redis.LOG_WARNING, "[TokenBucket] interval_ms is nil for key: " .. tostring(key))
  return redis.error_reply("interval_ms is nil")
end

-- Read existing values or default
local data = redis.call("HMGET", key, "tokens", "grace_tokens", "last_refill")
local tokens = tonumber(data[1]) or max_tokens
local grace_tokens = tonumber(data[2]) or grace_limit
local last_refill = tonumber(data[3]) or now

redis.log(redis.LOG_NOTICE, "[TokenBucket] Before refill: tokens=" .. tostring(tokens) .. ", grace_tokens=" .. tostring(grace_tokens) .. ", last_refill=" .. tostring(last_refill))

-- Calculate how much to refill (only normal tokens refill, grace tokens don't)
local delta = now - last_refill
local refill = math.floor(delta / interval_ms) * refill_rate
tokens = math.min(max_tokens, tokens + refill)

redis.log(redis.LOG_NOTICE, "[TokenBucket] After refill: tokens=" .. tostring(tokens) .. ", refill=" .. tostring(refill) .. ", delta=" .. tostring(delta))

-- Should we allow the request?
local allowed = 0
if tokens > 0 then
  -- Consume 1 normal token
  tokens = tokens - 1
  allowed = 1
  redis.log(redis.LOG_NOTICE, "[TokenBucket] Request allowed using normal token. tokens left=" .. tostring(tokens) .. ", grace_tokens=" .. tostring(grace_tokens))
elseif grace_tokens > 0 then
  -- Consume 1 grace token
  grace_tokens = grace_tokens - 1
  allowed = 1
  redis.log(redis.LOG_NOTICE, "[TokenBucket] Request allowed using grace token. tokens left=" .. tostring(tokens) .. ", grace_tokens left=" .. tostring(grace_tokens))
else
  redis.log(redis.LOG_NOTICE, "[TokenBucket] Request throttled. tokens left=" .. tostring(tokens) .. ", grace_tokens left=" .. tostring(grace_tokens))
end

-- Store updated state
redis.call("HMSET", key, "tokens", tokens, "grace_tokens", grace_tokens, "last_refill", now)
redis.call("PEXPIRE", key, interval_ms * 2)

redis.log(redis.LOG_NOTICE, "[TokenBucket] State updated: key=" .. tostring(key) .. ", tokens=" .. tostring(tokens) .. ", grace_tokens=" .. tostring(grace_tokens) .. ", last_refill=" .. tostring(now))

-- Return 1 if allowed, 0 if throttled
return allowed
