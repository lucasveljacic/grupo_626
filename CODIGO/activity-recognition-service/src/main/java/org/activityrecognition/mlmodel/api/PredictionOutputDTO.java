package org.activityrecognition.mlmodel.api;

public class PredictionOutputDTO {
    private float prediction;

    public PredictionOutputDTO(float prediction) {
        this.prediction = prediction;
    }

    public float getPrediction() {
        return prediction;
    }

    public void setPrediction(float prediction) {
        this.prediction = prediction;
    }
}
