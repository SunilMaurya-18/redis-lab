package com.redislab.controller;

import com.redislab.service.RedisPipelineService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/redis/pipeline")
public class RedisPipelineController {
    private final RedisPipelineService pipelineService;

    public RedisPipelineController(RedisPipelineService pipelineService) {
        this.pipelineService = pipelineService;
    }

    @PostMapping("/set")
    public List<Object> setBatch(
            @RequestParam String prefix,
            @RequestParam int count
    ) {
        return pipelineService.setBatch(prefix, count);
    }

    @GetMapping("/set")
    public List<Object> getBatch(
            @RequestParam String prefix,
            @RequestParam int count
    ) {
        return pipelineService.getBatch(prefix, count);
    }

    @PostMapping("/mixed")
    public List<Object> mixedPipeline(
            @RequestParam String prefix
    ) {
        return pipelineService.mixedPipeline(prefix);
    }

    @PostMapping("/large")
    public List<Object> largeBatch(
            @RequestParam String prefix,
            @RequestParam int count
    ) {
        return pipelineService.largeBatch(prefix, count);
    }

}
