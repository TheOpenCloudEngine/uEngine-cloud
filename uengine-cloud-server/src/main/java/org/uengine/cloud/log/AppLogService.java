package org.uengine.cloud.log;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.models.Project;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.uengine.cloud.app.*;
import org.uengine.cloud.scheduler.CronTable;
import org.uengine.cloud.scheduler.JobScheduler;
import org.uengine.cloud.tenant.TenantContext;
import org.uengine.iam.util.HttpUtils;
import org.uengine.iam.util.JsonUtils;
import org.uengine.iam.util.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created by uengine on 2017. 11. 16..
 */
@Service
public class AppLogService {
    @Autowired
    private Environment environment;

    @Autowired
    private AppLogRepository logRepository;

    @Autowired
    private AppService appService;

    public AppLogEntity addHistory(String appName, AppLogAction action, AppLogStatus status, Map params) {
        try {
            String updater = TenantContext.getThreadLocalInstance().getUserId();
            if ("anonymous".equals(updater)) {
                updater = "system";
            }

            String owner = null;
            Map app = appService.getAppByName(appName);
            if (app != null && app.containsKey("iam")) {
                owner = app.get("iam").toString();
            }
            AppLogEntity logEntity = new AppLogEntity();
            logEntity.setAppName(appName);
            logEntity.setAction(action);
            logEntity.setOwner(owner);
            logEntity.setUpdater(updater);
            logEntity.setStatus(status);
            logEntity.setLog(params);
            return logRepository.save(logEntity);

        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
            //이력 저장에 실패해도 플로우에는 영향을 주지 않는다.
        }
    }
}
