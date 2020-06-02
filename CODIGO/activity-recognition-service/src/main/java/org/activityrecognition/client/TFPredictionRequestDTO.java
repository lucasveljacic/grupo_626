package org.activityrecognition.client;

public class TFPredictionRequestDTO {

    private final String signatureName = "signature_name";
    private float[][][] instances;

    public TFPredictionRequestDTO(float[][][] instances) {
        this.instances = instances;
    }

    public String getSignatureName() {
        return signatureName;
    }

    public float[][][] getInstances() {
        return instances;
    }

    public void setInstances(float[][][] instances) {
        this.instances = instances;
    }
}
