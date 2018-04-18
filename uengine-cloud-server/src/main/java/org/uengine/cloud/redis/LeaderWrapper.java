package org.uengine.cloud.redis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import redis.clients.jedis.Jedis;

import javax.annotation.PostConstruct;
import java.util.concurrent.TimeUnit;

@Service
public class LeaderWrapper {

    private static final int LOCK_TIMEOUT = 5000;
    private static final String LEADER_LOCK = "LEADER_LOCK";
    private boolean isLeader;

    @Autowired
    private String myApplicationId;

    @Autowired
    private RedisTemplate redisTemplate;

    private ValueOperations valueOperations;

    @PostConstruct
    private void init() {
        valueOperations = redisTemplate.opsForValue();
    }

    @Scheduled(fixedDelay = 2000)
    public void tryToAcquireLock() {

        try {
            Object existLock = valueOperations.get(LEADER_LOCK);

            //if null, set me new leader
            if (existLock == null) {
                valueOperations.set(LEADER_LOCK, myApplicationId, LOCK_TIMEOUT, TimeUnit.MILLISECONDS);
                isLeader = true;
                return;
            }


            //if existLock equals myApplicationId, reset value with timeout.
            isLeader = myApplicationId.equals(existLock);
            if (isLeader) {
                valueOperations.set(LEADER_LOCK, myApplicationId, LOCK_TIMEOUT, TimeUnit.MILLISECONDS);
            }

        } catch (Exception ex) {
            isLeader = false;
        }

        if (isLeader) {
            System.out.println("[" + myApplicationId + "] Now I am leader node");
        } else {
            System.out.println("[" + myApplicationId + "] It's sad being a non-leader node :-( ");
        }
    }

    public boolean amILeader() {
        return isLeader;
    }

}
