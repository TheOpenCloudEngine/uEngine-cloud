package org.uengine.cloud.app.emitter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.uengine.cloud.app.AppEntity;
import org.uengine.iam.util.JsonUtils;

public class AppEntityBaseMessageHandler implements MessageListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(AppEntityBaseMessageHandler.class);

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    public static String chanelTopic = "AppEntityBaseMessage";

    @Autowired
    private SSEController sseController;

    @Override
    public void onMessage(Message message, byte[] bytes) {
        this.subscribe(message, bytes);
    }

    @Async
    public void subscribe(Message message, byte[] bytes) {
        try {
            String str = stringRedisTemplate.getStringSerializer().deserialize(message.getBody());
            AppEntityBaseMessage appEntityBaseMessage =
                    JsonUtils.convertValue(JsonUtils.unmarshal(str), AppEntityBaseMessage.class);

            //If body is null, set appEntity as body.
            if (appEntityBaseMessage.getBody() == null) {
                appEntityBaseMessage.setBody(appEntityBaseMessage.getAppEntity());
            }

            sseController.appEntityBaseEmitterSend(appEntityBaseMessage);

            LOGGER.info("subscribe from redis , {} , {}",
                    appEntityBaseMessage.getTopic().toString(),
                    appEntityBaseMessage.getAppEntity() == null ? null : appEntityBaseMessage.getAppEntity().getName());
        } catch (Exception ex) {
            LOGGER.error("subscribe from redis failed");
        }
    }

    @Async
    public void publish(AppEntityBaseMessageTopic topic, AppEntity appEntity, String stage, Object body) {
        try {
            LOGGER.info("publish to redis, {}, {}", topic.toString(), appEntity == null ? null : appEntity.getName());
            AppEntityBaseMessage message = new AppEntityBaseMessage();
            message.setTopic(topic);
            message.setAppEntity(appEntity);
            message.setStage(stage);
            message.setBody(body);

            stringRedisTemplate.convertAndSend(chanelTopic, JsonUtils.marshal(message));
        } catch (Exception ex) {
            LOGGER.error("publish to redis failed, {} , {}", appEntity == null ? null : appEntity.getName());
        }
    }
}
