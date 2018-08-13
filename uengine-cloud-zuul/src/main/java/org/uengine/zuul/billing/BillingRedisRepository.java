package org.uengine.zuul.billing;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.uengine.iam.util.JsonUtils;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.Map;

@Service
public class BillingRedisRepository {

    private Logger logger = LoggerFactory.getLogger(BillingRedisRepository.class);

    private static final String KEY = "USER_SUBSCRIPTIONS";

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private HashOperations hashOperations;

    private ObjectMapper objectMapper;

    @PostConstruct
    private void init() {
        hashOperations = redisTemplate.opsForHash();
        objectMapper = new ObjectMapper();
    }

    public UserSubscriptions getUserSubscriptions(String userName) {
        String s = (String) this.hashOperations.get(KEY, userName);
        if (s == null) {
            return null;
        }
        try {
            return objectMapper.readValue(s, UserSubscriptions.class);
        } catch (IOException ex) {
            return null;
        }
    }

    public void saveUserSubscriptions(String userName, UserSubscriptions userSubscriptions) {
        try {
            String s = objectMapper.writeValueAsString(userSubscriptions);
            this.hashOperations.put(KEY, userName, s);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

}
