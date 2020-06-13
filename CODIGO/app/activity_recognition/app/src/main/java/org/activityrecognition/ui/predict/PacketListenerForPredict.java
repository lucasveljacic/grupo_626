package org.activityrecognition.ui.predict;

public interface PacketListenerForPredict {
    void onPackageComplete(float[][][] inputPrediction);
}
