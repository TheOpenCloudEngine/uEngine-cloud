package org.uengine.cloud.app;

import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.models.Member;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.uengine.cloud.scheduler.CronTable;
import org.uengine.cloud.tenant.TenantContext;
import org.uengine.iam.client.model.OauthUser;

import java.util.*;

/**
 * Created by uengine on 2017. 11. 16..
 */
@Service
public class AppAccessLevelRepository {
    @Autowired
    private Environment environment;

    @Autowired
    private GitLabApi gitLabApi;

    @Autowired
    private AppJpaRepository appEntityRepository;

    @Autowired
    private CronTable cronTable;

    //페치 => 모든 앱 목록. (깃랩 멤버 포함)
    //리스트 => 패치 중 내 목록 조회, 아이디 필요
    //한건 => 앱 하나 조회, 아이디 필요
    //업로드 => 앱 하나 조회, 아이디 필요


    /**
     * 모든 앱 리스트를 가져온다.
     *
     * @return
     */
    public List<AppEntity> findAll() {
        //어세스 레벨 매핑
        OauthUser oauthUser = TenantContext.getThreadLocalInstance().getUser();
        List<AppEntity> appEntityList = cronTable.getAppEntityList();

        for (int i = 0; i < appEntityList.size(); i++) {
            this.setAccessLevel(appEntityList.get(i), oauthUser);
        }
        return appEntityList;
    }

//    /**
//     * 접속자에 소속된 앱 리스트를 가져온다.
//     *
//     * @return
//     */
//    public List<AppEntity> findMyAll() {
//        //어세스 레벨 매핑
//        OauthUser oauthUser = TenantContext.getThreadLocalInstance().getUser();
//        List<AppEntity> appEntityList = this.findAll();
//        for (int i = 0; i < appEntityList.size(); i++) {
//            this.setAccessLevel(appEntityList.get(i), oauthUser);
//        }
//
//        //어드민일 경우 전부 리턴, 아닐 경우 어세스레벨 별로 리턴
//        if (oauthUser == null) {
//            return new ArrayList<>();
//        } else if ("admin".equals(oauthUser.getMetaData().get("acl"))) {
//            return appEntityList;
//        } else {
//            List<AppEntity> list = new ArrayList<>();
//            for (int i = 0; i < appEntityList.size(); i++) {
//                if (appEntityList.get(i).getAccessLevel() > 0) {
//                    list.add(appEntityList.get(i));
//                }
//            }
//            return list;
//        }
//    }

    /**
     * 앱 한건을 가져온다.
     *
     * @param appName
     * @return
     */
    public AppEntity findByName(String appName) {
        OauthUser oauthUser = TenantContext.getThreadLocalInstance().getUser();
        AppEntity entity = appEntityRepository.findOne(appName);
        if (entity == null) {
            return null;
        }
        this.addGitlabMember(entity);
        this.setAccessLevel(entity, oauthUser);
        return entity;
    }

    /**
     * 깃랩 멤버데이터에 따라 어세스 레벨을 부여한다.
     *
     * @param appEntity
     * @param oauthUser
     * @return
     */
    public AppEntity setAccessLevel(AppEntity appEntity, OauthUser oauthUser) {
        try {
            appEntity.setAccessLevel(0);
            int gitlabId = ((Long) oauthUser.getMetaData().get("gitlab-id")).intValue();
            List<Member> members = appEntity.getMembers();
            for (int i = 0; i < members.size(); i++) {
                Member member = members.get(i);
                if (member.getId() == gitlabId) {
                    appEntity.setAccessLevel(member.getAccessLevel().toValue());
                }
            }
        } catch (Exception ex) {
            appEntity.setAccessLevel(0);
        }
        return appEntity;
    }

    /**
     * 깃랩 프로젝트 멤버 데이터를 추가한다.
     *
     * @param appEntity
     * @return
     */
    public AppEntity addGitlabMember(AppEntity appEntity) {
        if (appEntity == null) {
            return null;
        }
        int projectId = appEntity.getProjectId();
        try {
            List<Member> members = gitLabApi.getProjectApi().getMembers(projectId);
            appEntity.setMembers(members);
        } catch (Exception ex) {
            appEntity.setMembers(new ArrayList<Member>());
        }
        return appEntity;
    }
}
