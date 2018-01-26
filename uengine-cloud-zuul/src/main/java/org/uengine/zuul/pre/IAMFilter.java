package org.uengine.zuul.pre;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jwt.JWTClaimsSet;
import net.minidev.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.netflix.zuul.util.ZuulRuntimeException;
import org.springframework.http.HttpStatus;
import org.uengine.iam.client.model.OauthUser;
import org.uengine.iam.util.ApplicationContextRegistry;
import org.uengine.iam.util.JsonUtils;
import org.uengine.iam.util.JwtUtils;
import org.uengine.zuul.RuleService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

public class IAMFilter extends ZuulFilter {

    private static Logger log = LoggerFactory.getLogger(IAMFilter.class);

    @Override
    public String filterType() {
        return "pre";
    }

    @Override
    public int filterOrder() {
        return 1;
    }

    @Override
    public boolean shouldFilter() {
        return true;
    }

    @Override
    public Object run() {
        RuleService ruleService = ApplicationContextRegistry.getApplicationContext().getBean(RuleService.class);

        RequestContext ctx = RequestContext.getCurrentContext();
        HttpServletRequest request = ctx.getRequest();
        String token = request.getHeader("access_token");


        //java 처리 후
        String tenantId = null;
        String userName = null;
        OauthUser user = null;
        List<String> clientScopes = new ArrayList<>();
        List<String> userScopes = new ArrayList<>();
        boolean iamAuthentication = ruleService.getIamAuthentication();
        String iamJwtKey = ruleService.getIamJwtKey();

        if (!iamAuthentication) {
            return null;
        }

        try {
            JWSObject jwsObject = JWSObject.parse(token);

            //파싱 부분
            JSONObject jsonPayload = jwsObject.getPayload().toJSONObject();
            JWTClaimsSet jwtClaimsSet = JWTClaimsSet.parse(jsonPayload);
            JSONObject contexts = (JSONObject) jwtClaimsSet.getClaim("context");
            tenantId = userName.split("@")[1];
            user = JsonUtils.convertValue((Map) contexts.get("user"), OauthUser.class);
            clientScopes = (List<String>) contexts.get("scopes");

            if (user.getMetaData().containsKey("scopes")) {
                userScopes = (List<String>) user.getMetaData().get("scopes");
            }

            //만료 시간 체크
            Date expirationTime = jwtClaimsSet.getExpirationTime();
            int compareTo = (new Date()).compareTo(expirationTime);
            boolean isEnable = compareTo <= 0;
            if (!isEnable) {
                throw new ZuulRuntimeException(new ZuulException(
                        "Not auth", HttpStatus.UNAUTHORIZED.value(), "The token has expired."));
            }


            //시그네이쳐 체크
            boolean isValid = JwtUtils.validateToken(token, iamJwtKey);
            if (!isValid) {
                throw new ZuulRuntimeException(new ZuulException(
                        "Not auth", HttpStatus.UNAUTHORIZED.value(), "Invalid token signatures."));
            }


        } catch (Exception e) {
        }

        String requestedUrl = request.getRequestURL().toString();
        try {
            URL url = new URL(requestedUrl);
            String path = url.getPath();
            for (Map routeValueMap : ruleService.getRoutes().values()) {

                String routePath = (String) routeValueMap.get("path");
                if (routePath.endsWith("/**")) {
                    routePath = routePath.substring(0, routePath.length() - 3);
                }

                Map<String, String> iamScopes = null;
                if (routeValueMap.containsKey("iam-scopes")) {
                    iamScopes = (Map<String, String>) routeValueMap.get("iam-scopes");
                }

                boolean iamUserScopesCheck = false;
                if (routeValueMap.containsKey("iam-user-scopes-check")) {
                    iamUserScopesCheck = (boolean) routeValueMap.get("iam-user-scopes-check");
                }


                if (iamScopes != null && path.indexOf(routePath) > -1) {

                    boolean hasScope = false;
                    boolean hasUserScope = false;

                    for (String iamScope : iamScopes.values()) {
                        String[] scopeAndMethod = iamScope.split("/");
                        String scope = scopeAndMethod[0].trim();
                        String methods = scopeAndMethod.length > 1 ? scopeAndMethod[1] : null;

                        if (clientScopes.contains(scope) || "guest".equals(scope)) {
                            hasScope = true;
                            hasUserScope = userScopes.contains(scope);

                            methods = methods.trim();

                            String[] methodsArr = methods.split("-");

                            // allow if there's no method restrictions
                            if (methods == null || methods.length() == 0 || "*".equals(methods)) {

                                // check user also has scope
                                if (!"guest".equals(scope) && iamUserScopesCheck) {
                                    if (userScopes.contains(scope))
                                        return null;

                                } else {
                                    return null;
                                }
                            }


                            //need check method
                            for (String method : methodsArr) {
                                if (method.equals(request.getMethod())) {

                                    // check user also has scope
                                    if (!"guest".equals(scope) && iamUserScopesCheck) {
                                        if (userScopes.contains(scope))
                                            return null;

                                    } else {
                                        return null;
                                    }
                                }
                            }
                        }
                    }

                    // throwing Error
                    if (hasScope && iamUserScopesCheck && hasUserScope) {
                        throw new ZuulRuntimeException(new ZuulException(
                                "Method is not allowed", HttpStatus.METHOD_NOT_ALLOWED.value(), "User has scope, but method " + request.getMethod() + " is not allowed."));


                    } else if (hasScope && !iamUserScopesCheck) {
                        throw new ZuulRuntimeException(new ZuulException(
                                "Method is not allowed", HttpStatus.METHOD_NOT_ALLOWED.value(), "User has scope, but method " + request.getMethod() + " is not allowed."));

                    } else {
                        throw new ZuulRuntimeException(new ZuulException(
                                "Not auth", HttpStatus.UNAUTHORIZED.value(), "User doesn't have required scope."));
                    }

                }
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
//        log.info(String.format("%s request to %s", request.getMethod(), request.getRequestURL().toString()));

        return null;

    }
}