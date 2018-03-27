package org.uengine.zuul.pre;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
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
import org.uengine.iam.util.StringUtils;
import org.uengine.zuul.RuleService;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.util.*;

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

            //If token exist, get metadata
            Map metaData = new HashMap();
            String token = request.getHeader("access_token");
            if (!StringUtils.isEmpty(token)) {
                try {
                    JWSObject jwsObject = JWSObject.parse(token);

                    //파싱 부분
                    JSONObject jsonPayload = jwsObject.getPayload().toJSONObject();
                    JWTClaimsSet jwtClaimsSet = JWTClaimsSet.parse(jsonPayload);
                    JSONObject contexts = (JSONObject) jwtClaimsSet.getClaim("context");
                    OauthUser user = JsonUtils.convertValue(contexts.get("user"), OauthUser.class);
                    metaData = JwtUtils.decodeMetadata(
                            user.getMetaData(),
                            ruleService.getSecureMetadataFields(),
                            ruleService.getMetadataEncoderSecret1(),
                            ruleService.getMetadataEncoderSecret2());
                } catch (ParseException ex) {
                    ex.printStackTrace();
                }
            }

            URL url = new URL(requestedUrl);
            String path = url.getPath();

            for (String key : ruleService.getRoutes().keySet()) {

                Map routeValueMap = ruleService.getRoutes().get(key);

                String routePath = key;

                if (routeValueMap.containsKey("path")) {

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

                    if (headerToBeAdded != null) {
                        for (String headerName : headerToBeAdded.keySet()) {
                            String headerValue = headerToBeAdded.get(headerName);

                            //is JsonPath?
                            if (headerValue.startsWith("$")) {
                                Object json = Configuration.defaultConfiguration().jsonProvider().parse(JsonUtils.marshal(metaData));
                                headerValue = JsonPath.read(json, headerValue);
                            }

                            ctx.addZuulRequestHeader(headerName, headerValue);
                        }
                    }


                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        log.info(String.format("%s request to %s", request.getMethod(), request.getRequestURL().toString()));

        return null;

    }
}