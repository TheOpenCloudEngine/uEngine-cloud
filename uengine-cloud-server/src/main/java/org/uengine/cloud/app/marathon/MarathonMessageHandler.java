package org.uengine.cloud.app.marathon;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.uengine.cloud.app.AppEntity;
import org.uengine.cloud.app.SSEController;
import org.uengine.iam.util.JsonUtils;

import java.util.HashMap;
import java.util.Map;

public class MarathonMessageHandler implements MessageListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(MarathonMessageHandler.class);

    private RedisTemplate redisTemplate;

    public String topic = "marathonApp";

    @Autowired
    private SSEController sseController;

    @Autowired
    private MarathonCacheService marathonCacheService;

    @Autowired
    private MarathonService marathonService;


    @Autowired
    public void setRedisTemplate(RedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void onMessage(Message message, byte[] bytes) {
        this.subscribe(message, bytes);
    }

    @Async
    public void subscribe(Message message, byte[] bytes) {
        try {
            byte[] body = message.getBody();
            String marathonAppId = (String) redisTemplate.getStringSerializer().deserialize(body);
            marathonAppId = marathonAppId.replaceAll("\"", "");
            AppEntity appEntity = marathonService.getAppEntityFromMarathonAppId(marathonAppId);
            Map marathonApp = marathonCacheService.getMarathonAppByIdCache(marathonAppId);
            Map map = new HashMap();
            map.put("topic", topic);
            map.put("message", marathonApp);
            if (appEntity == null) {
                map.put("appName", appEntity.getName());
            }


            sseController.gitlabBaseEmitterSend(appEntity, JsonUtils.marshal(map));

            LOGGER.info("key : {}, message : {}", new String(bytes), marathonAppId);
        } catch (Exception ex) {
            LOGGER.error("subscribe from redis failed");
        }
    }

    @Async
    public void publish(String appId) {
        try {
            LOGGER.info("publish to redis, {}", appId);
            redisTemplate.convertAndSend(topic, appId);
        } catch (Exception ex) {
            LOGGER.error("publish to redis failed");
        }
    }
}
