package org.activityrecognition.client;

import feign.Feign;
import feign.Logger;
import feign.gson.GsonDecoder;
import feign.gson.GsonEncoder;
import feign.okhttp.OkHttpClient;
import feign.slf4j.Slf4jLogger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TensorFlowServiceClientFactory {

    @Bean
    public TensorFlowServiceClient getTensorflowServiceClient() {
        return Feign.builder()
                .client(new OkHttpClient())
                .encoder(new GsonEncoder())
                .decoder(new GsonDecoder())
                .logger(new Slf4jLogger(TensorFlowServiceClient.class))
                .logLevel(Logger.Level.FULL)
                .target(TensorFlowServiceClient.class, "http://localhost:9000");
    }
}
