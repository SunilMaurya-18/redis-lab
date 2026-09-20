package com.redislab.controller;

import com.redislab.model.BenchmarkReport;
import com.redislab.service.RedisBenchmarkService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/redis/benchmarks")
public class RedisBenchmarkController {

    private final RedisBenchmarkService service;

    public RedisBenchmarkController(RedisBenchmarkService service) {
        this.service = service;
    }

    @PostMapping("/run")
    public BenchmarkReport run(
            @RequestParam(defaultValue = "redislab:benchmark") String keyPrefix,
            @RequestParam(defaultValue = "100") int warmupIterations,
            @RequestParam(defaultValue = "1000") int measuredIterations,
            @RequestParam(defaultValue = "100") int batchSize) {

        return service.run(keyPrefix, warmupIterations, measuredIterations, batchSize);
    }
}
