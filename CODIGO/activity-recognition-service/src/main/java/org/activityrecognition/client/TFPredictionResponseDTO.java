package org.activityrecognition.client;

public class TFPredictionResponseDTO {

    private float[][] predictions;

    public float[][] getPredictions() {
        return predictions;
    }

    public void setPredictions(float[][] predictions) {
        this.predictions = predictions;
    }
}
