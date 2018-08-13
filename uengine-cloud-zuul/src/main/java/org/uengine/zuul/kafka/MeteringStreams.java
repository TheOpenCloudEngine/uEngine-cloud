package org.uengine.zuul.kafka;

import org.springframework.cloud.stream.annotation.Input;
import org.springframework.cloud.stream.annotation.Output;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.SubscribableChannel;

public interface MeteringStreams {
    String INPUT = "billing-sub";
    String OUTPUT = "metering-pub";

    @Input("billing-sub")
    SubscribableChannel billingSub();

    @Output("metering-pub")
    MessageChannel meteringPub();
}
