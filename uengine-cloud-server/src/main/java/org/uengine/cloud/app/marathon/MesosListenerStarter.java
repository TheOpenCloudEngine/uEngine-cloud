package org.uengine.cloud.app.marathon;

import com.launchdarkly.eventsource.EventSource;
import okhttp3.Headers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.concurrent.TimeUnit;

@Component
public class MesosListenerStarter {

    @Autowired
    private Environment environment;

    @Autowired
    private MesosChangeHandler mesosChangeHandler;

    @Async
    public void run() throws Exception {
        String url = environment.getProperty("dcos.host") + "/service/marathon/v2/events";
        EventSource.Builder builder = new EventSource.Builder(mesosChangeHandler, URI.create(url));
        Headers.Builder headerBuilder = new Headers.Builder();
        headerBuilder.add("Authorization", "token=" + environment.getProperty("dcos.token"));

        builder.headers(headerBuilder.build());
        try (EventSource eventSource = builder.build()) {
            eventSource.setReconnectionTimeMs(3000);
            eventSource.start();

            while (true) {
                TimeUnit.MINUTES.sleep(10);
            }
        } catch (Exception ex) {
            throw ex;
        }
    }
}
