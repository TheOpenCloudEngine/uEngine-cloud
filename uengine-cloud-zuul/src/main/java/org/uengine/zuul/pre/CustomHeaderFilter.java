package org.uengine.zuul.pre;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jwt.JWTClaimsSet;
import net.minidev.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uengine.iam.client.model.OauthUser;
import org.uengine.iam.util.ApplicationContextRegistry;
import org.uengine.iam.util.JsonUtils;
import org.uengine.iam.util.JwtUtils;
import org.uengine.iam.util.StringUtils;
import org.uengine.zuul.RuleService;
import org.uengine.zuul.tenant.TokenContext;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.URL;
import java.text.ParseException;
import java.util.*;

public class CustomHeaderFilter extends ZuulFilter {

    public static final String ADD_HEADER = "addHeader";
    private static Logger log = LoggerFactory.getLogger(CustomHeaderFilter.class);

    @Override
    public String filterType() {
        return "pre";
    }

    @Override
    public int filterOrder() {
        return 3;
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
            OauthUser user = TokenContext.getThreadLocalInstance().getUser();
            String token = TokenContext.getThreadLocalInstance().getToken();

            Map<String, Object> metaData = null;
            if (!StringUtils.isEmpty(token)) {
                try {
                    metaData = JwtUtils.decodeMetadata(
                            user.getMetaData(),
                            ruleService.getSecureMetadataFields(),
                            ruleService.getMetadataEncoderSecret1(),
                            ruleService.getMetadataEncoderSecret2());
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

            Map route = TokenContext.getThreadLocalInstance().getRoute();
            Map<String, String> headerToBeAdded = null;
            if (route.containsKey(ADD_HEADER)) {
                headerToBeAdded = (Map<String, String>) route.get(ADD_HEADER);
            }

            if (headerToBeAdded != null) {
                for (String headerName : headerToBeAdded.keySet()) {
                    String headerValue = headerToBeAdded.get(headerName);

                    //is JsonPath?
                    if (headerValue.startsWith("$") && metaData != null) {
                        Object json = Configuration.defaultConfiguration().jsonProvider().parse(JsonUtils.marshal(metaData));
                        headerValue = JsonPath.read(json, headerValue);
                    }
                    ctx.addZuulRequestHeader(headerName, headerValue);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        log.info(String.format("%s request to %s", request.getMethod(), request.getRequestURL().toString()));

        return null;

    }
}