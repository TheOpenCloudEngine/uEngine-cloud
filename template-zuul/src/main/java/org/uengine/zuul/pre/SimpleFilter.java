package org.uengine.zuul.pre;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jwt.JWTClaimsSet;
import net.minidev.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.uengine.iam.util.ApplicationContextRegistry;
import org.uengine.zuul.RuleService;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

public class SimpleFilter extends ZuulFilter {

    private static Logger log = LoggerFactory.getLogger(SimpleFilter.class);

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
            Map<String, Map> routes = ruleService.getRoutes();


            //익스파이어드 시간 체크


            //jwt-private-key 로 verify 체크.


            //이슈어 체크


        } catch (Exception e) {
        }


        log.info(String.format("%s request to %s", request.getMethod(), request.getRequestURL().toString()));

        return null;
    }

}
