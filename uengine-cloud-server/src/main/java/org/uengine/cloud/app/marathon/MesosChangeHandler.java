package org.uengine.cloud.app.marathon;

import com.launchdarkly.eventsource.MessageEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.uengine.iam.util.JsonUtils;

import java.util.Map;

@Component
public class MesosChangeHandler implements DefaultEventHandler {


    @Autowired
    private MesosKafkaService mesosKafkaService;

    private static final String STATUS_UPDATE_EVENT = "status_update_event";
    private static final String HEALTH_STATUS_CHANGED_EVENT = "health_status_changed_event";

    private static final Logger LOGGER = LoggerFactory.getLogger(MesosChangeHandler.class);

    /**
     * Mesos 이벤트 스트림 처리 핸들러.
     *
     * @param event
     * @param messageEvent
     * @throws Exception
     */
    @Override
    public void onMessage(String event, MessageEvent messageEvent) throws Exception {

        if (STATUS_UPDATE_EVENT.equals(event) || HEALTH_STATUS_CHANGED_EVENT.equals(event)) {

            String data = messageEvent.getData();
            Map message = JsonUtils.unmarshal(data);
            String appId = message.get("appId").toString();

            LOGGER.info("Event from mesos, {}", appId);

            mesosKafkaService.marathonAppChangeSend(message);
        }
    }
}