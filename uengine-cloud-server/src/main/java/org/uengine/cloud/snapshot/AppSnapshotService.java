package org.uengine.cloud.snapshot;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.common.base.Joiner;
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

    @Autowired
    private AppJpaRepository appJpaRepository;

    /**
     * 스냅샷으로부터 요청된 스테이지에 한해 앱을 복원한다.
     *
     * @param snapshotId
     * @param stages
     * @param overrideResource
     * @return
     * @throws Exception
     */
    public boolean restoreSnapshot(Long snapshotId, List<String> stages, AppConfigYmlResource overrideResource, boolean redeploy) throws Exception {

        //스냅샷 복원 검증
        AppSnapshot appSnapshot = this.validateRestoreSnapshot(snapshotId, stages);
        String appName = appSnapshot.getAppName();
        AppEntity appEntity = appJpaRepository.findOne(appName);

        //스냅샷으로부터 external 호스트를 가져와 vcap 서비스로 적용한다.
        for (String stage : stages) {
            AppStage appStage = null;
            switch (stage) {
                case "dev":
                    appStage = appEntity.getDev();
                    appStage.setExternal(appSnapshot.getApp().getDev().getExternal());
                    appEntity.setDev(appStage);
                    break;
                case "stg":
                    appStage = appEntity.getStg();
                    appStage.setExternal(appSnapshot.getApp().getStg().getExternal());
                    appEntity.setStg(appStage);
                    break;
                case "prod":
                    appStage = appEntity.getProd();
                    appStage.setExternal(appSnapshot.getApp().getProd().getExternal());
                    appEntity.setProd(appStage);
                    break;
            }
        }
        appService.updateAppExcludeDeployJson(appName, appEntity);

        //리소스 수정: overrideResource 존재시 덮어씀.
        AppConfigYmlResource configYmlResource = overrideResource == null ? appSnapshot.getAppConfigYmlResource() : overrideResource;

        //변경할 스테이지가 있다면 common 콘피그를 바꾼다.
        if (!stages.isEmpty()) {
            appService.updateAppConfigYml(appSnapshot.getAppName(), configYmlResource.getCommonYml(), null);
        }

        //스냡샷으로부터 리소스를 복원한다.
        for (String stage : stages) {
            switch (stage) {
                case "dev":
                    appService.updateAppConfigYml(appSnapshot.getAppName(), configYmlResource.getDevYml(), stage);
                    appService.updateDeployJson(appSnapshot.getAppName(), stage, configYmlResource.getMesosDev());
                    break;
                case "stg":
                    appService.updateAppConfigYml(appSnapshot.getAppName(), configYmlResource.getStgYml(), stage);
                    appService.updateDeployJson(appSnapshot.getAppName(), stage, configYmlResource.getMesosStg());
                    break;
                case "prod":
                    appService.updateAppConfigYml(appSnapshot.getAppName(), configYmlResource.getProdYml(), stage);
                    appService.updateDeployJson(appSnapshot.getAppName(), stage, configYmlResource.getMesosProd());
                    break;
            }

            //과거에 활성화 가능했던 스테이지인지 확인한다.
            String commit = this.getCommitRefFromSnapshot(appSnapshot, stage);
            if (redeploy) {
                //커밋이 없다면 현재 앱을 삭제하도록 한다.
                if (StringUtils.isEmpty(commit)) {
                    appService.removeDeployedApp(appName, stage);
                }

                //커밋이 있다면 앱을 배포한다.
                else {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                    String dateString = dateFormat.format(new Date());
                    String name = String.format("%s Auto deployment from Snapshot %s, %s %s", dateString, appSnapshot.getId(), appName, stage);
                    appService.runDeployedApp(appName, stage, commit, appSnapshot.getId(), true, name, null);
                }
            }
        }
        return true;
    }

    /**
     * 스냅샷으로부터 주어진 스테이지에 해당하는 커밋 레퍼런스를 가져온다.
     *
     * @param appSnapshot
     * @param stage
     * @return
     * @throws Exception
     */
    public String getCommitRefFromSnapshot(AppSnapshot appSnapshot, String stage) throws Exception {
        AppEntity app = appSnapshot.getApp();
        AppStage appStage = appService.getAppStage(app, stage);

        Map mesos = appStage.getMesos();

        //mesos 가 null 이면 스냅샷 당시 배포된 어플리케이션이 없다.
        if (mesos == null) {
            return null;
        }
        Map docker = (Map) ((Map) mesos.get("container")).get("docker");
        String image = docker.get("image").toString();
        return image.substring(image.lastIndexOf(":") + 1);
    }

    /**
     * 스냅샷 복원을 검증한다.
     *
     * @param snapshotId
     * @param stages     복원할 스테이지 목록
     * @return
     * @throws Exception
     */
    public AppSnapshot validateRestoreSnapshot(Long snapshotId, List<String> stages) throws Exception {

        //config 파일, mesos 설정 diff 확인.
        //공통 config 파일 바뀜 확인 - 그룹 스냅샷 복원인 경우 공통파일은 동일하므로 한번만 바꾸면 된다.

        //스냅샷 설정과 현재 설정이 바뀐점 확인시킴.
        //스냡샷 설정 <= 현재 설정 머지시키는 UI
        //결과물은 최종 스냅샷 리소스. - 이하 리소스 오버라이드라 칭함.
        //리소스 오버라이드가 없다면 모두 덮어쓰고, 따로 리소스 오버라이드가 있다면 제공된 오버라이드로 덮어쓴다.

        //스냡샷을 가져온다.
        AppSnapshot appSnapshot = snapshotRepository.findOne(snapshotId);
        if (appSnapshot == null) {
            throw new Exception(String.format("Not Found appSnapshot, %s", snapshotId));
        }

        //앱이 존재하는지 확인한다.
        String appName = appSnapshot.getApp().getName();
        AppEntity existApp = appJpaRepository.findOne(appName);
        if (existApp == null) {
            throw new Exception(String.format("Not Found application %s in appSnapshot, %s", appName, snapshotId));
        }

        //스테이지 String 이 맞는지 확인한다.
        String[] correctStages = new String[]{"dev", "stg", "prod"};
        for (String stage : stages) {
            if (!Arrays.asList(correctStages).contains(stage)) {
                throw new Exception(String.format("invalid restore stage , snapshot %s, stage %s", snapshotId, stage));
            }
        }

        //요청된 스테이지마다, 스냅샷에 저장된 활성 상태의 도커 이미지가 있다면, 이미지가 존재하는지 검증한다.
        String s = appSnapshot.getActiveStages() != null ? appSnapshot.getActiveStages() : "";
        String[] activeStages = s.split(",");
        for (String stage : stages) {

            //요청된 스테이지의 스냅샷이 과거에 활성화된 상태였다면 이미지 검색
            if (Arrays.asList(activeStages).contains(stage)) {
                try {
                    String commit = this.getCommitRefFromSnapshot(appSnapshot, stage);
                    Map tagMap = appService.getAppRegistryTags(appSnapshot.getAppName());
                    List<String> tags = (List<String>) tagMap.get("tags");
                    if (!tags.contains(commit)) {
                        throw new Exception(String.format("Not Found docker image for " +
                                "commit %s, snapshotId %s, appName %s, stage %s", commit, snapshotId, appSnapshot.getAppName(), stage));
                    }
                } catch (Exception ex) {
                    throw new Exception(String.format("Not Found docker image for " +
                            "snapshotId %s, appName %s, stage %s", snapshotId, appSnapshot.getAppName(), stage));
                }
            }
        }

        //TODO 리소스 가용성 확인
        return appSnapshot;
    }

    /**
     * 스냅샷을 생선한다.
     *
     * @param appName
     * @param snapshotName
     * @param appGroupSnapshotId
     * @return
     * @throws Exception
     */
    public AppSnapshot createSnapshot(String appName, String snapshotName, Long appGroupSnapshotId) throws Exception {
        //deployJson 을 포함한 app 정보를 가져온다.
        AppEntity appEntity = appService.getAppIncludeDeployJson(appName);

        //리소스를 가져온다.
        AppConfigYmlResource configYmlResource = this.createAppConfigSnapshot(appName, appEntity);

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
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            String dateString = dateFormat.format(new Date());
            snapshotName = String.format("%s %s Snapshot", dateString, appName);
        }
        snapshot.setName(snapshotName);

        //액티브 스테이지를 기록한다.
        ArrayList<String> activeStages = new ArrayList<>();
        if (this.getCommitRefFromSnapshot(snapshot, "dev") != null) {
            activeStages.add("dev");
        }
        if (this.getCommitRefFromSnapshot(snapshot, "stg") != null) {
            activeStages.add("stg");
        }
        if (this.getCommitRefFromSnapshot(snapshot, "prod") != null) {
            activeStages.add("prod");
        }
        if (activeStages.size() > 0) {
            snapshot.setActiveStages(Joiner.on(",").join(activeStages));
        }

        return snapshotRepository.save(snapshot);
    }

    public AppConfigYmlResource createAppConfigSnapshot(String appName, AppEntity appEntity) throws Exception {
        AppConfigYmlResource configYmlResource = new AppConfigYmlResource();

        configYmlResource.setApplication(appService.getApplicationYml());
        configYmlResource.setCommonYml(appService.getAppConfigYml(appName, null));
        configYmlResource.setDevYml(appService.getAppConfigYml(appName, "dev"));
        configYmlResource.setStgYml(appService.getAppConfigYml(appName, "stg"));
        configYmlResource.setProdYml(appService.getAppConfigYml(appName, "prod"));

        AppStage dev = appEntity.getDev();
        configYmlResource.setMesosDev(dev.getDeployJson());

        AppStage stg = appEntity.getStg();
        configYmlResource.setMesosStg(stg.getDeployJson());

        AppStage prod = appEntity.getProd();
        configYmlResource.setMesosProd(prod.getDeployJson());

        return configYmlResource;
    }
}
