package org.uengine.cloud.redis;

import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.core.env.Environment;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

@Configuration
public class LeaderElectionConfig {

    @Autowired
    private Environment environment;

    @Bean
    public JedisPool jedisPool() {
        String redisHost = environment.getProperty("spring.redis-host");

        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setTestOnBorrow(true);
        poolConfig.setTestWhileIdle(true);
        return new JedisPool(
                poolConfig,
                redisHost.split(":")[0],
                Integer.parseInt(redisHost.split(":")[1]),
                500);
    }

    @Bean
    public Jedis jedis(JedisPool jedisPool) {
        return jedisPool.getResource();
    }

    @Bean
    public String myApplicationId() {
        return "" + new Random().nextInt(Integer.MAX_VALUE);
    }

}
