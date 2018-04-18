package org.uengine.cloud.app.marathon;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.uengine.cloud.app.AppEntity;
import org.uengine.cloud.app.SSEController;
import org.uengine.iam.util.JsonUtils;

import java.util.HashMap;
import java.util.Map;

public class MarathonMessageHandler implements MessageListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(MarathonMessageHandler.class);

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    public String topic = "marathonApp";

    @Autowired
    private SSEController sseController;

    @Autowired
    private MarathonCacheService marathonCacheService;

    @Autowired
    private MarathonService marathonService;

    @Override
    public void onMessage(Message message, byte[] bytes) {
        this.subscribe(message, bytes);
    }

    @Async
    public void subscribe(Message message, byte[] bytes) {
        try {
            byte[] body = message.getBody();
            String str = stringRedisTemplate.getStringSerializer().deserialize(body);
            Map marathonApp = JsonUtils.unmarshal(str);
            AppEntity appEntity =
                    marathonService.getAppEntityFromMarathonAppId(((Map) marathonApp.get("app")).get("id").toString());
            Map map = new HashMap();
            map.put("topic", topic);
            map.put("message", marathonApp);
            if (appEntity != null) {
                map.put("service", false);
                map.put("appName", appEntity.getName());
            } else {
                map.put("service", true);
            }

            sseController.gitlabBaseEmitterSend(appEntity, JsonUtils.marshal(map));

            LOGGER.error("subscribe marathonApp from redis success");
        } catch (Exception ex) {
            LOGGER.error("subscribe marathonApp from redis failed");
        }
    }

    @Async
    public void publish(Map marathonApp) {
        try {
            LOGGER.info("publish marathonApp to redis");
            stringRedisTemplate.convertAndSend(topic, JsonUtils.marshal(marathonApp));
        } catch (Exception ex) {
            LOGGER.error("publish marathonApp to redis failed");
        }
    }
}
