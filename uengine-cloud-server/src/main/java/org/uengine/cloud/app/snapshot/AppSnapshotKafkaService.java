package org.uengine.cloud.app.snapshot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;
import org.uengine.cloud.app.AppLockService;
import org.uengine.cloud.app.config.AppConfigYmlResource;
import org.uengine.iam.util.JsonUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AppSnapshotKafkaService {

    @Autowired
    private AppSnapshotService snapshotService;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private AppSnapshotRepository snapshotRepository;

    @Autowired
    private AppLockService lockService;

    private static final Logger LOGGER = LoggerFactory.getLogger(AppSnapshotKafkaService.class);

    public static final String CREATE_SNAPSHOT_TOPIC = "CREATE_SNAPSHOT";
    public static final String RESTORE_SNAPSHOT_TOPIC = "RESTORE_SNAPSHOT";


    public void createSnapshotSend(String appName, String snapshotName, Long appGroupSnapshotId) throws Exception {

        LOGGER.info("createSnapshot send to kafka {}", appName);

        Map params = new HashMap();
        params.put("appName", appName);
        params.put("snapshotName", snapshotName);
        params.put("appGroupSnapshotId", appGroupSnapshotId);

        kafkaTemplate.send(CREATE_SNAPSHOT_TOPIC, JsonUtils.marshal(params));
    }

    @KafkaListener(topics = CREATE_SNAPSHOT_TOPIC, containerFactory = "kafkaListenerContainerFactory")
    public void createSnapshotReceive(String payload, Acknowledgment ack) throws Exception {

        try {
            Map params = JsonUtils.unmarshal(payload);
            String appName = params.get("appName").toString();
            String snapshotName = params.get("snapshotName") != null ? (String) params.get("snapshotName") : null;
            Long appGroupSnapshotId = params.get("appGroupSnapshotId") != null ? (Long) params.get("appGroupSnapshotId") : null;

            LOGGER.info("createSnapshot receive from kafka {}", appName);

            boolean lock = lockService.isLock(appName);

            if (lock) {

                LOGGER.info("createSnapshot resend to kafka for busy {}", appName);

                //1초 후 재 등록.
                Thread.sleep(1000);
                kafkaTemplate.send(CREATE_SNAPSHOT_TOPIC, JsonUtils.marshal(params));
                return;
            }

            try {
                lockService.lock(appName);

                snapshotService.createSnapshot(appName, snapshotName, appGroupSnapshotId);
            } finally {
                lockService.unLock(appName);
            }

        } finally {
            ack.acknowledge();
        }
    }

    public void restoreSnapshotSend(Long snapshotId, List<String> stages, AppConfigYmlResource overrideResource, boolean redeploy) throws Exception {

        LOGGER.info("restoreSnapshot send to kafka {}", snapshotId);

        Map params = new HashMap();
        params.put("snapshotId", snapshotId);
        params.put("stages", stages);
        params.put("overrideResource", overrideResource);
        params.put("redeploy", redeploy);

        kafkaTemplate.send(RESTORE_SNAPSHOT_TOPIC, JsonUtils.marshal(params));
    }

    @KafkaListener(topics = RESTORE_SNAPSHOT_TOPIC, containerFactory = "kafkaListenerContainerFactory")
    public void restoreSnapshotReceive(String payload, Acknowledgment ack) throws Exception {

        try {
            Map params = JsonUtils.unmarshal(payload);
            Long snapshotId = params.get("snapshotId") != null ? (Long) params.get("snapshotId") : null;
            List<String> stages = params.get("stages") != null ? (List<String>) params.get("stages") : null;
            AppConfigYmlResource overrideResource = params.get("overrideResource") != null ? (AppConfigYmlResource) params.get("overrideResource") : null;
            boolean redeploy = params.get("redeploy") != null ? (boolean) params.get("redeploy") : false;


            LOGGER.info("restoreSnapshot receive from kafka {}", snapshotId);

            AppSnapshot appSnapshot = snapshotRepository.findOne(snapshotId);
            String appName = appSnapshot.getAppName();
            boolean lock = lockService.isLock(appName);

            if (lock) {

                LOGGER.info("restoreSnapshot resend to kafka for busy {}, {}", appName, snapshotId);

                //1초 후 재 등록.
                Thread.sleep(1000);
                kafkaTemplate.send(CREATE_SNAPSHOT_TOPIC, JsonUtils.marshal(params));
                return;
            }

            try {
                lockService.lock(appName);

                snapshotService.restoreSnapshot(snapshotId, stages, overrideResource, redeploy);
            } finally {
                lockService.unLock(appName);
            }

        } finally {
            ack.acknowledge();
        }
    }
}
