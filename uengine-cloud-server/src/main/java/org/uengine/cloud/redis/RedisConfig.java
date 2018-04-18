package org.uengine.cloud.redis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.cache.interceptor.SimpleKeyGenerator;
import org.springframework.cache.transaction.TransactionAwareCacheManagerProxy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.GenericToStringSerializer;
import org.uengine.cloud.app.AppMessageHandler;
import org.uengine.cloud.app.AppWebCacheService;
import org.uengine.cloud.app.marathon.MarathonMessageHandler;
import redis.clients.jedis.Jedis;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Random;

@Configuration
public class RedisConfig {

    @Autowired
    private Environment environment;


    @Bean
    public JedisConnectionFactory jedisConnectionFactory() {
        String redisHost = environment.getProperty("spring.redis-host");

        JedisConnectionFactory factory = new JedisConnectionFactory();
        factory.setHostName(redisHost.split(":")[0]);
        factory.setPort(Integer.parseInt(redisHost.split(":")[1]));
        factory.setUsePool(true);
        return factory;
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate() {
        RedisTemplate<String, Object> template = new RedisTemplate<String, Object>();
        template.setConnectionFactory(jedisConnectionFactory());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        return template;
    }

    @Bean
    public MarathonMessageHandler marathonMessageHandler() {
        return new MarathonMessageHandler();
    }

    @Bean
    public AppMessageHandler appMessageHandler() {
        return new AppMessageHandler();
    }

    @Bean
    public RedisMessageListenerContainer redisContainer(RedisConnectionFactory connectionFactory) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.addMessageListener(marathonMessageHandler(), new PatternTopic("marathonApp"));
        container.addMessageListener(appMessageHandler(), new PatternTopic("app"));

        return container;
    }

    @Bean
    public CacheManager cacheManager(RedisTemplate redisTemplate) {
        RedisCacheManager cacheManager = new RedisCacheManager(redisTemplate);
        cacheManager.setDefaultExpiration(7200);
        cacheManager.setTransactionAware(true);
        return cacheManager;
    }

    @Bean
    public String myApplicationId() {
        return "" + new Random().nextInt(Integer.MAX_VALUE);
    }
}
