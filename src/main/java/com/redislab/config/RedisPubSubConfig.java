package com.redislab.config;

import com.redislab.service.RedisPubSubListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;

@Configuration
@ConditionalOnProperty(
        name = "redislab.pubsub.listener.enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class RedisPubSubConfig {

    public static final String EVENT_CHANNEL =
            "redislab:events";

    @Bean
    public MessageListenerAdapter eventMessageListener(
            RedisPubSubListener listener) {

        return new MessageListenerAdapter(
                listener,
                "onMessage"
        );
    }

    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(
            RedisConnectionFactory connectionFactory,
            MessageListenerAdapter eventMessageListener) {

        RedisMessageListenerContainer container =
                new RedisMessageListenerContainer();

        container.setConnectionFactory(connectionFactory);

        container.addMessageListener(
                eventMessageListener,
                new PatternTopic(EVENT_CHANNEL)
        );

        return container;
    }
}
