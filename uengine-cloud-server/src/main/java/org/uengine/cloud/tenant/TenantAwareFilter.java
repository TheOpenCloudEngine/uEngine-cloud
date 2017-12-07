package org.uengine.cloud.tenant;

import com.nimbusds.jose.JWSObject;
import com.nimbusds.jwt.JWTClaimsSet;
import net.minidev.json.JSONObject;
import org.springframework.http.HttpMethod;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Map;

/**
 * Created by uengine on 2017. 6. 12..
 */
@WebFilter
//@Component
public class TenantAwareFilter implements Filter {

    public TenantAwareFilter() {
        setAllowAnonymousTenant(true);
    }

    boolean allowAnonymousTenant;

    public boolean isAllowAnonymousTenant() {
        return allowAnonymousTenant;
    }

    /**
     * @param allowAnonymousTenant enable anonymously accessing tenant that doesn't have any token information access. for testing or some purposes.
     */
    public void setAllowAnonymousTenant(boolean allowAnonymousTenant) {
        this.allowAnonymousTenant = allowAnonymousTenant;
    }


    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        //String token = ((HttpServletRequest)servletRequest).getParameter("access_token");
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        if (request.getMethod().equals(HttpMethod.OPTIONS.toString())) {
            filterChain.doFilter(servletRequest, servletResponse);
        } else {
            String token = ((HttpServletRequest) servletRequest).getHeader("access_token");

            //java 처리 후
            String jwt = token;

            JWSObject jwsObject = null;
            String tenantId = null;
            String userName = null;
            Map user = null;
            JSONObject contexts = null;
            try {
                jwsObject = JWSObject.parse(token);

                JSONObject jsonPayload = jwsObject.getPayload().toJSONObject();
                JWTClaimsSet jwtClaimsSet = JWTClaimsSet.parse(jsonPayload);

                contexts = (JSONObject) jwtClaimsSet.getClaim("context");
                userName = (String) contexts.get("userName");
                tenantId = userName.split("@")[1];
                user = (Map) contexts.get("user");

            } catch (Exception e) {
                if (isAllowAnonymousTenant()) {
                    new TenantContext("anonymous");
                    TenantContext.getThreadLocalInstance().setUserId("anonymous");

                    filterChain.doFilter(servletRequest, servletResponse);
                    return;
                } else {
                    filterChain.doFilter(servletRequest, servletResponse);
                    return;
                }

            }

            new TenantContext(tenantId);
            TenantContext.getThreadLocalInstance().setUserId(userName);
            TenantContext.getThreadLocalInstance().setUser(user);
            filterChain.doFilter(servletRequest, servletResponse);
        }
    }

    @Override
    public void destroy() {

    }
}
