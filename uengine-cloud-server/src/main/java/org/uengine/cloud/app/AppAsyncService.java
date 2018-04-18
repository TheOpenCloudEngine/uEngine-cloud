package org.uengine.cloud.app;

import org.apache.commons.lang.StringEscapeUtils;
import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.models.Project;
import org.gitlab4j.api.models.ProjectHook;
import org.gitlab4j.api.models.User;
import org.gitlab4j.api.models.Visibility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.uengine.cloud.app.config.AppConfigService;
import org.uengine.cloud.app.deployjson.AppDeployJsonService;
import org.uengine.cloud.app.deployment.AppDeploymentService;
import org.uengine.cloud.app.deployment.DeploymentHistoryEntity;
import org.uengine.cloud.app.deployment.DeploymentHistoryRepository;
import org.uengine.cloud.app.git.GitlabExtentApi;
import org.uengine.cloud.app.log.AppLogAction;
import org.uengine.cloud.app.log.AppLogService;
import org.uengine.cloud.app.log.AppLogStatus;
import org.uengine.cloud.app.marathon.DcosApi;
import org.uengine.cloud.app.pipeline.AppPipeLineService;
import org.uengine.cloud.app.snapshot.AppSnapshot;
import org.uengine.cloud.app.snapshot.AppSnapshotService;
import org.uengine.cloud.catalog.CatalogService;
import org.uengine.cloud.catalog.CategoryItem;
import org.uengine.cloud.catalog.FileMapping;
import org.uengine.cloud.deployment.*;
import org.uengine.cloud.templates.MustacheTemplateEngine;
import org.uengine.iam.client.IamClient;
import org.uengine.iam.client.model.OauthUser;
import org.uengine.iam.util.ApplicationContextRegistry;
import org.uengine.iam.util.JsonUtils;
import org.uengine.iam.util.StringUtils;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

@Lazy
@Service
public class AppAsyncService {

    @Autowired
    private Environment environment;

    @Autowired
    private AppWebService appWebService;

    @Autowired
    private GitLabApi gitLabApi;

    @Autowired
    private GitlabExtentApi gitlabExtentApi;

    @Autowired
    private AppLogService logService;

    @Autowired
    private CatalogService catalogService;

    @Autowired
    private DeploymentHistoryRepository historyRepository;

    @Autowired
    private DcosApi dcosApi;

    @Autowired
    private AppSnapshotService snapshotService;

    //not use cache.
    @Autowired
    private AppEntityRepository appEntityRepository;

    @Autowired
    private AppPipeLineService pipeLineService;

    @Autowired
    private AppConfigService appConfigService;

    @Autowired
    private AppDeployJsonService deployJsonService;

}
