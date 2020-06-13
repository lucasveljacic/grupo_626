package org.activityrecognition.ui.collect;

import java.util.List;

public interface PacketListenerForCollect {
    void onPackageComplete(List<String> packet);
}
