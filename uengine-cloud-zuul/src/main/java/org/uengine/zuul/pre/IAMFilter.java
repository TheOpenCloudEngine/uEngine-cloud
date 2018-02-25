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


        //java 처리
        List<String> clientScopes = new ArrayList<>();
        List<String> userScopes = new ArrayList<>();
        boolean iamAuthentication = ruleService.getIamAuthentication();
        String iamJwtKey = ruleService.getIamJwtKey();
        String userName = null;

        if (!iamAuthentication) {
            return null;
        }

        try {
            JWSObject jwsObject = JWSObject.parse(token);

            //파싱 부분
            JSONObject jsonPayload = jwsObject.getPayload().toJSONObject();
            JWTClaimsSet jwtClaimsSet = JWTClaimsSet.parse(jsonPayload);
            JSONObject contexts = (JSONObject) jwtClaimsSet.getClaim("context");
            OauthUser user = JsonUtils.convertValue((Map) contexts.get("user"), OauthUser.class);
            userName = user.getUserName();

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

            for (String key : ruleService.getRoutes().keySet()) {

                Map routeValueMap = ruleService.getRoutes().get(key);

                String routePath = key;

                if(routeValueMap.containsKey("path")){

                    routePath = (String) routeValueMap.get("path");

                    //라우터 패스
                    if (routePath.endsWith("/**")) {
                        routePath = routePath.substring(0, routePath.length() - 3);
                    }

                }

                //라우터 스코프
                Map<String, String> iamScopes = null;
                if (routeValueMap.containsKey("iam-scopes")) {
                    iamScopes = (Map<String, String>) routeValueMap.get("iam-scopes");
                }

                //라우터 유저 스코프 체크 여부
                boolean iamUserScopesCheck = false;
                if (routeValueMap.containsKey("iam-user-scopes-check")) {
                    iamUserScopesCheck = (boolean) routeValueMap.get("iam-user-scopes-check");
                }

                /*
                 * 라우트에 대한 검증 조건.
                 * 1. 라우트 패스와 리퀘스트 패스가 일치할 경우
                 * 2. 라우트가 스코프 체크를 필요로 할 경우
                 * 3. 1,2 에 해당하는 경우, passable 하지 못하면 에러.
                 */
                if (path.indexOf(routePath) > -1 && iamScopes != null) {
                    boolean passable = false;
                    boolean hasScope = false;

                    //라우트에 명시된 스코프/메소드 마다 루프
                    //스코프/메소드 조건 중 하나라도 해당된다면 라우드에 대해 passable 하다.
                    for (String iamScope : iamScopes.values()) {

                        //필요한 스콥
                        String[] scopeAndMethod = iamScope.split("/");
                        String scope = scopeAndMethod[0].trim();

                        //필요한 메소드 목록
                        String methods = scopeAndMethod.length > 1 ? scopeAndMethod[1] : "";
                        methods = methods.trim();
                        String[] methodsArr = methods.split("-");


                        boolean methodMatch = false;
                        //모든 메소드 허용 체크
                        if ("*".equals(methods)) {
                            methodMatch = true;
                        }
                        //메소드 체크 필요
                        else {
                            for (String method : methodsArr) {
                                if (method.equals(request.getMethod())) {
                                    methodMatch = true;
                                }
                            }
                        }

                        boolean scopeMath = false;
                        //모든 스코프 통과
                        if ("guest".equals(scope)) {
                            scopeMath = true;
                        }
                        //스코프 체크 필요
                        else {
                            //사용자 스코프 체크가 필요없는경우
                            if (clientScopes.contains(scope) && !iamUserScopesCheck) {
                                scopeMath = true;
                            }

                            //사용자 스코프 체크가 필요한 경우
                            else if (clientScopes.contains(scope) && iamUserScopesCheck) {
                                if (userScopes.contains(scope)) {
                                    scopeMath = true;
                                }
                            }
                        }

                        if (scopeMath && !"guest".equals(scope)) {
                            hasScope = true;
                        }
                        if (methodMatch && scopeMath) {
                            passable = true;
                        }
                    }

                    // if passable, end filter.
                    if (passable) {
                        return null;
                    }

                    // throwing Error
                    if (hasScope) {
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
        log.info(String.format("%s request to %s", request.getMethod(), request.getRequestURL().toString()));

        return null;

    }
}