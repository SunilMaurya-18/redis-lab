package com.redislab;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = "redislab.pubsub.listener.enabled=false")
class RedisLabApplicationTests {

	@Test
	void contextLoads() {
	}

}
