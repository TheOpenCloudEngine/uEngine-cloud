package org.uengine.zuul;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.uengine.zuul.billing.BillingConfig;


@Configuration
@EnableConfigurationProperties(BillingConfig.class)
@ConditionalOnProperty(prefix = BillingConfig.PREFIX, name = "enable", havingValue = "true")
public class RedisConfig {

    @Autowired
    private Environment environment;

    @Bean
    @ConditionalOnProperty(prefix = BillingConfig.PREFIX, name = "enable", havingValue = "true")
    public JedisConnectionFactory jedisConnectionFactory() {
        String host = environment.getProperty("zuul.redis.host");
        int port = Integer.parseInt(environment.getProperty("zuul.redis.port"));

        JedisConnectionFactory factory = new JedisConnectionFactory();
        factory.setHostName(host);
        factory.setPort(port);
        factory.setUsePool(true);
        return factory;
    }

    @Bean
    @ConditionalOnBean(JedisConnectionFactory.class)
    public RedisTemplate<String, Object> redisTemplate() {
        RedisTemplate<String, Object> template = new RedisTemplate<String, Object>();
        template.setConnectionFactory(jedisConnectionFactory());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        return template;
    }

    @Bean
    @ConditionalOnBean(JedisConnectionFactory.class)
    public StringRedisTemplate stringRedisTemplate() {
        StringRedisTemplate template = new StringRedisTemplate();
        template.setConnectionFactory(jedisConnectionFactory());
        template.setValueSerializer(new StringRedisSerializer());
        return template;
    }

    @Bean
    @ConditionalOnBean(JedisConnectionFactory.class)
    public RedisCacheManager cacheManager(JedisConnectionFactory jedisConnectionFactory) {
        RedisCacheManager cacheManager = RedisCacheManager.create(jedisConnectionFactory);
        cacheManager.setTransactionAware(true);
        return cacheManager;
    }
}
