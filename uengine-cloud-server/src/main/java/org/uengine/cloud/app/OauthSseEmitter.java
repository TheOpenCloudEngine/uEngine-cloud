package org.uengine.cloud.app;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.uengine.iam.client.model.OauthUser;

public class OauthSseEmitter extends SseEmitter {

    private OauthUser oauthUser;
    private String acl;
    private int gitlabId;

    public OauthSseEmitter(Long timeout, OauthUser oauthUser) {
        super(timeout);
        this.oauthUser = oauthUser;
        try {
            this.acl = oauthUser.getMetaData().get("acl").toString();
        } catch (Exception ex) {
            this.acl = null;
        }

        try {
            this.gitlabId = ((Long) oauthUser.getMetaData().get("gitlab-id")).intValue();
        } catch (Exception ex) {
            this.gitlabId = 0;
        }

    }

    public OauthUser getOauthUser() {
        return oauthUser;
    }

    public void setOauthUser(OauthUser oauthUser) {
        this.oauthUser = oauthUser;
    }

    public String getAcl() {
        return acl;
    }

    public void setAcl(String acl) {
        this.acl = acl;
    }

    public int getGitlabId() {
        return gitlabId;
    }

    public void setGitlabId(int gitlabId) {
        this.gitlabId = gitlabId;
    }
}
