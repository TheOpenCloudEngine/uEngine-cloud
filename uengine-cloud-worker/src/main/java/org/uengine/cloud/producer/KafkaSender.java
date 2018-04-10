package org.uengine.cloud.producer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.uengine.cloud.redis.Movie;
import org.uengine.cloud.redis.RedisRepository;

import java.util.UUID;

public class KafkaSender {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(KafkaSender.class);

    @Autowired
    private RedisRepository redisRepository;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    public void send(String topic, String payload) {
        System.out.println(String.format("sending to topic='%s'", topic));

//        Movie movie = new Movie();
//        movie.setId(UUID.randomUUID().toString());
//        redisRepository.add(movie);

        kafkaTemplate.send(topic, payload);
    }
}
