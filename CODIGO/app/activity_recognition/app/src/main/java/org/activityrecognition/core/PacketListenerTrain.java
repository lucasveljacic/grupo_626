package org.activityrecognition.core;

import java.util.List;

public interface PacketListenerTrain {
    void onPackageComplete(List<String> packet);
}
