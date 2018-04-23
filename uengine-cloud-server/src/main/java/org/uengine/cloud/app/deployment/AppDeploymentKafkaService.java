package org.uengine.cloud.app.deployment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;
import org.uengine.cloud.app.*;
import org.uengine.iam.util.JsonUtils;

import java.util.*;

@Service
public class AppDeploymentKafkaService {

    @Autowired
    private AppDeploymentService deploymentService;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private AppLockService lockService;

    private static final Logger LOGGER = LoggerFactory.getLogger(AppDeploymentKafkaService.class);

    private static final String DEPLOY_APP_TOPIC = "DEPLOY_APP";
    private static final String ROLLBACK_APP_TOPIC = "ROLLBACK_APP";

    public void deployAppSend(
            String appName,
            String stage,
            String commit,
            Long snapshotId,
            String name,
            String description
    ) throws Exception {

        LOGGER.info("deployApp send to kafka {}", appName);

        AppDeployMessage deployAppMessage = new AppDeployMessage(appName, stage, commit, snapshotId, name, description);
        kafkaTemplate.send(DEPLOY_APP_TOPIC, JsonUtils.marshal(deployAppMessage));
    }

    @KafkaListener(topics = DEPLOY_APP_TOPIC, containerFactory = "kafkaListenerContainerFactory")
    public void deployAppReceive(String payload, Acknowledgment ack) throws Exception {

        try {
            AppDeployMessage deployAppMessage = JsonUtils.convertValue((Map) JsonUtils.unmarshal(payload), AppDeployMessage.class);
            String appName = deployAppMessage.getAppName();

            LOGGER.info("deployApp receive from kafka {}", appName);

            boolean lock = lockService.isLock(appName);

            if (lock) {

                LOGGER.info("deployApp resend to kafka for busy {}", appName);

                //1초 후 재 등록.
                Thread.sleep(1000);
                kafkaTemplate.send(DEPLOY_APP_TOPIC, JsonUtils.marshal(deployAppMessage));
                return;
            }

            try {
                lockService.lock(appName);

                deploymentService.deployApp(
                        deployAppMessage.getAppName(),
                        deployAppMessage.getStage(),
                        deployAppMessage.getCommit(),
                        deployAppMessage.getSnapshotId(),
                        deployAppMessage.getName(),
                        deployAppMessage.getDescription()
                );
            } finally {
                lockService.unLock(appName);
            }

        } finally {
            ack.acknowledge();
        }
    }


    public void rollbackAppSend(String appName, String stage) throws Exception {

        LOGGER.info("rollbackApp send to kafka {}", appName);

        Map params = new HashMap();
        params.put("appName", appName);
        params.put("stage", stage);

        kafkaTemplate.send(ROLLBACK_APP_TOPIC, JsonUtils.marshal(params));
    }

    @KafkaListener(topics = ROLLBACK_APP_TOPIC, containerFactory = "kafkaListenerContainerFactory")
    public void rollbackAppReceive(String payload, Acknowledgment ack) throws Exception {

        try {
            Map params = JsonUtils.unmarshal(payload);
            String appName = params.get("appName").toString();
            String stage = params.get("stage").toString();

            LOGGER.info("rollbackApp receive from kafka {}", appName);

            boolean lock = lockService.isLock(appName);

            if (lock) {
                LOGGER.info("rollbackApp resend to kafka for busy {}", appName);

                //1초 후 재 등록.
                Thread.sleep(1000);
                kafkaTemplate.send(DEPLOY_APP_TOPIC, JsonUtils.marshal(params));
                return;
            }

            try {
                lockService.lock(appName);

                deploymentService.rollbackApp(appName, stage);
            } finally {
                lockService.unLock(appName);
            }

        } finally {
            ack.acknowledge();
        }
    }
}
