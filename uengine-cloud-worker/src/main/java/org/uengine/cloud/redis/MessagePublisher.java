package org.uengine.cloud.redis;

public interface MessagePublisher {

    void publish(final String message);
}
