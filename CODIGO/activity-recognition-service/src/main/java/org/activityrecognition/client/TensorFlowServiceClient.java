package org.activityrecognition.client;

import feign.Headers;
import feign.Param;
import feign.RequestLine;

public interface TensorFlowServiceClient {

    @RequestLine("POST /v1/models/{model_name}/versions/1:predict")
    @Headers("Content-Type: application/json")
    TFPredictionResponseDTO predict(@Param(value = "model_name") String modelName,
                                    TFPredictionRequestDTO predictionRequestDTO);
}
