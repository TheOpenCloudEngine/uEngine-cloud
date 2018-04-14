package org.uengine.cloud.redis;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;

public class TopicMessageListener implements MessageListener {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(TopicMessageListener.class);

    private RedisTemplate redisTemplate;

    @Autowired
    public void setRedisTemplate(RedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void onMessage(Message message, byte[] bytes) {
        byte[] body = message.getBody();
        String str = (String) redisTemplate.getStringSerializer().deserialize(body);
        LOGGER.info("key : {}, message : {}", new String(bytes), str);
    }
}
