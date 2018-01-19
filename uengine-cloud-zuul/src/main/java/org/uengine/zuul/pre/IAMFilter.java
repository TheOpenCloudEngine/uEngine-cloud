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
import org.uengine.iam.util.ApplicationContextRegistry;
import org.uengine.zuul.RuleService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
        String jwt = token;
        JWSObject jwsObject = null;
        String tenantId = null;
        String userName = null;
        Map user = null;
        JSONObject contexts = null;
        Set<String> myScopes = new HashSet<String>();

        try {
            jwsObject = JWSObject.parse(token);

            //파싱 부분
            JSONObject jsonPayload = jwsObject.getPayload().toJSONObject();
            JWTClaimsSet jwtClaimsSet = JWTClaimsSet.parse(jsonPayload);
            contexts = (JSONObject) jwtClaimsSet.getClaim("context");
            userName = (String) contexts.get("userName");
            tenantId = userName.split("@")[1];
            user = (Map) contexts.get("user");
            List<String> scopes = (List<String>) contexts.get("scopes");
            List<String> userScopes = (List<String>) contexts.get("metadata");

            myScopes.addAll(scopes);


            //익스파이어드 시간 체크
            //jwt-private-key 로 verify 체크.
            //이슈어 체크
        } catch (Exception e) {
        }

        String requestedUrl = request.getRequestURL().toString();
        try {
            URL url = new URL(requestedUrl);
            String path = url.getPath();

            for (Map routeValueMap : ruleService.getRoutes().values()) {
                Map<String, String> iamScopes = (Map<String, String>) routeValueMap.get("iam-scopes");

                if (iamScopes != null) {
                    String routePath = (String) routeValueMap.get("path");

                    if (routePath.endsWith("/**")) {
                        routePath = routePath.substring(0, routePath.length() - 3);
                    }

                    boolean hasScope = false;


                    if (path.indexOf(routePath) > -1) {


                        for (String iamScope : iamScopes.values()) {
                            String[] scopeAndMethod = iamScope.split("/");
                            String scope = scopeAndMethod[0].trim();
                            String methods = scopeAndMethod.length > 1 ? scopeAndMethod[1] : null;

                            if (myScopes.contains(scope) || "*".equals(scope)) {

                                hasScope = true;

                                methods = methods.trim();

                                String[] methodsArr = methods.split("-");

                                // allow if there's no method restrictions
                                if(methods==null || methods.length() == 0 || "*".equals(methods))
                                    return null;

                                //need check method
                                for (String method : methodsArr) {
                                    if (method.equals(request.getMethod())) {
                                        return null;
                                    }
                                }
                            }
                        }
                    }

                    // throwing Error

                    if(hasScope) {
                        throw new ZuulRuntimeException(new ZuulException(
                                "Method is not allowed", HttpStatus.METHOD_NOT_ALLOWED.value(), "User has scope, but method "+ request.getMethod()+" is not allowed."));

                    }else {
                        throw new ZuulRuntimeException(new ZuulException(
                                "Not auth", HttpStatus.UNAUTHORIZED.value(), "User doesn't have required scope."));
                    }
//                ctx.unset();
//
//                    if(hasScope) {
//                        ctx.setResponseBody("User has scope, but method "+ request.getMethod()+" is not allowed.");
//                        ctx.setResponseStatusCode(HttpStatus.METHOD_NOT_ALLOWED.value());
//                    }
//                    else {
//                        ctx.setResponseBody("User doesn't have required scope.");
//                        ctx.setResponseStatusCode(HttpStatus.UNAUTHORIZED.value());
//
//                    }


                }
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

//        log.info(String.format("%s request to %s", request.getMethod(), request.getRequestURL().toString()));

        return null;

    }
}