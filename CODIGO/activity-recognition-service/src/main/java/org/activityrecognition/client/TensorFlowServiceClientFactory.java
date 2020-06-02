package org.activityrecognition.client;

import feign.Feign;
import feign.httpclient.ApacheHttpClient;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;

public class TensorFlowServiceClientFactory {

    public static TensorFlowServiceClient getClient() {
        Feign.Builder builder = Feign.builder()
                .client(new ApacheHttpClient())
                .encoder(new JacksonEncoder())
                .decoder(new JacksonDecoder())
                .errorDecoder(new TFErrorDecoder())
                .requestInterceptor(new OutboundRequestInterceptor());

        return builder.target(TensorFlowServiceClient.class, "http://localhost:9000");
    }
}
