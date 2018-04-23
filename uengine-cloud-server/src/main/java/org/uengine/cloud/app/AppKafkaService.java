package org.uengine.cloud.app;

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
import org.uengine.cloud.app.git.GitlabExtentApi;
import org.uengine.cloud.tenant.TenantContext;
import org.uengine.iam.client.IamClient;
import org.uengine.iam.client.model.OauthUser;
import org.uengine.iam.util.JsonUtils;
import org.uengine.iam.util.StringUtils;

import java.util.Map;
import java.util.UUID;

@Service
public class AppKafkaService {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private AppWebCacheService appWebCacheService;

    @Autowired
    private GitlabExtentApi gitlabExtentApi;

    @Autowired
    private Environment environment;

    @Autowired
    private GitLabApi gitLabApi;

    @Autowired
    private AppLockService lockService;

    @Autowired
    private AppWebService appWebService;

    private static final Logger LOGGER = LoggerFactory.getLogger(AppKafkaService.class);

    private static final String CREATE_APP_TOPIC = "CREATE_APP_TOPIC";
    private static final String DELETE_APP_TOPIC = "DELETE_APP_TOPIC";

    public AppEntity createAppSend(AppCreate appCreate) throws Exception {

        //user save
        appCreate.setUser(TenantContext.getThreadLocalInstance().getUser());

        //appName 체크
        if (StringUtils.isEmpty(appCreate.getAppName())) {
            throw new Exception("App name is empty");
        }

        //이름 강제 고정
        String appName = appCreate.getAppName().toLowerCase().replaceAll(" ", "-");
        //remove all the special characters a part of alpha numeric characters, space and hyphen.
        appName = appName.replaceAll("[^a-zA-Z0-9 -]", "");
        appCreate.setAppName(appName);

        //appName 중복 체크
        AppEntity existEntity = appWebCacheService.findOneCache(appCreate.getAppName());
        if (existEntity != null) {
            throw new Exception("App name is exist");
        }

        //깃랩 프로젝트 체크
        gitLabApi.unsudo();
        if (appCreate.getProjectId() > 0) {
            try {
                Project project = gitLabApi.getProjectApi().getProject(appCreate.getProjectId());
            } catch (Exception e) {
                throw new Exception(e);
            }
        }

        //러너체크
        gitlabExtentApi.getDockerRunnerId();


        //깃랩 사용자 정의
        String userName = TenantContext.getThreadLocalInstance().getUserId();
        IamClient iamClient = new IamClient(environment.getProperty("iam.host"),
                Integer.parseInt(environment.getProperty("iam.port")),
                environment.getProperty("iam.clientId"),
                environment.getProperty("iam.clientSecret"));

        OauthUser oauthUser = iamClient.getUser(userName);
        int gitlabId = (int) oauthUser.getMetaData().get("gitlab-id");

        //깃랩에 유저가 있는지 체크
        User existGitlabUser = gitLabApi.getUserApi().getUser(gitlabId);
        if (existGitlabUser == null) {
            throw new Exception("Not found gitlab user id: " + gitlabId);
        }

        //신규 appEntity
        AppEntity appEntity = new AppEntity();
        appEntity.setName(appCreate.getAppName());
        appEntity.setAppType(appCreate.getCategoryItemId());
        appEntity.setIam(userName);

        //생성 상태 저장
        appEntity.setCreateStatus("repository-create");

        //콘피그 패스워드 생성
        appEntity.setConfigPassword(UUID.randomUUID().toString());

        //시큐어 콘피그 여부 저장
        appEntity.setInsecureConfig(appCreate.getInsecureConfig());

        //초기 멤버 유저 저장
        appEntity.setMemberIds("m" + gitlabId + "m");

        //it will throw exception if transaction accident fired.
        AppEntity save = appWebCacheService.saveCache(appEntity);

        LOGGER.info("createApp send to kafka {}", appCreate.getAppName());

        kafkaTemplate.send(CREATE_APP_TOPIC, JsonUtils.marshal(appCreate));

        return save;
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

                appWebService.createApp(appCreate);

            } finally {
                lockService.creationUnLock(appName);
            }

        } finally {
            ack.acknowledge();
        }
    }
}
