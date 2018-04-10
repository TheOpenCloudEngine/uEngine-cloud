package org.uengine.cloud.listener;

import com.launchdarkly.eventsource.MessageEvent;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.uengine.cloud.producer.KafkaSender;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonValue;
import java.io.StringReader;

@Component
public class MesosChangeHandler implements DefaultEventHandler {

    @Autowired
    private KafkaSender sender;

    @Value("${spring.kafka.topic.mesos}")
    private String MESOS_TOPIC;

    @Override
    public void onMessage(String event, MessageEvent messageEvent) throws Exception {
        // System.out.println(messageEvent.getData());

        sender.send(MESOS_TOPIC, messageEvent.getData());
//        try (JsonReader jsonReader = Json
//                .createReader(new StringReader(messageEvent.getData()))) {
//            JsonObject jsonObject = jsonReader.readObject();

//            JsonValue title = jsonObject.getValue("/title");
//            JsonValue changeType = jsonObject.getValue("/type");
//            System.out.println(changeType.toString() + " : " + title.toString());

    }
}