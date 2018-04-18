package org.uengine.cloud.app.marathon;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.uengine.iam.util.JsonUtils;

import java.util.Map;

@Service
public class MesosKafkaService {

    @Autowired
    private MarathonCacheService marathonCacheService;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private MarathonMessageHandler marathonMessageHandler;

    @Autowired
    private RedisTemplate redisTemplate;

    private ChannelTopic topic = new ChannelTopic("marathonApp");

    private static final Logger LOGGER = LoggerFactory.getLogger(MesosKafkaService.class);

    public static final String MARATHON_APP_TOPIC = "MARATHON_APP";

    public void marathonAppChangeSend(Map data) throws Exception {

        LOGGER.info("marathonAppChangeSend to kafka {}", data.get("appId").toString());
        kafkaTemplate.send(MARATHON_APP_TOPIC, JsonUtils.marshal(data));
    }

    @KafkaListener(topics = MARATHON_APP_TOPIC, containerFactory = "kafkaListenerContainerFactory")
    public void marathonAppChangeReceive(String payload, Acknowledgment ack) throws Exception {

        try {
            Map data = JsonUtils.unmarshal(payload);
            String appId = data.get("appId").toString();

            LOGGER.info("marathonAppChangeReceive from kafka {}", appId);
            Map marathonApp = marathonCacheService.updateMarathonAppByIdCache(appId);
            marathonMessageHandler.publish(marathonApp);

        } finally {
            ack.acknowledge();
        }
    }
}
