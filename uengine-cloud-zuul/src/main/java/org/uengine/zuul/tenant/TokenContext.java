package org.uengine.zuul.tenant;

import com.netflix.zuul.exception.ZuulException;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jwt.JWTClaimsSet;
import lombok.Data;
import net.minidev.json.JSONObject;
import org.springframework.cloud.netflix.zuul.filters.RouteLocator;
import org.springframework.cloud.netflix.zuul.util.ZuulRuntimeException;
import org.springframework.http.HttpStatus;
import org.springframework.web.util.UrlPathHelper;
import org.uengine.iam.client.model.OauthUser;
import org.uengine.iam.util.JsonUtils;
import org.uengine.iam.util.JwtUtils;
import org.uengine.zuul.billing.BillingContext;
import org.uengine.zuul.billing.UserSubscriptions;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by uengine on 2017. 11. 14..
 */
@Data
public class TokenContext {
    static ThreadLocal<TokenContext> local = new ThreadLocal();
    String userName;
    OauthUser user;
    String clientKey;
    String token;
    List<String> scopes;
    Date expirationTime;
    Map route;
    UserSubscriptions userSubscriptions;
    BillingContext billingContext;

    public TokenContext(String token) {
        this.token = token;
        this.billingContext = new BillingContext();
        if (token != null) {
            try {
                JWSObject jwsObject = JWSObject.parse(token);

                //파싱 부분
                JSONObject jsonPayload = jwsObject.getPayload().toJSONObject();
                JWTClaimsSet jwtClaimsSet = JWTClaimsSet.parse(jsonPayload);
                JSONObject contexts = (JSONObject) jwtClaimsSet.getClaim("context");

                //클라이언트 스코프
                this.scopes = (List<String>) contexts.get("scopes");

                this.clientKey = (String) contexts.get("clientKey");

                //만료 시간
                this.expirationTime = jwtClaimsSet.getExpirationTime();

                this.user = JsonUtils.convertValue(contexts.get("user"), OauthUser.class);

                this.userName = contexts.get("userName").toString();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        local.set(this);
    }

    public static TokenContext getThreadLocalInstance() {
        TokenContext tc = (TokenContext) local.get();
        return tc != null ? tc : new TokenContext(null);
    }
}
