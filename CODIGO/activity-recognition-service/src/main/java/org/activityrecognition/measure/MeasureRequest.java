package org.activityrecognition.measure;

import java.io.Serializable;
import java.util.List;

public class MeasureRequest implements Serializable {
    List<String> packet;

    public MeasureRequest() {
    }

    public void setPacket(List<String> packet) {
        this.packet = packet;
    }
    public List<String> getPacket() {
        return packet;
    }
}
