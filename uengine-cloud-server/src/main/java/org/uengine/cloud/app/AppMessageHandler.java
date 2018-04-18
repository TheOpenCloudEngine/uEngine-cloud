package org.uengine.cloud.app;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.uengine.cloud.app.marathon.MarathonCacheService;
import org.uengine.iam.util.JsonUtils;

import java.util.HashMap;
import java.util.Map;

public class AppMessageHandler implements MessageListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(AppMessageHandler.class);

    private RedisTemplate redisTemplate;

    public String topic = "app";

    @Autowired
    private SSEController sseController;

    @Autowired
    private AppWebCacheService appWebCacheService;


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
            String appName = (String) redisTemplate.getStringSerializer().deserialize(body);
            appName = appName.replaceAll("\"", "");
            AppEntity appEntity = appWebCacheService.findOneCache(appName);
            Map map = new HashMap();
            map.put("topic", topic);
            map.put("message", appEntity);

            sseController.gitlabBaseEmitterSend(appEntity, JsonUtils.marshal(map));

            LOGGER.info("key : {}, message : {}", new String(bytes), appName);
        } catch (Exception ex) {
            LOGGER.error("subscribe from redis failed");
        }
    }

    @Async
    public void publish(String appName) {
        try {
            LOGGER.info("publish to redis, {}", appName);
            redisTemplate.convertAndSend(topic, appName);
        } catch (Exception ex) {
            LOGGER.error("publish to redis failed");
        }
    }
}
