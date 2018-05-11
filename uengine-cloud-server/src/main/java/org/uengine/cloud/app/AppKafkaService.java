package org.uengine.cloud.app;

import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.UserService;
import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.models.Project;
import org.gitlab4j.api.models.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.uengine.cloud.app.git.GithubExtentApi;
import org.uengine.cloud.app.git.GitlabExtentApi;
import org.uengine.cloud.tenant.TenantContext;
import org.uengine.iam.client.IamClient;
import org.uengine.iam.client.model.OauthUser;
import org.uengine.iam.util.JsonUtils;

import java.util.Map;
import java.util.UUID;

@Service
public class AppKafkaService {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private AppLockService lockService;

    @Autowired
    private AppCreateService appCreateService;

    private static final Logger LOGGER = LoggerFactory.getLogger(AppKafkaService.class);

    private static final String CREATE_APP_TOPIC = "CREATE_APP_TOPIC";
    private static final String DELETE_APP_TOPIC = "DELETE_APP_TOPIC";

    public void createAppSend(AppCreate appCreate) throws Exception {

        LOGGER.info("createApp send to kafka {}", appCreate.getAppName());

        kafkaTemplate.send(CREATE_APP_TOPIC, JsonUtils.marshal(appCreate));
    }

    @KafkaListener(topics = CREATE_APP_TOPIC, containerFactory = "kafkaListenerContainerFactory")
    public void createAppReceive(String payload, Acknowledgment ack) throws Exception {

        try {
            AppCreate appCreate = JsonUtils.convertValue((Map) JsonUtils.unmarshal(payload), AppCreate.class);
            String appName = appCreate.getAppName();

            LOGGER.info("createApp receive from kafka {}", appName);

            boolean creatable = lockService.isCreatable();

            if (!creatable) {

                int queueCount = lockService.createQueueCount();
                LOGGER.info("createApp resend to kafka for previous queueCount {}", queueCount);

                //1초 후 재 등록.
                Thread.sleep(1000);
                kafkaTemplate.send(CREATE_APP_TOPIC, JsonUtils.marshal(appCreate));
                return;
            }

            try {
                lockService.creationLock(appName);

                appCreateService.performAppCreation(appCreate);

            } finally {
                lockService.creationUnLock(appName);
            }

        } finally {
            ack.acknowledge();
        }
    }
}
