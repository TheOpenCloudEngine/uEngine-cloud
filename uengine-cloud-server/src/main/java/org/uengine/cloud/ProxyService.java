package org.uengine.cloud;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProxyService {

    /**
     * SLF4J Logging
     */
    private Logger logger = LoggerFactory.getLogger(ProxyService.class);

    public void doProxy(ProxyRequest proxyRequest) throws Exception {
        try {
            new ProxyAction(proxyRequest).service();
        } catch (Exception ex) {
            logger.error("error", ex);
            throw new Exception(ex);
        }
    }
}

