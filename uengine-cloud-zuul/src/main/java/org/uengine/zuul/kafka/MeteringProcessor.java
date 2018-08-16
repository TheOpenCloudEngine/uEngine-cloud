package org.uengine.zuul.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.Input;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeTypeUtils;
import org.uengine.zuul.billing.BillingConfig;
import org.uengine.zuul.billing.BillingService;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.util.Arrays;
import java.util.Map;

@Service
public class MeteringProcessor {

    private final Log logger = LogFactory.getLog(getClass());

    @Autowired
    private BillingService billingService;

    @Autowired
    private BillingConfig billingConfig;

    private MeteringStreams meteringStreams;

    public MeteringProcessor(MeteringStreams meteringStreams) {
        this.meteringStreams = meteringStreams;
    }


    public void sendMeteringMessage(final String message) {
        try {
            logger.info("Sending metering messages");

            MessageChannel messageChannel = meteringStreams.meteringPub();
            messageChannel.send(MessageBuilder
                    .withPayload(message)
                    .setHeader(MessageHeaders.CONTENT_TYPE, MimeTypeUtils.APPLICATION_JSON)
                    .build());
        } catch (Exception ex) {

        }
    }

    @StreamListener
    public void receiveBillingMessage(@Input(MeteringStreams.INPUT) Flux<String> inbound) {
        if(billingConfig.isEnable()){
            inbound
                    .log()
                    .subscribeOn(Schedulers.elastic())
                    .subscribe(value -> {
                        try {
                            logger.info("receive BillingMessage : " + value);
                            Map map = new ObjectMapper().readValue(value, Map.class);
                            String eventType = map.get("eventType").toString();
                            String accountId = map.get("accountId").toString();

                            String[] acceptTypes = new String[]{
                                    "ACCOUNT_CREATION",
                                    "ACCOUNT_CHANGE",
                                    "SUBSCRIPTION_CREATION",
                                    "SUBSCRIPTION_PHASE",
                                    "SUBSCRIPTION_CHANGE",
                                    "SUBSCRIPTION_CANCEL",
                                    "SUBSCRIPTION_UNCANCEL",
                                    "SUBSCRIPTION_BCD_CHANGE"
                            };

                            if (Arrays.asList(acceptTypes).contains(eventType)) {
                                billingService.updateUserSubscriptionsByAccountId(accountId);
                            }
                        } catch (Exception ex) {
                            logger.error("update UserSubscriptions failed");
                        }
                    }, error -> System.err.println("CAUGHT " + error));
        }
    }
}


