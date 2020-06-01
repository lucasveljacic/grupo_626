package org.activityrecognition.mlmodel.api;

public class PredictionOutputDTO {
    private float prediction;
    private String message;

    public PredictionOutputDTO(float prediction, String message) {
        this.prediction = prediction;
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public float getPrediction() {
        return prediction;
    }

    public void setPrediction(float prediction) {
        this.prediction = prediction;
    }
}
