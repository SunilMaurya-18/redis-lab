package com.redislab.controller;

import com.redislab.service.RedisLuaService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/redis/lua")

public class RedisLuaController {
    private final RedisLuaService luaService;

    public RedisLuaController(RedisLuaService luaService) {
        this.luaService = luaService;
    }

    @PostMapping("/increment")
    public Long increment(
            @RequestParam String key,
            @RequestParam long amount
    ) {
        return luaService.increment(key, amount);
    }

    @PostMapping("/check-and-set")
    public boolean checkAndSet(
            @RequestParam String key,
            @RequestParam String expectedValue,
            @RequestParam String newValue
    ) {
        return luaService.checkAndSet(key, expectedValue, newValue);
    }

    @GetMapping("/echo")
    public String echo(
            @RequestParam String message
    ) {
        return luaService.echo(message);
    }
}
