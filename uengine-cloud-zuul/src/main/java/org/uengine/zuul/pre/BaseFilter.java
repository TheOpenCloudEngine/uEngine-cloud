package org.uengine.zuul.pre;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.netflix.zuul.filters.Route;
import org.springframework.cloud.netflix.zuul.filters.RouteLocator;
import org.springframework.web.util.UrlPathHelper;
import org.uengine.iam.client.model.OauthUser;
import org.uengine.iam.util.ApplicationContextRegistry;
import org.uengine.zuul.RuleService;
import org.uengine.zuul.billing.BillingConfig;
import org.uengine.zuul.billing.BillingRedisRepository;
import org.uengine.zuul.billing.BillingService;
import org.uengine.zuul.billing.UserSubscriptions;
import org.uengine.zuul.tenant.TokenContext;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RequiredArgsConstructor
public class BaseFilter extends ZuulFilter {

    private static Logger log = LoggerFactory.getLogger(BaseFilter.class);

    private final RouteLocator routeLocator;
    private final UrlPathHelper urlPathHelper = new UrlPathHelper();

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

    Route route(HttpServletRequest request) {
        String requestURI = urlPathHelper.getPathWithinApplication(request);
        return routeLocator.getMatchingRoute(requestURI);
    }

    @Override
    public Object run() {

        RequestContext ctx = RequestContext.getCurrentContext();
        HttpServletRequest request = ctx.getRequest();
        Route route = route(request);
        RuleService ruleService = ApplicationContextRegistry.getApplicationContext().getBean(RuleService.class);
        BillingService billingService = ApplicationContextRegistry.getApplicationContext().getBean(BillingService.class);
        BillingConfig billingConfig = ApplicationContextRegistry.getApplicationContext().getBean(BillingConfig.class);

        Map<String, Map> routes = ruleService.getRoutes();
        Map matchRoute = routes.get(route.getId());

        String token = request.getHeader("access_token");

        TokenContext tokenContext = new TokenContext(token);
        tokenContext.setRoute(matchRoute);


        String userName = tokenContext.getUserName();

        //사용자가 판별되고, 빌링이 사용될 경우
        if (userName != null && billingConfig.isEnable()) {
            UserSubscriptions userSubscriptions = billingService.getUserSubscriptionsByUserName(userName);
            tokenContext.setUserSubscriptions(userSubscriptions);
        }
        return null;
    }
}