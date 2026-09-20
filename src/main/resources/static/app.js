const state = {
    commandCount: 0,
    history: [],
    historyIndex: -1,
    lastResponse: null,
    lockToken: ""
};

const $ = (selector) => document.querySelector(selector);
const $$ = (selector) => [...document.querySelectorAll(selector)];

function query(params = {}) {
    const search = new URLSearchParams();
    Object.entries(params).forEach(([key, value]) => {
        if (value !== undefined && value !== null && value !== "") search.append(key, String(value));
    });
    const text = search.toString();
    return text ? `?${text}` : "";
}

async function api(path, options = {}) {
    const response = await fetch(path, {
        headers: options.body ? { "Content-Type": "application/json" } : undefined,
        ...options
    });
    const text = await response.text();
    let data = text;
    try { data = text ? JSON.parse(text) : null; } catch (_) { /* plain text response */ }
    if (!response.ok) {
        const message = typeof data === "string" ? data : (data?.message || JSON.stringify(data));
        throw new Error(`${response.status} ${response.statusText}: ${message}`);
    }
    return data;
}

function showToast(message, error = false) {
    const toast = $("#toast");
    toast.textContent = message;
    toast.classList.toggle("error", error);
    toast.classList.add("show");
    clearTimeout(showToast.timer);
    showToast.timer = setTimeout(() => toast.classList.remove("show"), 2800);
}

function pretty(value) {
    if (typeof value === "string") return value;
    return JSON.stringify(value, null, 2);
}

function updateResponse(data) {
    state.lastResponse = data;
    $("#responseViewer").textContent = pretty(data);
    const text = pretty(data);
    ["#memoryResult", "#scanResult"].forEach((selector) => {
        const element = $(selector);
        if (element && element.dataset.followResponse === "true") element.textContent = text;
    });
}

function appendConsole(kind, text) {
    const output = $("#consoleOutput");
    const line = document.createElement("div");
    line.className = `console-line ${kind}-line`;
    line.textContent = text;
    output.appendChild(line);
    output.scrollTop = output.scrollHeight;
}

function incrementCommands() {
    state.commandCount += 1;
    $("#metricCommands").textContent = String(state.commandCount).padStart(2, "0");
}

async function runApiCommand(command, operation) {
    appendConsole("command", `redis> ${command}`);
    $("#lastAction").textContent = operation || command;
    try {
        const data = await operation();
        incrementCommands();
        updateResponse(data);
        appendConsole("result", pretty(data));
        showToast("Operation completed");
        return data;
    } catch (error) {
        incrementCommands();
        appendConsole("error", `✕ ${error.message}`);
        updateResponse({ error: error.message });
        showToast(error.message, true);
        throw error;
    }
}

function tokens(input) {
    const result = [];
    const pattern = /"([^"\\]*(?:\\.[^"\\]*)*)"|'([^']*)'|(\S+)/g;
    let match;
    while ((match = pattern.exec(input)) !== null) result.push(match[1] ?? match[2] ?? match[3]);
    return result;
}

function helpText() {
    return [
        "RedisLab commands:",
        "  PING | SET key value | GET key | DEL key | TTL key",
        "  HSET key field value | HGET key field | HGETALL key",
        "  LPUSH/RPUSH key value | LPOP/RPOP key | LRANGE key start end",
        "  SADD key member | SMEMBERS key | SISMEMBER key member",
        "  ZADD key score member | ZRANGE key start end | ZCARD key",
        "  XADD stream eventType payload | XREAD stream group consumer",
        "  XGROUP CREATE stream group | XPENDING stream group | XACK stream group recordId",
        "  LOCK TRY key owner ttlMs | LOCK RELEASE key token | LOCK TTL key",
        "  EXPIRE key seconds | PERSIST key | PUBLISH channel message",
        "  TXSET key1 value1 key2 value2 | WATCHSET key expected newValue",
        "  LUA INCR key amount | LUA CAS key expected newValue | LUA ECHO message",
        "  RATE LIMIT scope limit windowMs | MEMORY | SCAN pattern | ANALYZE pattern",
        "  PIPELINE prefix count | BENCHMARK | CLEAR"
    ].join("\n");
}

async function executeCommand(rawInput, options = {}) {
    const command = rawInput.trim();
    if (!command) return;
    if (!options.fromQuickAction) {
        state.history = [command, ...state.history.filter((item) => item !== command)].slice(0, 30);
        state.historyIndex = -1;
    }
    const args = tokens(command);
    const verb = (args.shift() || "").toUpperCase();
    const arg = (index, fallback = "") => args[index] ?? fallback;
    const requireArgs = (count, usage) => {
        if (args.length < count) throw new Error(`Usage: ${usage}`);
    };
    const get = (path) => api(path);
    const post = (path) => api(path, { method: "POST" });
    const del = (path) => api(path, { method: "DELETE" });

    if (verb === "HELP" || verb === "?") {
        appendConsole("command", `redis> ${command}`);
        appendConsole("result", helpText());
        updateResponse({ help: helpText().split("\n") });
        return;
    }
    if (verb === "CLEAR") {
        $("#consoleOutput").innerHTML = "";
        return;
    }

    let path;
    let method = "GET";
    let operation = "";
    switch (verb) {
        case "PING": path = "/api/redis/ping"; operation = "Connection ping"; break;
        case "SET":
            requireArgs(2, "SET key value"); path = `/api/redis/strings${query({ key: arg(0), value: args.slice(1).join(" ") })}`; method = "POST"; operation = "SET string"; break;
        case "GET":
            requireArgs(1, "GET key"); path = `/api/redis/strings${query({ key: arg(0) })}`; operation = "GET string"; break;
        case "DEL":
            requireArgs(1, "DEL key"); path = `/api/redis/strings${query({ key: arg(0) })}`; method = "DELETE"; operation = "Delete string"; break;
        case "TTL":
            requireArgs(1, "TTL key"); path = `/api/redis/strings/ttl${query({ key: arg(0) })}`; operation = "Read TTL"; break;
        case "INCR":
            requireArgs(1, "INCR key"); path = `/api/redis/strings/increment${query({ key: arg(0) })}`; method = "POST"; operation = "INCR string"; break;
        case "EXPIRE":
            requireArgs(2, "EXPIRE key seconds"); path = `/api/redis/expiration/expire${query({ key: arg(0), seconds: arg(1) })}`; method = "POST"; operation = "EXPIRE key"; break;
        case "PERSIST":
            requireArgs(1, "PERSIST key"); path = `/api/redis/expiration/persist${query({ key: arg(0) })}`; method = "POST"; operation = "PERSIST key"; break;
        case "HSET":
            requireArgs(3, "HSET key field value"); path = `/api/redis/hashes/field${query({ key: arg(0), field: arg(1), value: args.slice(2).join(" ") })}`; method = "POST"; operation = "HSET hash field"; break;
        case "HGET":
            requireArgs(2, "HGET key field"); path = `/api/redis/hashes/field${query({ key: arg(0), field: arg(1) })}`; operation = "HGET hash field"; break;
        case "HGETALL":
            requireArgs(1, "HGETALL key"); path = `/api/redis/hashes${query({ key: arg(0) })}`; operation = "HGETALL hash"; break;
        case "LPUSH": case "RPUSH": {
            requireArgs(2, `${verb} key value`); path = `/api/redis/lists/${verb === "LPUSH" ? "left" : "right"}${query({ key: arg(0), value: args.slice(1).join(" ") })}`; method = "POST"; operation = `${verb} list`; break;
        }
        case "LPOP": case "RPOP":
            requireArgs(1, `${verb} key`); path = `/api/redis/lists/pop/${verb === "LPOP" ? "left" : "right"}${query({ key: arg(0) })}`; method = "POST"; operation = `${verb} list`; break;
        case "LRANGE":
            requireArgs(1, "LRANGE key start end"); path = `/api/redis/lists/range${query({ key: arg(0), start: arg(1, "0"), end: arg(2, "-1") })}`; operation = "LRANGE list"; break;
        case "LLEN":
            requireArgs(1, "LLEN key"); path = `/api/redis/lists/length${query({ key: arg(0) })}`; operation = "LLEN list"; break;
        case "SADD":
            requireArgs(2, "SADD key member"); path = `/api/redis/sets/add${query({ key: arg(0), value: args.slice(1).join(" ") })}`; method = "POST"; operation = "SADD set"; break;
        case "SMEMBERS":
            requireArgs(1, "SMEMBERS key"); path = `/api/redis/sets${query({ key: arg(0) })}`; operation = "SMEMBERS set"; break;
        case "SISMEMBER":
            requireArgs(2, "SISMEMBER key member"); path = `/api/redis/sets/member${query({ key: arg(0), value: args.slice(1).join(" ") })}`; operation = "SISMEMBER set"; break;
        case "SCARD":
            requireArgs(1, "SCARD key"); path = `/api/redis/sets/size${query({ key: arg(0) })}`; operation = "SCARD set"; break;
        case "ZADD":
            requireArgs(3, "ZADD key score member"); path = `/api/redis/sorted-sets/add${query({ key: arg(0), score: arg(1), member: args.slice(2).join(" ") })}`; method = "POST"; operation = "ZADD sorted set"; break;
        case "ZRANGE":
            requireArgs(1, "ZRANGE key start end"); path = `/api/redis/sorted-sets${query({ key: arg(0), start: arg(1, "0"), end: arg(2, "-1") })}`; operation = "ZRANGE sorted set"; break;
        case "ZCARD":
            requireArgs(1, "ZCARD key"); path = `/api/redis/sorted-sets/size${query({ key: arg(0) })}`; operation = "ZCARD sorted set"; break;
        case "XADD":
            requireArgs(3, "XADD stream eventType payload"); path = `/api/v1/redis/streams/add${query({ stream: arg(0), eventType: arg(1), payload: args.slice(2).join(" ") })}`; method = "POST"; operation = "XADD stream event"; break;
        case "XREAD":
            requireArgs(3, "XREAD stream group consumer"); path = `/api/v1/redis/stream-groups/read${query({ stream: arg(0), group: arg(1), consumer: arg(2), offset: ">", count: 10 })}`; operation = "XREADGROUP stream"; break;
        case "XPENDING":
            requireArgs(2, "XPENDING stream group"); path = `/api/v1/redis/stream-groups/pending/summary${query({ stream: arg(0), group: arg(1) })}`; operation = "XPENDING summary"; break;
        case "XACK":
            requireArgs(3, "XACK stream group recordId"); path = `/api/v1/redis/stream-groups/ack${query({ stream: arg(0), group: arg(1), recordIds: arg(2) })}`; method = "POST"; operation = "XACK stream message"; break;
        case "XGROUP": {
            if ((arg(0) || "").toUpperCase() !== "CREATE") throw new Error("Usage: XGROUP CREATE stream group");
            requireArgs(3, "XGROUP CREATE stream group"); path = `/api/v1/redis/stream-groups/create${query({ stream: arg(1), group: arg(2), startId: "0-0" })}`; method = "POST"; operation = "Create stream group"; break;
        }
        case "XGROUPS":
            requireArgs(1, "XGROUPS stream"); path = `/api/v1/redis/stream-groups/groups${query({ stream: arg(0) })}`; operation = "List stream groups"; break;
        case "XCONSUMERS":
            requireArgs(2, "XCONSUMERS stream group"); path = `/api/v1/redis/stream-groups/consumers${query({ stream: arg(0), group: arg(1) })}`; operation = "List stream consumers"; break;
        case "XAUTOCLAIM":
            requireArgs(3, "XAUTOCLAIM stream group consumer"); path = `/api/v1/redis/stream-groups/autoclaim${query({ stream: arg(0), group: arg(1), newConsumer: arg(2), minIdleMs: arg(3, "1000"), startId: "0-0", count: 100 })}`; method = "POST"; operation = "Auto-claim idle messages"; break;
        case "PUBLISH":
            requireArgs(2, "PUBLISH channel message"); path = `/api/v1/redis/pubsub/publish${query({ channel: arg(0), message: args.slice(1).join(" ") })}`; method = "POST"; operation = "Publish message"; break;
        case "TXSET":
            requireArgs(4, "TXSET key1 value1 key2 value2"); path = `/api/redis/transactions/execute${query({ key1: arg(0), value1: arg(1), key2: arg(2), value2: args.slice(3).join(" ") })}`; method = "POST"; operation = "MULTI EXEC transaction"; break;
        case "WATCHSET":
            requireArgs(3, "WATCHSET key expectedValue newValue"); path = `/api/redis/transactions/watch-set${query({ key: arg(0), expectedValue: arg(1), newValue: args.slice(2).join(" ") })}`; method = "POST"; operation = "WATCH transaction"; break;
        case "LUA": {
            const action = (arg(0) || "").toUpperCase();
            if (action === "INCR") { requireArgs(3, "LUA INCR key amount"); path = `/api/v1/redis/lua/increment${query({ key: arg(1), amount: arg(2) })}`; method = "POST"; operation = "Lua increment"; }
            else if (action === "CAS") { requireArgs(4, "LUA CAS key expectedValue newValue"); path = `/api/v1/redis/lua/check-and-set${query({ key: arg(1), expectedValue: arg(2), newValue: args.slice(3).join(" ") })}`; method = "POST"; operation = "Lua check and set"; }
            else if (action === "ECHO") { requireArgs(2, "LUA ECHO message"); path = `/api/v1/redis/lua/echo${query({ message: args.slice(1).join(" ") })}`; operation = "Lua echo"; }
            else throw new Error("LUA actions: INCR, CAS, ECHO");
            break;
        }
        case "MEMORY": path = "/api/v1/redis/diagnostics/memory"; operation = "Redis memory report"; break;
        case "SCAN": path = `/api/v1/redis/diagnostics/scan${query({ pattern: arg(0, "redislab:*"), count: 100 })}`; operation = "SCAN keys"; break;
        case "ANALYZE": path = `/api/v1/redis/diagnostics/analyze${query({ pattern: arg(0, "redislab:*"), scanCount: 1000, largestCount: 20 })}`; operation = "Analyze keys"; break;
        case "BENCHMARK": path = `/api/v1/redis/benchmarks/run${query({ warmupIterations: 10, measuredIterations: 50, batchSize: 20 })}`; method = "POST"; operation = "Run benchmark"; break;
        case "PIPELINE": requireArgs(2, "PIPELINE prefix count"); path = `/api/v1/redis/pipeline/set${query({ prefix: arg(0), count: arg(1) })}`; method = "POST"; operation = "Pipeline batch"; break;
        case "LOCK": {
            const action = (arg(0) || "").toUpperCase();
            if (action === "TRY") { requireArgs(4, "LOCK TRY key owner ttlMs"); path = `/api/v1/redis/locks/try${query({ key: arg(1), owner: arg(2), ttlMs: arg(3) })}`; method = "POST"; operation = "Try distributed lock"; }
            else if (action === "RELEASE") { requireArgs(3, "LOCK RELEASE key token"); path = `/api/v1/redis/locks/release${query({ key: arg(1), token: arg(2) })}`; method = "POST"; operation = "Release distributed lock"; }
            else if (action === "RENEW") { requireArgs(4, "LOCK RENEW key token ttlMs"); path = `/api/v1/redis/locks/renew${query({ key: arg(1), token: arg(2), ttlMs: arg(3) })}`; method = "POST"; operation = "Renew distributed lock"; }
            else if (action === "TTL") { requireArgs(2, "LOCK TTL key"); path = `/api/v1/redis/locks/ttl${query({ key: arg(1) })}`; operation = "Lock TTL"; }
            else throw new Error("LOCK actions: TRY, RELEASE, RENEW, TTL");
            break;
        }
        case "RATE": {
            if ((arg(0) || "").toUpperCase() !== "LIMIT") throw new Error("Usage: RATE LIMIT scope limit windowMs");
            requireArgs(4, "RATE LIMIT scope limit windowMs"); path = `/api/v1/redis/rate-limits/atomic${query({ scope: arg(1), limit: arg(2), windowMs: arg(3) })}`; method = "POST"; operation = "Atomic rate limit"; break;
        }
        default: throw new Error(`Unknown command: ${verb}. Type HELP for examples.`);
    }
    await runApiCommand(command, () => api(path, method === "GET" ? {} : { method }));
}

function navigate(section) {
    $$(".nav-item").forEach((item) => item.classList.toggle("active", item.dataset.section === section));
    $$(".section-view").forEach((view) => view.classList.toggle("active", view.id === `section-${section}`));
    const title = $(`[data-section="${section}"]`);
    $("#pageTitle").textContent = title?.textContent?.trim() || "Overview";
    window.scrollTo({ top: 0, behavior: "smooth" });
}

function setResult(selector, data, error = false) {
    const element = $(selector);
    element.textContent = pretty(data);
    element.classList.toggle("success", !error);
    element.classList.toggle("error", error);
}

async function refreshStatus() {
    try {
        const ping = await api("/api/redis/ping");
        $("#sidebarStatus").textContent = "Redis connected";
        $("#sidebarStatusDot").classList.remove("offline");
        $("#metricConnection").textContent = "ONLINE";
        $("#metricConnection").style.color = "var(--green)";
        $("#metricConnectionNote").textContent = ping.response || "PONG";
        try {
            const memory = await api("/api/v1/redis/diagnostics/memory");
            const usage = memory.memoryUsage || {};
            const used = usage.used_memory_human || usage.usedMemoryHuman || usage.used_memory || usage.usedMemory || "available";
            $("#metricMemory").textContent = String(used).replace(" ", "");
            $("#metricMemoryNote").textContent = `${memory.databaseSize ?? 0} keys · Redis INFO memory`;
        } catch (_) { $("#metricMemoryNote").textContent = "Memory endpoint unavailable"; }
        showToast("Redis connection is healthy");
    } catch (error) {
        $("#sidebarStatus").textContent = "Redis offline";
        $("#sidebarStatusDot").classList.add("offline");
        $("#metricConnection").textContent = "OFFLINE";
        $("#metricConnection").style.color = "var(--red)";
        $("#metricConnectionNote").textContent = "Start Docker and Spring Boot";
        showToast(error.message, true);
    }
}

function setupDataBuilder() {
    const operations = {
        string: [["set", "SET"], ["get", "GET"], ["increment", "INCR"], ["delete", "DEL"]],
        hash: [["hset", "HSET"], ["hget", "HGET"], ["hgetall", "HGETALL"], ["delete", "DEL"]],
        list: [["lpush", "LPUSH"], ["rpush", "RPUSH"], ["lrange", "LRANGE"], ["lpop", "LPOP"], ["length", "LLEN"], ["delete", "DEL"]],
        set: [["sadd", "SADD"], ["smembers", "SMEMBERS"], ["ismember", "SISMEMBER"], ["size", "SCARD"], ["delete", "DEL"]],
        zset: [["zadd", "ZADD"], ["range", "ZRANGE"], ["size", "ZCARD"], ["delete", "DEL"]]
    };
    const structure = $("#dataStructure");
    const operation = $("#dataOperation");
    const fieldLabel = $("#dataFieldLabel");
    const extraLabel = $("#dataExtraLabel");
    const valueInput = $("#dataValue");
    const extraInput = $("#dataExtra");
    function renderOperations() {
        const key = structure.value;
        operation.innerHTML = operations[key].map(([value, label]) => `<option value="${value}">${label}</option>`).join("");
        $("#operationChip").textContent = key === "zset" ? "SORTED SET" : key.toUpperCase();
        const isHash = key === "hash";
        const isZset = key === "zset";
        fieldLabel.firstChild.textContent = isHash ? "Value" : isZset ? "Member" : "Value";
        valueInput.placeholder = isHash ? "field value" : isZset ? "member name" : "value";
        extraLabel.style.display = isHash || isZset ? "grid" : "none";
        extraLabel.firstChild.textContent = isHash ? "Field" : "Score";
        extraInput.placeholder = isHash ? "field name" : isZset ? "0" : "field / score / end";
        updateHelp();
    }
    function updateHelp() {
        const selected = operation.value;
        const label = operation.options[operation.selectedIndex]?.text || "operation";
        $("#dataHelp").textContent = `Execute ${label} on the selected structure.`;
        if (selected === "lrange") { fieldLabel.firstChild.textContent = "Start"; valueInput.value = "0"; extraLabel.style.display = "grid"; extraLabel.firstChild.textContent = "End"; extraInput.value = "-1"; }
        if (selected === "delete" || selected === "get" || selected === "hgetall" || selected === "smembers" || selected === "size" || selected === "range" || selected === "length" || selected === "lpop") extraLabel.style.display = "none";
    }
    structure.addEventListener("change", renderOperations);
    operation.addEventListener("change", updateHelp);
    $("#runDataBtn").addEventListener("click", async () => {
        const type = structure.value;
        const op = operation.value;
        const key = $("#dataKey").value.trim();
        const value = valueInput.value;
        const extra = extraInput.value;
        let command;
        if (type === "string") command = { set: `SET ${key} ${value}`, get: `GET ${key}`, increment: `INCR ${key}`, delete: `DEL ${key}` }[op];
        if (type === "hash") command = { hset: `HSET ${key} ${extra} ${value}`, hget: `HGET ${key} ${extra}`, hgetall: `HGETALL ${key}`, delete: `DEL ${key}` }[op];
        if (type === "list") command = { lpush: `LPUSH ${key} ${value}`, rpush: `RPUSH ${key} ${value}`, lrange: `LRANGE ${key} ${value || 0} ${extra || -1}`, lpop: `LPOP ${key}`, length: `LLEN ${key}`, delete: `DEL ${key}` }[op];
        if (type === "set") command = { sadd: `SADD ${key} ${value}`, smembers: `SMEMBERS ${key}`, ismember: `SISMEMBER ${key} ${value}`, size: `SCARD ${key}`, delete: `DEL ${key}` }[op];
        if (type === "zset") command = { zadd: `ZADD ${key} ${extra || 0} ${value}`, range: `ZRANGE ${key} 0 -1`, size: `ZCARD ${key}`, delete: `DEL ${key}` }[op];
        try { await executeCommand(command); } catch (_) { /* already shown in console */ }
    });
    renderOperations();
}

function setupStreamBuilder() {
    $("#runStreamBtn").addEventListener("click", async () => {
        const action = $("#streamOperation").value;
        const stream = $("#streamKey").value.trim();
        const group = $("#streamGroup").value.trim();
        const consumer = $("#streamConsumer").value.trim();
        const type = $("#streamEventType").value.trim();
        const payload = $("#streamPayload").value.trim();
        const commands = {
            create: `XGROUP CREATE ${stream} ${group}`,
            add: `XADD ${stream} ${type} ${payload}`,
            read: `XREAD ${stream} ${group} ${consumer}`,
            pending: `XPENDING ${stream} ${group}`,
            ack: `XACK ${stream} ${group} ${payload}`,
            groups: `XGROUPS ${stream}`,
            consumers: `XCONSUMERS ${stream} ${group}`
        };
        if (action === "autoclaim") {
            try {
                await runApiCommand("XAUTOCLAIM", () => api(`/api/v1/redis/stream-groups/autoclaim${query({ stream, group, newConsumer: consumer, minIdleMs: 1000, startId: "0-0", count: 100 })}`, { method: "POST" }));
            } catch (_) { /* shown */ }
            return;
        }
        if (action === "create") {
            try { await runApiCommand("XGROUP CREATE", () => api(`/api/v1/redis/stream-groups/create${query({ stream, group, startId: "0-0" })}`, { method: "POST" })); } catch (_) { /* shown */ }
            return;
        }
        if (action === "groups") {
            try { await runApiCommand("XINFO GROUPS", () => api(`/api/v1/redis/stream-groups/groups${query({ stream })}`)); } catch (_) { /* shown */ }
            return;
        }
        if (action === "consumers") {
            try { await runApiCommand("XINFO CONSUMERS", () => api(`/api/v1/redis/stream-groups/consumers${query({ stream, group })}`)); } catch (_) { /* shown */ }
            return;
        }
        try { await executeCommand(commands[action]); } catch (_) { /* shown */ }
    });
}

function setupSafety() {
    $$("[data-lock-action]").forEach((button) => button.addEventListener("click", async () => {
        const action = button.dataset.lockAction;
        const key = $("#lockKey").value.trim();
        const owner = $("#lockOwner").value.trim();
        const ttlMs = $("#lockTtl").value;
        const token = $("#lockToken").value.trim() || state.lockToken;
        let path;
        let method = "GET";
        if (action === "try") { path = `/api/v1/redis/locks/try${query({ key, owner, ttlMs })}`; method = "POST"; }
        if (action === "ttl") path = `/api/v1/redis/locks/ttl${query({ key })}`;
        if (action === "release") { path = `/api/v1/redis/locks/release${query({ key, token })}`; method = "POST"; }
        if (action === "renew") { path = `/api/v1/redis/locks/renew${query({ key, token, ttlMs })}`; method = "POST"; }
        try {
            const data = await runApiCommand(`LOCK ${action.toUpperCase()} ${key}`, () => api(path, method === "POST" ? { method } : {}));
            if (data?.token) { state.lockToken = data.token; $("#lockToken").value = data.token; }
            setResult("#lockResult", data);
        } catch (error) { setResult("#lockResult", { error: error.message }, true); }
    }));
    $("#runRateBtn").addEventListener("click", () => consumeRate());
    $("#burstRateBtn").addEventListener("click", async () => { for (let i = 0; i < 6; i += 1) await consumeRate(); });
    async function consumeRate() {
        const scope = $("#rateScope").value.trim();
        try {
            const data = await runApiCommand("RATE LIMIT", () => api(`/api/v1/redis/rate-limits/atomic${query({ scope, limit: $("#rateLimit").value, windowMs: $("#rateWindow").value })}`, { method: "POST" }));
            setResult("#rateResult", data);
        } catch (error) { setResult("#rateResult", { error: error.message }, true); }
    }
}

function setupDiagnostics() {
    $("#runMemoryBtn").addEventListener("click", async () => {
        try { const data = await runApiCommand("MEMORY", () => api("/api/v1/redis/diagnostics/memory")); setResult("#memoryResult", data); } catch (error) { setResult("#memoryResult", { error: error.message }, true); }
    });
    $("#runScanBtn").addEventListener("click", async () => {
        try { const data = await runApiCommand("SCAN", () => api(`/api/v1/redis/diagnostics/scan${query({ pattern: $("#scanPattern").value, count: 100 })}`)); setResult("#scanResult", data); } catch (error) { setResult("#scanResult", { error: error.message }, true); }
    });
    $("#runAnalyzeBtn").addEventListener("click", async () => {
        try { const data = await runApiCommand("ANALYZE", () => api(`/api/v1/redis/diagnostics/analyze${query({ pattern: $("#scanPattern").value, scanCount: 1000, largestCount: 20 })}`)); setResult("#scanResult", data); } catch (error) { setResult("#scanResult", { error: error.message }, true); }
    });
}

function setupBenchmarks() {
    $("#runBenchmarkBtn").addEventListener("click", async () => {
        const params = { keyPrefix: $("#benchmarkPrefix").value, warmupIterations: $("#benchmarkWarmup").value, measuredIterations: $("#benchmarkMeasured").value, batchSize: $("#benchmarkBatch").value };
        try {
            const data = await runApiCommand("BENCHMARK", () => api(`/api/v1/redis/benchmarks/run${query(params)}`, { method: "POST" }));
            renderBenchmark(data);
        } catch (_) { /* shown */ }
    });
}

function renderBenchmark(data) {
    const source = data?.results || data?.benchmarks || data;
    const entries = Array.isArray(source)
        ? source.map((value, index) => [value?.operation || `operation-${index + 1}`, value])
        : source && typeof source === "object" ? Object.entries(source) : [];
    const cards = entries.filter(([, value]) => value && typeof value === "object").slice(0, 8).map(([name, value]) => {
        const p50 = value.p50Ms ?? value.p50Micros ?? value.p50 ?? value.medianMicros ?? value.averageMicros ?? value.latencyMicros;
        const p95 = value.p95Ms ?? value.p95Micros ?? value.p95;
        const p99 = value.p99Ms ?? value.p99Micros ?? value.p99;
        const unit = value.p50Ms !== undefined ? "ms" : "µs";
        return `<article class="benchmark-card"><div class="metric-label">${String(name).replaceAll("_", " ").toUpperCase()}</div><div class="metric-value">${p50 === undefined ? "ready" : `${p50} ${unit}`}</div><div class="metric-note">p50${p95 === undefined ? "" : ` · p95 ${p95} ${unit}`}${p99 === undefined ? "" : ` · p99 ${p99} ${unit}`}</div></article>`;
    }).join("");
    $("#benchmarkResults").innerHTML = cards || `<article class="panel response-panel"><div class="panel-heading"><h3>Benchmark response</h3></div><pre>${escapeHtml(pretty(data))}</pre></article>`;
}

function escapeHtml(value) { return String(value).replace(/[&<>"']/g, (char) => ({ "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#039;" }[char])); }

function setupConsole() {
    $("#commandForm").addEventListener("submit", async (event) => { event.preventDefault(); const input = $("#commandInput"); const value = input.value; input.value = ""; try { await executeCommand(value); } catch (_) { /* shown in output */ } });
    $("#commandInput").addEventListener("keydown", (event) => {
        if (event.key === "ArrowUp") { event.preventDefault(); if (state.history.length) { state.historyIndex = Math.min(state.historyIndex + 1, state.history.length - 1); event.currentTarget.value = state.history[state.historyIndex]; } }
        if (event.key === "ArrowDown") { event.preventDefault(); state.historyIndex = Math.max(state.historyIndex - 1, -1); event.currentTarget.value = state.historyIndex === -1 ? "" : state.history[state.historyIndex]; }
    });
    $$("[data-fill]").forEach((button) => button.addEventListener("click", () => { navigate("console"); $("#commandInput").value = button.dataset.fill; $("#commandInput").focus(); }));
    $("#copyOutputBtn").addEventListener("click", async () => { await navigator.clipboard?.writeText($("#consoleOutput").innerText); showToast("Console output copied"); });
    $("#copyResponseBtn").addEventListener("click", async () => { await navigator.clipboard?.writeText($("#responseViewer").innerText); showToast("JSON response copied"); });
}

function setupNavigation() {
    $$("[data-section]").forEach((button) => button.addEventListener("click", () => navigate(button.dataset.section)));
    $$("[data-goto]").forEach((button) => button.addEventListener("click", () => navigate(button.dataset.goto)));
    $$("[data-command]").forEach((button) => button.addEventListener("click", async () => { try { await executeCommand(button.dataset.command, { fromQuickAction: true }); } catch (_) { /* shown */ } }));
    $("#clearConsoleBtn").addEventListener("click", () => { $("#consoleOutput").innerHTML = ""; showToast("Console output cleared"); });
    $("#refreshStatusBtn").addEventListener("click", refreshStatus);
    $("#runSmokeBtn").addEventListener("click", async () => {
        navigate("console");
        for (const command of ["PING", "SET redislab:smoke gui-online", "GET redislab:smoke", "TTL redislab:smoke"]) { try { await executeCommand(command, { fromQuickAction: true }); } catch (_) { break; } }
    });
}

setupNavigation();
setupConsole();
setupDataBuilder();
setupStreamBuilder();
setupSafety();
setupDiagnostics();
setupBenchmarks();
refreshStatus();
