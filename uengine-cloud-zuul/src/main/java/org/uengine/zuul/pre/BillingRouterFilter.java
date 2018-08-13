package org.uengine.zuul.pre;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.netflix.zuul.util.ZuulRuntimeException;
import org.springframework.http.HttpStatus;
import org.uengine.iam.util.ApplicationContextRegistry;
import org.uengine.zuul.billing.BillingConfig;
import org.uengine.zuul.billing.BillingContext;
import org.uengine.zuul.billing.UserSubscriptions;
import org.uengine.zuul.tenant.TokenContext;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

public class BillingRouterFilter extends ZuulFilter {

    private static Logger log = LoggerFactory.getLogger(BillingRouterFilter.class);

    @Override
    public String filterType() {
        return "pre";
    }

    @Override
    public int filterOrder() {
        return 4;
    }

    @Override
    public boolean shouldFilter() {
        return true;
    }

    @Override
    public Object run() {
        RequestContext ctx = RequestContext.getCurrentContext();
        HttpServletRequest request = ctx.getRequest();

        BillingConfig billingConfig = ApplicationContextRegistry.getApplicationContext().getBean(BillingConfig.class);
        if (!billingConfig.isEnable()) {
            return null;
        }
        TokenContext tokenContext = TokenContext.getThreadLocalInstance();
        Map route = tokenContext.getRoute();
        if (!route.containsKey("billing-plans")) {
            return null;
        }
        try {
            Map<String, String> billingPlans = (Map<String, String>) route.get("billing-plans");
            if (billingPlans == null) {
                return null;
            }
            boolean passable = false;
            boolean hasPlan = false;


            for (String billingPlan : billingPlans.values()) {
                String[] planAndMethod = billingPlan.split("/");
                String plan = planAndMethod[0].trim();

                //필요한 메소드 목록
                String methods = planAndMethod.length > 1 ? planAndMethod[1] : "";
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

                boolean planMatch = false;
                //모든 플랜 통과
                if ("guest".equals(plan)) {
                    planMatch = true;
                }
                //플랜 체크 필요
                else {
                    UserSubscriptions subscriptions = tokenContext.getUserSubscriptions();
                    List<UserSubscriptions.Subscription> subscriptionList = subscriptions.getSubscriptions();
                    if (subscriptionList != null) {
                        for (int i = 0; i < subscriptionList.size(); i++) {
                            planMatch = plan.equals(subscriptionList.get(i).getPlan());
                            if (planMatch) {
                                //save routeLog for logging
                                BillingContext.RouteLog routeLog = new BillingContext.RouteLog();
                                routeLog.setPlan(plan);
                                routeLog.setSubscriptionId(subscriptionList.get(i).getId());
                                routeLog.setProduct(subscriptionList.get(i).getProduct());

                                tokenContext.getBillingContext().setRouteLog(routeLog);
                            }
                            break;
                        }
                    }
                }

                if (planMatch && !"guest".equals(plan)) {
                    hasPlan = true;
                }
                if (methodMatch && planMatch) {

                    passable = true;
                }
            }

            // if passable, end filter.
            if (passable) {
                return null;
            }

            // throwing Error
            if (hasPlan) {
                throw new ZuulRuntimeException(new ZuulException(
                        "Method is not allowed", HttpStatus.METHOD_NOT_ALLOWED.value(), "User has plan, but method " + request.getMethod() + " is not allowed."));

            } else {
                throw new ZuulRuntimeException(new ZuulException(
                        "Not has plan", HttpStatus.UNAUTHORIZED.value(), "User doesn't have required plan."));
            }
        } catch (Exception ex) {
            ctx.unset();
            ctx.setResponseStatusCode(HttpStatus.UNAUTHORIZED.value());
        }
        return null;
    }
}