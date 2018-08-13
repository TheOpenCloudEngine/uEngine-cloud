package org.uengine.zuul.post;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.uengine.iam.client.model.OauthUser;
import org.uengine.iam.util.ApplicationContextRegistry;
import org.uengine.zuul.billing.BillingConfig;
import org.uengine.zuul.billing.BillingContext;
import org.uengine.zuul.billing.UserSubscriptions;
import org.uengine.zuul.tenant.TokenContext;

import javax.servlet.http.HttpServletRequest;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class LogFilter extends ZuulFilter {

    private static Logger log = LoggerFactory.getLogger(LogFilter.class);

    @Override
    public String filterType() {
        return "post";
    }

    @Override
    public int filterOrder() {
        return 2000;
    }

    @Override
    public boolean shouldFilter() {
        return true;
    }

    @Override
    public Object run() {
        //add Metering log
        RequestContext ctx = RequestContext.getCurrentContext();
        HttpServletRequest request = ctx.getRequest();

        TokenContext tokenContext = TokenContext.getThreadLocalInstance();
        MeteringLog meteringLog = new MeteringLog();

        BillingConfig billingConfig = ApplicationContextRegistry.getApplicationContext().getBean(BillingConfig.class);
        LogService logService = ApplicationContextRegistry.getApplicationContext().getBean(LogService.class);

        meteringLog.setMethod(request.getMethod());
        meteringLog.setRemoteAddr(request.getRemoteAddr());
        meteringLog.setRequestURI(request.getRequestURI());

        try {
            meteringLog.setServiceId(tokenContext.getRoute().get("serviceId").toString());
        } catch (Exception ex) {

        }
        meteringLog.setResponseStatus(ctx.getResponseStatusCode());
        List<UserSubscriptions.Subscription> subscriptions = tokenContext.getUserSubscriptions().getSubscriptions();

        List<MeteringLog.Subscription> list = new ArrayList<>();
        if(subscriptions != null){
            for (int i = 0; i < subscriptions.size(); i++) {
                MeteringLog.Subscription subscription = new MeteringLog.Subscription();
                subscription.setId(subscriptions.get(i).getId());
                subscription.setPlan(subscriptions.get(i).getPlan());
                subscription.setProduct(subscriptions.get(i).getProduct());
                list.add(subscription);
            }
        }
        meteringLog.setSubscriptions(list);
        meteringLog.setClientKey(tokenContext.getClientKey());
        meteringLog.setUserName(tokenContext.getUserName());
        meteringLog.setAccountId(tokenContext.getUserSubscriptions().getAccountId());
        meteringLog.setTimestamp(new Timestamp(new Date().getTime()));
        meteringLog.setOrganizationId(billingConfig.getOrganization());
        logService.addMeteringLog(meteringLog);

        //Add usageLog
        BillingContext.UsageLog usageLog = tokenContext.getBillingContext().getUsageLog();
        if (usageLog != null) {
            int statusCode = ctx.getResponseStatusCode();
            Integer[] when = usageLog.getWhen();
            //save if status code in when options
            if (Arrays.asList(when).contains(statusCode)) {
                UsageLog log = new UsageLog();
                log.setUserName(tokenContext.getUserName());
                log.setClientKey(tokenContext.getClientKey());
                log.setPlan(usageLog.getPlan());
                log.setSubscriptionId(usageLog.getSubscriptionId());
                log.setAccountId(tokenContext.getUserSubscriptions().getAccountId());
                log.setUnit(usageLog.getUnit());
                log.setTimestamp(new Timestamp(new Date().getTime()));
                log.setProduct(usageLog.getProduct());
                log.setOrganizationId(billingConfig.getOrganization());
                logService.addUsageLog(log);
            }
        }

        return null;
    }
}