package org.uengine.cloud.producer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

public class KafkaSender {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(KafkaSender.class);

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    public void send(String topic, String payload) {
        System.out.println(String.format("sending to topic='%s'", topic));
        kafkaTemplate.send(topic, payload);
    }
}
