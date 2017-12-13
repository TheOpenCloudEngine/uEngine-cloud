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
import org.opencloudengine.garuda.client.IamClient;
import org.opencloudengine.garuda.client.model.OauthUser;
import org.opencloudengine.garuda.util.ApplicationContextRegistry;
import org.opencloudengine.garuda.util.JsonUtils;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.core.env.Environment;
import org.uengine.cloud.app.AppCreate;
import org.uengine.cloud.app.AppService;
import org.uengine.cloud.app.GitlabExtentApi;
import org.uengine.cloud.templates.MustacheTemplateEngine;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DefaultDcosJob implements Job {

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        // 클라이언트 잡 실행시 필요한 정보를 가져온다.
        JobDataMap map = jobExecutionContext.getMergedJobDataMap();
        int pipelineId = 0;
        int projectId = 0;
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        AppCreate appCreate = new ObjectMapper().convertValue(map, AppCreate.class);


        Environment environment = ApplicationContextRegistry.getApplicationContext().getBean(Environment.class);
        AppService appService = ApplicationContextRegistry.getApplicationContext().getBean(AppService.class);
        GitLabApi gitLabApi = ApplicationContextRegistry.getApplicationContext().getBean(GitLabApi.class);
        GitlabExtentApi gitlabExtentApi = ApplicationContextRegistry.getApplicationContext().getBean(GitlabExtentApi.class);

        try {
            //DCOS 앱 찾기
            Map app = appService.getAppByName(appCreate.getAppName());

            //gitlabUsername 는 이메일에서 @ 앞부분(깃랩에서 허용하지 않기 때문에)
            String gitlabUsername = app.get("owner").toString();

            //iamUserName 은 IAM 유저네임
            String iamUserName = app.get("iam").toString();

            //name 은 사람 이름(Iam 에 name 필드가 있다고 가정)
            String gitlabName = appCreate.getUser().get("name").toString();


            //IAM 유저정보(패스워드 포함)
            IamClient iamClient = new IamClient(environment.getProperty("iam.host"),
                    Integer.parseInt(environment.getProperty("iam.port")),
                    environment.getProperty("iam.clientId"),
                    environment.getProperty("iam.clientSecret"));
            OauthUser oauthUser = iamClient.getUserByName(iamUserName);

            //깃랩 사용자 등록
            User user = null;
            try {
                user = gitLabApi.getUserApi().getUser(gitlabUsername);
            } catch (Exception ex) {

            }

            //깃랩 사용자가 없다면 생성.
            if (user == null) {
                Map userMap = new HashMap();
                userMap.put("name", gitlabName);
                userMap.put("username", gitlabUsername);
                userMap.put("email", iamUserName);
                userMap.put("password", oauthUser.getUserPassword());
                userMap.put("skip_confirmation", true);
                Map created = gitlabExtentApi.createUser(userMap);
            }

            //프로젝트 포크

            //템플릿 프로젝트 정보얻기.
            int repoId = Integer.parseInt(environment.getProperty("gitlab.config-repo.projectId"));
            String definitionFile = gitlabExtentApi.getRepositoryFile(repoId, "master", "template/" + appCreate.getCategoryItemId() + "/definition.json");
            Map definition = JsonUtils.unmarshal(definitionFile);
            int tempProjectId = (int) definition.get("projectId");
            List<Map> mappings = (List) definition.get("mappings");

            //포크하기
            Map forkProject = gitlabExtentApi.forkProject(tempProjectId, gitlabUsername);
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
            MustacheTemplateEngine templateEngine = new MustacheTemplateEngine();
            for (Map mapping : mappings) {
                String path = mapping.get("path").toString();
                String file = mapping.get("file").toString();

                String templateText = gitlabExtentApi.getRepositoryFile(repoId, "master", "template/" + appCreate.getCategoryItemId() + "/" + file);
                final String body = templateEngine.executeTemplateText(templateText, data);
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
            gitlabExtentApi.createTrigger(projectId, gitlabUsername, "dcosTrigger");

            String[] deployFiles = null;
            //마라톤 디플로이 명세 파일 복사
            if (appCreate.getCategoryItemId().equals("springboot")) {
                deployFiles = new String[]{
                        "template/springboot/file/ci-deploy-production.json",
                        "template/springboot/file/ci-deploy-staging.json",
                        "template/springboot/file/ci-deploy-dev.json"
                };
            } else {
                deployFiles = new String[]{
                        "template/polyglot/file/ci-deploy-production.json",
                        "template/polyglot/file/ci-deploy-staging.json",
                        "template/polyglot/file/ci-deploy-dev.json"
                };
            }

            //생성시 결정한 시스템 자원을 반영한다.
            for (String deployFile : deployFiles) {
                String deployText = gitlabExtentApi.getRepositoryFile(repoId, "master", deployFile);
                Map deployJson = JsonUtils.unmarshal(deployText);
                deployJson.put("cpus", appCreate.getCpu());
                deployJson.put("mem", appCreate.getMem());
                deployJson.put("instances", appCreate.getInstances());

                String[] split = deployFile.split("/");
                String fileName = split[split.length - 1];
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


            //dcos projectId 업데이트
            Map dcosMap = appService.getDcosMap();
            ((Map) app.get("gitlab")).put("projectId", projectId);
            ((Map) ((Map) dcosMap.get("dcos")).get("apps")).put(appCreate.getAppName(), app);
            appService.saveDcosYaml(dcosMap);

            //스테이터스 변경
            //deployment/create.json 변경.
            Map createMap = new HashMap();
            createMap.put("status", "repository-create-success");
            createMap.put("definition", JsonUtils.convertClassToMap(appCreate));
            gitlabExtentApi.updateOrCraeteRepositoryFile(
                    Integer.parseInt(environment.getProperty("gitlab.config-repo.projectId")),
                    "master", "deployment/" + appCreate.getAppName() + "/create.json", JsonUtils.marshal(createMap)
            );

            //파이프라인 트리거 실행.
            Map pipeline = appService.excutePipelineTrigger(appCreate.getAppName(), "master", null);
            pipelineId = (int) pipeline.get("id");


            System.out.println("end");
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

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
