package org.uengine.cloud.redis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import redis.clients.jedis.Jedis;

@Service
public class LeaderWrapper {

    private static final int LOCK_TIMEOUT = 5000;
    private static final String LEADER_LOCK = "LEADER_LOCK";
    private boolean isLeader;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private String myApplicationId;


    @Scheduled(fixedDelay = 2000)
    public void tryToAcquireLock() {

        try {
            Jedis jedis = (Jedis) redisTemplate.getConnectionFactory().getConnection().getNativeConnection();

            if (isLeader) {
                jedis.del(LEADER_LOCK);
            }
            jedis.set(LEADER_LOCK, "" + myApplicationId, "NX", "PX", LOCK_TIMEOUT);
            String get = jedis.get(LEADER_LOCK);
            isLeader = myApplicationId.equals(get);
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
