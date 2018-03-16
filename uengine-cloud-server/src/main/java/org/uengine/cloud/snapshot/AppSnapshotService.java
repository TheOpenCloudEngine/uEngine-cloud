package org.uengine.cloud.snapshot;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import freemarker.template.SimpleDate;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.models.Project;
import org.gitlab4j.api.models.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.uengine.cloud.app.*;
import org.uengine.cloud.scheduler.CronTable;
import org.uengine.cloud.scheduler.JobScheduler;
import org.uengine.cloud.tenant.TenantContext;
import org.uengine.iam.client.IamClient;
import org.uengine.iam.client.ResourceOwnerPasswordCredentials;
import org.uengine.iam.client.TokenType;
import org.uengine.iam.client.model.OauthUser;
import org.uengine.iam.util.HttpUtils;
import org.uengine.iam.util.JsonUtils;
import org.uengine.iam.util.StringUtils;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by uengine on 2017. 11. 16..
 */
@Service
public class AppSnapshotService {

    @Autowired
    private AppService appService;

    @Autowired
    private AppSnapshotRepository snapshotRepository;

    public boolean restoreSnapshot(Long snapshotId, String stage) throws Exception {
        AppSnapshot appSnapshot = snapshotRepository.findOne(snapshotId);
        if (appSnapshot == null) {
            throw new Exception(String.format("Not Found appSnapshot %s", snapshotId));
        }
        return false;
    }

    public boolean validateSnapshot(AppSnapshot appSnapshot) throws Exception {
        //스냅샷을 점검한다.
        return false;
    }

    public AppSnapshot createSnapshot(String appName, String snapshotName, Long appGroupSnapshotId) throws Exception {
        //deployJson 을 포함한 app 정보를 가져온다.
        AppEntity appEntity = appService.getAppIncludeDeployJson(appName);

        //리소스를 가져온다.
        AppConfigYmlResource configYmlResource = this.createAppConfigSnapshot(appName);

        AppSnapshot snapshot = new AppSnapshot();
        snapshot.setApp(appEntity);
        snapshot.setAppConfigYmlResource(configYmlResource);

        if (appGroupSnapshotId != null && appGroupSnapshotId > 0) {
            snapshot.setAppGroupSnapshotId(appGroupSnapshotId);
        }
        snapshot.setAppName(appEntity.getName());
        snapshot.setIam(appEntity.getIam());

        //name 생성
        if (StringUtils.isEmpty(snapshotName)) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
            String dateString = dateFormat.format(new Date());
            snapshotName = String.format("%s %s Snapshot", dateString, appName);
        }
        snapshot.setName(snapshotName);

        return snapshotRepository.save(snapshot);
    }

    public AppConfigYmlResource createAppConfigSnapshot(String appName) throws Exception {
        AppConfigYmlResource configYmlResource = new AppConfigYmlResource();

        configYmlResource.setApplication(appService.getApplicationYml());
        configYmlResource.setCommon(appService.getAppConfigYml(appName, null));
        configYmlResource.setDev(appService.getAppConfigYml(appName, "dev"));
        configYmlResource.setStg(appService.getAppConfigYml(appName, "stg"));
        configYmlResource.setProd(appService.getAppConfigYml(appName, "prod"));

        return configYmlResource;
    }
}
