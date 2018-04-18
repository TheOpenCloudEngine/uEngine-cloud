package org.uengine.cloud.app;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.scheduling.annotation.Async;
import org.uengine.cloud.app.marathon.MarathonCacheService;
import org.uengine.iam.util.JsonUtils;

import javax.json.Json;
import java.util.HashMap;
import java.util.Map;

public class AppMessageHandler implements MessageListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(AppMessageHandler.class);

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    public String topic = "app";

    @Autowired
    private SSEController sseController;

    @Override
    public void onMessage(Message message, byte[] bytes) {
        this.subscribe(message, bytes);
    }

    @Async
    public void subscribe(Message message, byte[] bytes) {
        try {
            byte[] body = message.getBody();
            String str = stringRedisTemplate.getStringSerializer().deserialize(body);
            AppEntity appEntity = JsonUtils.convertValue(JsonUtils.unmarshal(str), AppEntity.class);
            Map map = new HashMap();
            map.put("topic", topic);
            map.put("appName", appEntity.getName());
            map.put("message", appEntity);

            sseController.gitlabBaseEmitterSend(appEntity, JsonUtils.marshal(map));

            LOGGER.info("key : {}, message : {}", new String(bytes), appEntity.getName());
        } catch (Exception ex) {
            LOGGER.error("subscribe from redis failed");
        }
    }

    @Async
    public void publish(AppEntity appEntity) {
        try {
            LOGGER.info("publish to redis, {}", appEntity.getName());
            stringRedisTemplate.convertAndSend(topic, JsonUtils.marshal(appEntity));
        } catch (Exception ex) {
            LOGGER.error("publish to redis failed, {}", appEntity.getName());
        }
    }
}
