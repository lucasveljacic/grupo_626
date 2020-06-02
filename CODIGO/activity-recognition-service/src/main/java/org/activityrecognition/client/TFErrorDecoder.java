package org.activityrecognition.client;

import feign.Response;
import feign.codec.ErrorDecoder;
import org.activityrecognition.client.exception.ModelNotFoundException;

import static feign.FeignException.errorStatus;

public class TFErrorDecoder implements ErrorDecoder {
    @Override
    public Exception decode(String methodKey, Response response) {
        if (response.status() >= 400 && response.status() <= 499) {
            return new ModelNotFoundException();
        }

        return errorStatus(methodKey, response);
    }
}
