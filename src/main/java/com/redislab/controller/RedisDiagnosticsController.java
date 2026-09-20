package com.redislab.controller;

import com.redislab.model.RedisKeyAnalysis;
import com.redislab.model.RedisKeyInspection;
import com.redislab.model.RedisMemoryReport;
import com.redislab.service.RedisDiagnosticsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/redis/diagnostics")
public class RedisDiagnosticsController {

    private final RedisDiagnosticsService service;

    public RedisDiagnosticsController(RedisDiagnosticsService service) {
        this.service = service;
    }

    @GetMapping("/key")
    public RedisKeyInspection key(@RequestParam String key) {
        return service.inspect(key);
    }

    @GetMapping("/memory")
    public RedisMemoryReport memory() {
        return service.memoryReport();
    }

    @GetMapping("/scan")
    public List<String> scan(
            @RequestParam(required = false) String pattern,
            @RequestParam(defaultValue = "100") long count) {

        return service.scan(pattern, count);
    }

    @GetMapping("/analyze")
    public RedisKeyAnalysis analyze(
            @RequestParam(required = false) String pattern,
            @RequestParam(defaultValue = "1000") long scanCount,
            @RequestParam(defaultValue = "20") int largestCount) {

        return service.analyze(pattern, scanCount, largestCount);
    }
}
