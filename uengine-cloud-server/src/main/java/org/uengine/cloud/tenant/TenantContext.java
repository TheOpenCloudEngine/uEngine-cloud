package org.uengine.cloud.tenant;

import java.util.Map;

/**
 * Created by uengine on 2017. 11. 14..
 */
public class TenantContext {
    static ThreadLocal<TenantContext> local = new ThreadLocal();
    String tenantId;
    String userId;
    Map user;

    public String getTenantId() {
        return this.tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getUserId() {
        return this.userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Map getUser() {
        return user;
    }

    public void setUser(Map user) {
        this.user = user;
    }

    public TenantContext(String tenantId) {
        this.tenantId = tenantId;
        local.set(this);
    }

    public static TenantContext getThreadLocalInstance() {
        TenantContext tc = (TenantContext) local.get();
        return tc != null ? tc : new TenantContext((String) null);
    }
}
