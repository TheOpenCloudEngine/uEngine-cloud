package org.uengine.cloud.app;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.util.Map;

@Service
public class AppLockService {

    private static final String BUSY_KEY = "APP_BUSY";
    private static final String CREATION_KEY = "APP_CREATING";

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private HashOperations hashOperations;

    @PostConstruct
    private void init() {
        hashOperations = redisTemplate.opsForHash();
    }

    public void lock(String appName) {
        hashOperations.put(BUSY_KEY, appName, "OK");
    }

    public void unLock(String appName) {
        try {
            hashOperations.delete(BUSY_KEY, appName);
        } catch (Exception ex) {

        }
    }

    public boolean isLock(String appName) {
        try {
            return "OK".equals((String) hashOperations.get(BUSY_KEY, appName));
        } catch (Exception ex) {
            return false;
        }
    }

    public void creationLock(String appName) {
        hashOperations.put(CREATION_KEY, appName, "OK");
    }

    public void creationUnLock(String appName) {
        hashOperations.delete(CREATION_KEY, appName);
    }

    /**
     * 생성 중인 앱 작업이 없으면 생성 가능함.
     *
     * @return
     */
    public boolean isCreatable() {
        try {
            Map entries = hashOperations.entries(CREATION_KEY);
            return entries.isEmpty();
        } catch (Exception ex) {
            return false;
        }
    }

    /**
     * 생성 대기중인 큐 카운트를 반환한다.
     *
     * @return
     */
    public int createQueueCount() {
        try {
            Map entries = hashOperations.entries(CREATION_KEY);
            return entries.keySet().size();
        } catch (Exception ex) {
            return 0;
        }
    }
}
