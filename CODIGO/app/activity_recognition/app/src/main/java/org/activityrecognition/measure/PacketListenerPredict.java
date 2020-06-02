package org.activityrecognition.measure;

public interface PacketListenerPredict {
    void onPackageComplete(float[][][] inputPrediction);
}
