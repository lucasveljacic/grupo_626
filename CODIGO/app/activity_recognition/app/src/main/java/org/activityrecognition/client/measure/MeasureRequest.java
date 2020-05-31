package org.activityrecognition.client.measure;

import java.io.Serializable;
import java.util.List;

public class MeasureRequest implements Serializable {
    List<String> packet;

    public MeasureRequest(List<String> packet) {
        this.packet = packet;
    }

    public void setPacket(List<String> packet) {
        this.packet = packet;
    }

    public List<String> getPacket() {
        return packet;
    }
}
