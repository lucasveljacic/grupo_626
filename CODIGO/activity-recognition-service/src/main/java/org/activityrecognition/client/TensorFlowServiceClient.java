package org.activityrecognition.client;

import feign.Headers;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

public interface TensorFlowServiceClient {

    @PostMapping("/v1/models/{model_name}/versions/1:predict")
    @Headers("Content-Type: application/json")
    TFPredictionResponseDTO predict(@PathVariable(value = "model_name") String modelName, TFPredictionRequestDTO predictionRequestDTO);
}
