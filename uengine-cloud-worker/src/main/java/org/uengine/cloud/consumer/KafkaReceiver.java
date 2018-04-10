package org.uengine.cloud.consumer;

import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import org.uengine.cloud.redis.MessagePublisher;
import org.uengine.cloud.redis.Movie;
import org.uengine.cloud.redis.RedisRepository;
import org.uengine.iam.util.StringUtils;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

public class KafkaReceiver {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(KafkaReceiver.class);

    private CountDownLatch latch = new CountDownLatch(1);

    @Autowired
    private Environment environment;

    @Autowired
    private MessagePublisher redisPub;

    @Autowired
    private RedisRepository redisRepository;

//    public CountDownLatch getLatch() {
//        return latch;
//    }


    @KafkaListener(topics = "${spring.kafka.topic.mesos}", containerFactory = "kafkaListenerContainerFactory")
    public void receive(String payload, Acknowledgment ack) throws Exception {
        try {
            System.out.println(String.format("received payload='%s'", payload));

//            Map<Object, Object> allMovies = redisRepository.findAllMovies();
//
//            LOGGER.info("redis count {}", allMovies.keySet().size());

            redisPub.publish(payload);

        } finally {
            latch.countDown();
            //commit event
            ack.acknowledge();
        }
    }
}
