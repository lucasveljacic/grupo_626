package org.activityrecognition.client.exception;

public class ModelNotFoundException extends Exception {
    public ModelNotFoundException() {
        super("Model not found");
    }
}
