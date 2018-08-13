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
import org.uengine.zuul.tenant.TokenContext;

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
        return 2;
    }

    @Override
    public boolean shouldFilter() {
        return true;
    }

    @Override
    public Object run() {
        RuleService ruleService = ApplicationContextRegistry.getApplicationContext().getBean(RuleService.class);
        TokenContext tokenContext = TokenContext.getThreadLocalInstance();

        RequestContext ctx = RequestContext.getCurrentContext();
        HttpServletRequest request = ctx.getRequest();

        //java 처리
        List<String> clientScopes = tokenContext.getScopes();
        boolean iamAuthentication = ruleService.isIamAuthentication();
        String iamJwtKey = ruleService.getIamJwtKey();
        Map route = tokenContext.getRoute();

        if (!iamAuthentication) {
            return null;
        }

        try {
            //만료 시간 체크
            Date expirationTime = tokenContext.getExpirationTime();
            int compareTo = (new Date()).compareTo(expirationTime);
            boolean isEnable = compareTo <= 0;
            if (!isEnable) {
                throw new ZuulRuntimeException(new ZuulException(
                        "Not auth", HttpStatus.UNAUTHORIZED.value(), "The token has expired."));
            }


            //시그네이쳐 체크
            boolean isValid = JwtUtils.validateToken(tokenContext.getToken(), iamJwtKey);
            if (!isValid) {
                throw new ZuulRuntimeException(new ZuulException(
                        "Not auth", HttpStatus.UNAUTHORIZED.value(), "Invalid token signatures."));
            }
        } catch (Exception e) {
            ctx.unset();
            ctx.setResponseStatusCode(HttpStatus.UNAUTHORIZED.value());
        }

        try {
            //라우터 스코프
            Map<String, String> iamScopes = null;
            if (route.containsKey("iam-scopes")) {
                iamScopes = (Map<String, String>) route.get("iam-scopes");
            }
            /*
             * 라우트에 대한 검증 조건.
             * 1. 라우트 패스와 리퀘스트 패스가 일치할 경우
             * 2. 라우트가 스코프 체크를 필요로 할 경우
             * 3. 1,2 에 해당하는 경우, passable 하지 못하면 에러.
             */
            if (iamScopes != null) {
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

                    boolean scopeMatch = false;
                    //모든 스코프 통과
                    if ("guest".equals(scope)) {
                        scopeMatch = true;
                    }
                    //스코프 체크 필요
                    else if (clientScopes.contains(scope)) {
                        scopeMatch = true;
                    }

                    if (scopeMatch && !"guest".equals(scope)) {
                        hasScope = true;
                    }
                    if (methodMatch && scopeMatch) {
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

        } catch (Exception ex) {
            ctx.unset();
            ctx.setResponseStatusCode(HttpStatus.UNAUTHORIZED.value());
        }
        log.info(String.format("%s request to %s", request.getMethod(), request.getRequestURL().toString()));

        return null;
    }
}