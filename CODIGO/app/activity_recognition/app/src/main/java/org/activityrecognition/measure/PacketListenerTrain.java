package org.activityrecognition.measure;

import java.util.List;

public interface PacketListenerTrain {
    void onPackageComplete(List<String> packet);
}
