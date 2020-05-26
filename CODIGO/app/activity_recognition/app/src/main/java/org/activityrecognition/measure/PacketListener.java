package org.activityrecognition.measure;

import java.util.List;

public interface PacketListener {
    void onPackageComplete(List<String> packet);
}
