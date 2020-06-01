package org.activityrecognition.mlmodel.api;

public class PredictionInputDTO {
    private float[][][] input;

    public float[][][] getInput() {
        return input;
    }

    public void setInput(float[][][] input) {
        this.input = input;
    }
}
