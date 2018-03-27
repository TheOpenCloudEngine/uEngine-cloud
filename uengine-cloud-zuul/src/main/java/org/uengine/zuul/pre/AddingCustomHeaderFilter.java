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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class AddingCustomHeaderFilter extends ZuulFilter {

    public static final String ADD_HEADER = "addHeader";
    private static Logger log = LoggerFactory.getLogger(AddingCustomHeaderFilter.class);

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

        RequestContext ctx = RequestContext.getCurrentContext();
        HttpServletRequest request = ctx.getRequest();

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

                if (path.indexOf(routePath) > -1) {
                    Map<String, String> headerToBeAdded = null;
                    if (routeValueMap.containsKey(ADD_HEADER)) {
                        headerToBeAdded = (Map<String, String>) routeValueMap.get(ADD_HEADER);
                    }

                    if(headerToBeAdded!=null){

                        for(String headerName: headerToBeAdded.keySet()){

                            ctx.addZuulRequestHeader(headerName, headerToBeAdded.get(headerName));

                        }


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