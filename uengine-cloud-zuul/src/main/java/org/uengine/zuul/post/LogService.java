package org.uengine.zuul.post;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.uengine.zuul.kafka.MeteringProcessor;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Service
public class LogService {

    private ArrayList<MeteringLog> meteringLogs;
    private ArrayList<UsageLog> usageLogs;

    @Autowired
    MeteringProcessor gatewayProcessor;

    @PostConstruct
    private void init() {
        meteringLogs = new ArrayList<>();
        usageLogs = new ArrayList<>();
    }

    public void addMeteringLog(MeteringLog log) {
        meteringLogs.add(log);
    }

    public void addUsageLog(UsageLog log) {
        usageLogs.add(log);
    }

    @Scheduled(initialDelay = 1000, fixedDelay = 5000)
    public void logScheduler() {
        try {
            Map metering = new HashMap();
            metering.put("eventType", "metering");
            metering.put("logs", meteringLogs);
            String meteringMessage = new ObjectMapper().writeValueAsString(metering);
            gatewayProcessor.sendMeteringMessage(meteringMessage);
            meteringLogs.clear();

            Map usage = new HashMap();
            usage.put("eventType", "usage");
            usage.put("logs", usageLogs);
            String usageMessage = new ObjectMapper().writeValueAsString(usage);
            gatewayProcessor.sendMeteringMessage(usageMessage);
            usageLogs.clear();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
