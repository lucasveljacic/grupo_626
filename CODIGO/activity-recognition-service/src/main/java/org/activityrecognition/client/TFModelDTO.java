package org.activityrecognition.client;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class TFModelDTO {
    private List<TFModelVersionStatusDTO> modelVersionStatus;

    @JsonCreator
    public TFModelDTO(@JsonProperty("model_version_status") List<TFModelVersionStatusDTO> modelVersionStatus) {
        this.modelVersionStatus = modelVersionStatus;
    }

    public List<TFModelVersionStatusDTO> getModelVersionStatus() {
        return modelVersionStatus;
    }

    public void setModelVersionStatus(List<TFModelVersionStatusDTO> modelVersionStatus) {
        this.modelVersionStatus = modelVersionStatus;
    }
}
