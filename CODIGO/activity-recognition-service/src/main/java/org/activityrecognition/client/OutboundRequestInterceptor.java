package org.activityrecognition.client;
import feign.RequestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OutboundRequestInterceptor implements feign.RequestInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(OutboundRequestInterceptor.class);

    @Override
    public void apply(RequestTemplate requestTemplate) {
        logger.info(String.format("%s - %s", requestTemplate.method(), requestTemplate.url()));
    }
}
