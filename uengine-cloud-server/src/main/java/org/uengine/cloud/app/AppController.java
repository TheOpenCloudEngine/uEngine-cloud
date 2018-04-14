package org.uengine.cloud.app;


import org.gitlab4j.api.models.Member;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.uengine.cloud.app.log.AppLogAction;
import org.uengine.cloud.app.log.AppLogService;
import org.uengine.cloud.app.log.AppLogStatus;
import org.uengine.cloud.tenant.TenantContext;
import org.uengine.iam.client.model.OauthUser;
import org.uengine.iam.util.JsonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by uengine on 2017. 11. 14..
 */
@RestController
@RequestMapping("/app")
public class AppController {

    @Autowired
    private AppWebService appWebService;

    @Autowired
    private AppWebCacheService appWebCacheService;

    @Autowired
    private AppLogService logService;

    @Autowired
    private AppEntityRepository appEntityRepository;

    private static final Logger LOGGER = LoggerFactory.getLogger(AppController.class);

    /**
     * 앱의 도커 이미지 목록을 가져온다.
     *
     * @param request
     * @param response
     * @param appName  앱 이름
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/{appName}/tags", method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
    public Map getAppRegistryTags(HttpServletRequest request,
                                  HttpServletResponse response,
                                  @PathVariable("appName") String appName
    ) throws Exception {
        return appWebService.getAppRegistryTags(appName);
    }

    //get app list 추가. (pageable, oauth user)
    //admin 일 경우 gitlabid 빈값 호출.

    /**
     * 앱 목록을 가져온다.
     *
     * @param request
     * @param response
     * @param name
     * @param pageable
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "", method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
    public Page<AppEntity> getApps(HttpServletRequest request,
                                   HttpServletResponse response,
                                   @RequestParam(required = false, value = "name", defaultValue = "") String name,
                                   @PageableDefault Pageable pageable
    ) throws Exception {
        OauthUser user = TenantContext.getThreadLocalInstance().getUser();
        String acl = user.getMetaData().get("acl").toString();
        int gitlabId = (int) user.getMetaData().get("gitlab-id");

        Page<AppEntity> appEntities = null;
        if ("admin".equals(acl)) {
            appEntities = appEntityRepository.findLikeNameAndGitlabId(name, "", pageable);
        } else {
            appEntities = appEntityRepository.findLikeNameAndGitlabId(name, "m" + gitlabId + "m", pageable);
        }

        //TODO remove if performance down.
        if (!appEntities.getContent().isEmpty()) {
            for (AppEntity appEntity : appEntities.getContent()) {
                appWebService.setAccessLevel(appEntity, user);
            }
        }
        return appEntities;
    }

    /**
     * 앱 정보를 가져온다.
     *
     * @param request
     * @param response
     * @param appName  앱 이름
     * @return
     * @throws Exception
     */
//    @Cacheable(value = "app", key = "#appName")
    @RequestMapping(value = "/{appName}", method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
    public AppEntity getApp(HttpServletRequest request,
                      HttpServletResponse response,
                      @PathVariable("appName") String appName
    ) throws Exception {
        OauthUser user = TenantContext.getThreadLocalInstance().getUser();
        AppEntity appEntity = appWebCacheService.findOneCache(appName);
        appEntity = appWebService.setAccessLevel(appEntity, user);
        String acl = user.getMetaData().get("acl").toString();

        if ("admin".equals(acl) || appEntity.getAccessLevel() > 0) {
            //return JsonUtils.convertClassToMap(appEntity);
            return appEntity;
        }

        LOGGER.warn("Unauthorized getApp request for {} , {}", appName, user.getUserName());
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
        return null;
    }

    /**
     * 앱의 멤버 정보를 가져온다.
     *
     * @param request
     * @param response
     * @param appName  앱 이름
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/{appName}/member", method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
    public List<Member> getAppMember(HttpServletRequest request,
                      HttpServletResponse response,
                      @PathVariable("appName") String appName
    ) throws Exception {
        OauthUser user = TenantContext.getThreadLocalInstance().getUser();
        AppEntity appEntity = appWebCacheService.findOneCache(appName);
        List<Member> members = appWebCacheService.getAppMemberCache(appName);

        appEntity = appWebService.setAccessLevel(appEntity, user);
        String acl = user.getMetaData().get("acl").toString();

        if ("admin".equals(acl) || appEntity.getAccessLevel() > 0) {
            return members;
        }

        LOGGER.warn("Unauthorized getApp members request for {} , {}", appName, user.getUserName());
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
        return null;
    }

    /**
     * 앱 정보를 업데이트한다.
     *
     * @param request
     * @param response
     * @param appName   앱 이름
     * @param appEntity 앱 정보 내용
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/{appName}", method = RequestMethod.PUT, produces = "application/json;charset=UTF-8")
    public Map updateApp(HttpServletRequest request,
                         HttpServletResponse response,
                         @PathVariable("appName") String appName,
                         @RequestBody Map appEntity,
                         @RequestParam(value = "excludeDeploy", defaultValue = "false") boolean excludeDeploy
    ) throws Exception {
        try {
            AppEntity entity = JsonUtils.convertValue(appEntity, AppEntity.class);
            entity = appWebService.save(entity, true);

            logService.addHistory(appName, AppLogAction.UPDATE_APP, AppLogStatus.SUCCESS, null);
            return JsonUtils.convertClassToMap(entity);
        } catch (Exception ex) {
            logService.addHistory(appName, AppLogAction.UPDATE_APP, AppLogStatus.FAILED, null);
            throw ex;
        }
    }

    /**
     * 앱을 삭제한다.
     *
     * @param request
     * @param response
     * @param appName          앱 이름
     * @param removeRepository 깃랩 프로젝트 삭제 여부
     * @throws Exception
     */
    @RequestMapping(value = "/{appName}", method = RequestMethod.DELETE, produces = "application/json;charset=UTF-8")
    public void deleteApp(HttpServletRequest request,
                          HttpServletResponse response,
                          @PathVariable("appName") String appName,
                          @RequestParam(value = "removeRepository", defaultValue = "true") boolean removeRepository
    ) throws Exception {
        Map log = new HashMap();
        log.put("removeRepository", removeRepository);

        try {
            appWebService.deleteApp(appName, removeRepository);
            response.setStatus(200);

            logService.addHistory(appName, AppLogAction.DELETE_APP, AppLogStatus.SUCCESS, log);
        } catch (Exception ex) {
            logService.addHistory(appName, AppLogAction.DELETE_APP, AppLogStatus.FAILED, log);
            throw ex;
        }
    }

    /**
     * 앱을 생성한다.
     *
     * @param request
     * @param response
     * @param appCreate 앱 생성 내용
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    public Map createApp(HttpServletRequest request,
                         HttpServletResponse response,
                         @RequestBody AppCreate appCreate) throws Exception {

        Map<String, Object> log = JsonUtils.convertClassToMap(appCreate);
        try {
            AppEntity appEntity = appWebService.createApp(appCreate);
            logService.addHistory(appCreate.getAppName(), AppLogAction.CREATE_APP_REQUEST, AppLogStatus.SUCCESS, log);
            return JsonUtils.convertClassToMap(appEntity);
        } catch (Exception ex) {
            logService.addHistory(appCreate.getAppName(), AppLogAction.CREATE_APP_REQUEST, AppLogStatus.FAILED, log);
            throw ex;
        }
    }

}

