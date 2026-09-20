package com.redislab.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.script.RedisScript;

@Configuration
public class RedisScriptingConfig {
    @Bean
    public RedisScript<Long> incrementScript() {

        return RedisScript.of(
                new ClassPathResource(
                        "scripts/increment.lua"
                ),
                Long.class
        );
    }
    @Bean
    public RedisScript<Long> checkAndSetScript() {

        return RedisScript.of(
                new ClassPathResource(
                        "scripts/check-and-set.lua"
                ),
                Long.class
        );
    }
}
