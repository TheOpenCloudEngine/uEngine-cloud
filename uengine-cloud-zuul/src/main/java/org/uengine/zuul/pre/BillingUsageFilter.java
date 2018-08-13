package org.uengine.zuul.pre;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.netflix.zuul.util.ZuulRuntimeException;
import org.springframework.http.HttpStatus;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;
import org.springframework.web.util.UrlPathHelper;
import org.uengine.iam.util.ApplicationContextRegistry;
import org.uengine.zuul.RuleService;
import org.uengine.zuul.billing.BillingConfig;
import org.uengine.zuul.billing.BillingContext;
import org.uengine.zuul.billing.UserSubscriptions;
import org.uengine.zuul.tenant.TokenContext;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class BillingUsageFilter extends ZuulFilter {

    private static Logger log = LoggerFactory.getLogger(BillingUsageFilter.class);

    @Override
    public String filterType() {
        return "pre";
    }

    @Override
    public int filterOrder() {
        return 5;
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
        RuleService ruleService = ApplicationContextRegistry.getApplicationContext().getBean(RuleService.class);
        if (!billingConfig.isEnable()) {
            return null;
        }

        //usage 는 사용자 구독 리스트가 있을 경우에만 추가.
        TokenContext tokenContext = TokenContext.getThreadLocalInstance();
        UserSubscriptions subscriptions = tokenContext.getUserSubscriptions();
        List<UserSubscriptions.Subscription> subscriptionList = subscriptions.getSubscriptions();
        if (subscriptionList == null || subscriptionList.size() < 1) {
            return null;
        }

        String requestURI = new UrlPathHelper().getPathWithinApplication(request);
        RuleService.Usage[] usages = ruleService.getUsages();
        for (int i = 0; i < usages.length; i++) {
            RuleService.Usage usage = usages[i];

            AntPathMatcher pathMatcher = new AntPathMatcher();
            boolean match = pathMatcher.match(usage.getPath(), requestURI);
            if (match) {
                List<String> billingPlans = Arrays.asList(usage.getBillingPlans());
                for (int t = 0; t < billingPlans.size(); t++) {
                    String billingPlan = billingPlans.get(t);

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
                    if (methodMatch) {
                        for (int s = 0; s < subscriptionList.size(); s++) {
                            UserSubscriptions.Subscription subscription = subscriptionList.get(s);

                            //Match!!
                            if (plan.equals(subscription.getPlan())) {
                                BillingContext.UsageLog usageLog = new BillingContext.UsageLog();
                                usageLog.setPlan(plan);
                                usageLog.setSubscriptionId(subscription.getId());
                                usageLog.setUnit(usage.getUnit());
                                usageLog.setWhen(usage.getWhen());
                                usageLog.setProduct(subscription.getProduct());
                                tokenContext.getBillingContext().setUsageLog(usageLog);
                                break;
                            }
                        }
                    }
                }
            }
        }
        return null;
    }
}