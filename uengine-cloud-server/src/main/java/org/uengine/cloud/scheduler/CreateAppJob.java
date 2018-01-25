/**
 * Copyright (C) 2011 Flamingo Project (http://www.cloudine.io).
 * <p/>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p/>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p/>
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.uengine.cloud.scheduler;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.models.Project;
import org.gitlab4j.api.models.ProjectHook;
import org.gitlab4j.api.models.User;
import org.gitlab4j.api.models.Visibility;

import org.uengine.cloud.app.*;
import org.uengine.cloud.catalog.CatalogService;
import org.uengine.cloud.catalog.CategoryItem;
import org.uengine.cloud.catalog.FileMapping;
import org.uengine.cloud.log.AppLogAction;
import org.uengine.cloud.log.AppLogService;
import org.uengine.cloud.log.AppLogStatus;
import org.uengine.iam.client.IamClient;
import org.uengine.iam.client.model.OauthUser;
import org.uengine.iam.util.ApplicationContextRegistry;
import org.uengine.iam.util.JsonUtils;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.core.env.Environment;
import org.uengine.cloud.templates.MustacheTemplateEngine;
import org.uengine.iam.util.StringUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CreateAppJob implements Job {

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        // 클라이언트 잡 실행시 필요한 정보를 가져온다.
        JobDataMap map = jobExecutionContext.getMergedJobDataMap();
        int pipelineId = 0;
        int projectId = 0;
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        AppCreate appCreate = new ObjectMapper().convertValue(map, AppCreate.class);

        Map<String, Object> log = null;
        try {
            log = JsonUtils.convertClassToMap(appCreate);
        } catch (IOException ex) {
            log = new HashMap<>();
        }

        Environment environment = ApplicationContextRegistry.getApplicationContext().getBean(Environment.class);
        AppService appService = ApplicationContextRegistry.getApplicationContext().getBean(AppService.class);
        GitLabApi gitLabApi = ApplicationContextRegistry.getApplicationContext().getBean(GitLabApi.class);
        GitlabExtentApi gitlabExtentApi = ApplicationContextRegistry.getApplicationContext().getBean(GitlabExtentApi.class);
        AppLogService logService = ApplicationContextRegistry.getApplicationContext().getBean(AppLogService.class);
        CatalogService catalogService = ApplicationContextRegistry.getApplicationContext().getBean(CatalogService.class);
        AppJpaRepository appJpaRepository = ApplicationContextRegistry.getApplicationContext().getBean(AppJpaRepository.class);

        try {
            //DCOS 앱 찾기
            AppEntity appEntity = appJpaRepository.findOne(appCreate.getAppName());

            //iamUserName 은 IAM 유저네임
            String iamUserName = appEntity.getIam();


            //IAM 유저정보(패스워드 포함)
            IamClient iamClient = new IamClient(environment.getProperty("iam.host"),
                    Integer.parseInt(environment.getProperty("iam.port")),
                    environment.getProperty("iam.clientId"),
                    environment.getProperty("iam.clientSecret"));
            OauthUser oauthUser = iamClient.getUser(iamUserName);

            //깃랩 유저
            int gitlabId = ((Long) oauthUser.getMetaData().get("gitlab-id")).intValue();
            User gitlabUser = gitLabApi.getUserApi().getUser(gitlabId);

            //네임스페이스 구하기
            String namespace = gitlabUser.getUsername();
            if (!StringUtils.isEmpty(appCreate.getNamespace())) {
                namespace = appCreate.getNamespace();
            }

            //프로젝트 포크

            //템플릿 프로젝트 정보얻기.
            CategoryItem categoryItem = catalogService.getCategoryItemWithFiles(appCreate.getCategoryItemId());


//            int repoId = Integer.parseInt(environment.getProperty("gitlab.config-repo.projectId"));
//            String definitionFile = gitlabExtentApi.getRepositoryFile(repoId, "master", "template/" + appCreate.getCategoryItemId() + "/definition.json");
//            Map definition = JsonUtils.unmarshal(definitionFile);
//            int tempProjectId = (int) definition.get("projectId");
//            List<Map> mappings = (List) definition.get("mappings");

            //포크하기
            Map forkProject = gitlabExtentApi.forkProject(categoryItem.getProjectId(), namespace);


            projectId = (int) forkProject.get("id");
            Project project = gitLabApi.getProjectApi().getProject(projectId);

            //포크릴레이션 삭제
            gitlabExtentApi.deleteForkRelation(project.getId());

            //레파지토리 이름 변경, 프로젝트 이름 변경, 퍼블릭 변경
            project.setPublic(true);
            project.setVisibility(Visibility.PUBLIC);
            project.setPath(appCreate.getAppName());
            project.setName(appCreate.getAppName());
            gitLabApi.getProjectApi().updateProject(project);

            Map<String, Object> data = appService.getTriggerVariables(appCreate.getAppName());

            //mapping 파일들의 콘텐트를 교체한다.
            List<FileMapping> mappings = categoryItem.getMappings();
            MustacheTemplateEngine templateEngine = new MustacheTemplateEngine();
            for (FileMapping mapping : mappings) {
                String path = mapping.getPath();
                String file = mapping.getFile();

                final String body = templateEngine.executeTemplateText(file, data);
                gitlabExtentApi.updateOrCraeteRepositoryFile(projectId, "master", path, body);
            }


            //러너 등록. 웹훅 등록. 트리거 등록. 마라톤 데피니션 및 스크립트 파일들 복사. 파이프라인 실행. 스테이터스 변경
            //러너 등록.
            int dockerRunnerId = gitlabExtentApi.getDockerRunnerId();
            gitlabExtentApi.enableRunner(projectId, dockerRunnerId);

            //웹훅 등록
            ProjectHook hook = new ProjectHook();
            hook.setPushEvents(true);
            hook.setPipelineEvents(true);
            hook.setEnableSslVerification(false);
            gitLabApi.getProjectApi().addHook(projectId, data.get("UENGINE_CLOUD_URL").toString() + "/hook", hook, false, null);

            //트리거 등록
            gitlabExtentApi.createTrigger(projectId, gitlabUser.getUsername(), "dcosTrigger");

            //마라톤 디플로이 명세 파일 복사
            int repoId = Integer.parseInt(environment.getProperty("gitlab.config-repo.projectId"));
            String[] stages = new String[]{"dev", "stg", "prod"};

            //생성시 결정한 시스템 자원을 반영한다.
            for (String stage : stages) {
                Map deployJson = null;
                String fileName = null;
                switch (stage) {
                    case "prod":
                        deployJson = JsonUtils.unmarshal(categoryItem.getDeployProd());
                        fileName = "ci-deploy-production.json";
                        break;
                    case "dev":
                        deployJson = JsonUtils.unmarshal(categoryItem.getDeployDev());
                        fileName = "ci-deploy-dev.json";
                        break;
                    case "stg":
                        deployJson = JsonUtils.unmarshal(categoryItem.getDeployStg());
                        fileName = "ci-deploy-staging.json";
                        break;
                }
                deployJson.put("cpus", appCreate.getCpu());
                deployJson.put("mem", appCreate.getMem());
                deployJson.put("instances", appCreate.getInstances());
                gitlabExtentApi.updateOrCraeteRepositoryFile(
                        repoId, "master",
                        "deployment/" + data.get("APP_NAME").toString() + "/" + fileName, JsonUtils.marshal(deployJson));
            }

            //스크립트 및 파이프라인 파일 복사
            String[] scripFiles = new String[]{
                    "template/common/ci-deploy-production.sh",
                    "template/common/ci-deploy-staging.sh",
                    "template/common/ci-deploy-dev.sh",
                    "template/common/ci-test.sh",
                    "template/common/ci-pipeline.json"
            };

            for (String scripFile : scripFiles) {
                String scriptText = gitlabExtentApi.getRepositoryFile(repoId, "master", scripFile);
                String[] split = scripFile.split("/");
                String fileName = split[split.length - 1];
                gitlabExtentApi.updateOrCraeteRepositoryFile(
                        repoId, "master", "deployment/" + data.get("APP_NAME").toString() + "/" + fileName, scriptText);
            }


            //클라우드 콘피그 파일 복사
            appService.createAppConfigYml(
                    appCreate.getAppName(),
                    categoryItem.getConfig(),
                    categoryItem.getConfigDev(),
                    categoryItem.getConfigStg(),
                    categoryItem.getConfigProd()
            );

            //dcos projectId 업데이트
            appEntity.setProjectId(projectId);

            //스테이터스 변경
            appEntity.setCreateStatus("repository-create-success");
            appJpaRepository.save(appEntity);


            //파이프라인 트리거 실행.
            Map pipeline = appService.excutePipelineTrigger(appCreate.getAppName(), "master", null);
            pipelineId = (int) pipeline.get("id");


            System.out.println("end");

            logService.addHistory(appCreate.getAppName(), AppLogAction.CREATE_APP, AppLogStatus.SUCCESS, log);

        } catch (Exception ex) {
            ex.printStackTrace();
            try {
                //파이프라인이 있다면 파이프라인 중지.
                if (pipelineId > 0) {
                    gitLabApi.getPipelineApi().cancelPipelineJobs(projectId, pipelineId);
                }

                //프로젝트 삭제
                if (projectId > 0) {
                    gitLabApi.getProjectApi().deleteProject(projectId);
                    //한번 더 삭제 TODO 이상함..
                    try {
                        gitLabApi.getProjectApi().deleteProject(projectId);
                    } catch (Exception exx) {

                    }
                }

                //vcap 서비스, config.yml 삭제
                try {
                    appService.removeAppToVcapService(appCreate.getAppName());
                    appService.removeAppConfigYml(appCreate.getAppName());
                } catch (Exception ee) {

                }

                //스테이터스 실패 등록
                //deployment/create.json 변경.
                Map createMap = new HashMap();
                createMap.put("status", "repository-create-failed");
                createMap.put("definition", JsonUtils.convertClassToMap(appCreate));
                gitlabExtentApi.updateOrCraeteRepositoryFile(
                        Integer.parseInt(environment.getProperty("gitlab.config-repo.projectId")),
                        "master", "deployment/" + appCreate.getAppName() + "/create.json", JsonUtils.marshal(createMap)
                );

                //삭제 시도에 대한 이력
                logService.addHistory(appCreate.getAppName(), AppLogAction.CREATE_APP, AppLogStatus.FAILED, log);

            } catch (Exception e) {
                e.printStackTrace();

                //삭제 시도를 실패했을 경우도 실패 이력
                logService.addHistory(appCreate.getAppName(), AppLogAction.CREATE_APP, AppLogStatus.FAILED, log);
            }
        }
    }
}
