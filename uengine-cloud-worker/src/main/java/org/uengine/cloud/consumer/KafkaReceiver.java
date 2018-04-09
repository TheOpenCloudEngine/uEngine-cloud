package org.uengine.cloud.consumer;

import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.uengine.iam.util.StringUtils;

public class KafkaReceiver {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(KafkaReceiver.class);

    //private CountDownLatch latch = new CountDownLatch(1);

//    public CountDownLatch getLatch() {
//        return latch;
//    }

    @KafkaListener(topics = "${kafka.topic.mesos}")
    public void receive(String payload) {
        System.out.println(String.format("received payload='%s'", payload));
    }
}
