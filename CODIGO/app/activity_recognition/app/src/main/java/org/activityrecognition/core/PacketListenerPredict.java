package org.activityrecognition.core;

public interface PacketListenerPredict {
    void onPackageComplete(float[][][] inputPrediction);
}
