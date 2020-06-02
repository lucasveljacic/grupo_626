package org.activityrecognition.measure;

import android.hardware.Sensor;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class MeasureGroup {
    private boolean ready;
    private Long timestamp;
    private Map<Integer, Measure> measures;
    private Set<Integer> sensorsCollectedSet;

    private final List<Integer> sensorTypes = Arrays.asList(
            Sensor.TYPE_ACCELEROMETER,
            Sensor.TYPE_GYROSCOPE,
            Sensor.TYPE_GRAVITY,
            Sensor.TYPE_ROTATION_VECTOR);

    MeasureGroup() {
        measures = new HashMap<>();
        sensorTypes.forEach(type -> measures.put(type, new Measure()));
        sensorsCollectedSet = new HashSet<>();
    }

    void addMeasure(Long timestamp, int sensorType, float[] values) {
        this.timestamp = timestamp;
        measures.get(sensorType).addValues(values);
        sensorsCollectedSet.add(sensorType);
    }

    boolean ready() {
        if (!ready) {
            ready = sensorsCollectedSet.size() == sensorTypes.size();
        }
        return ready;
    }

    public String toPacket() {
        return timestamp + "," + measures.entrySet().stream()
                .map(entry -> entry.getKey()+","+entry.getValue().collectAsString())
                .collect(Collectors.joining(","));
    }

    public float[] toInputPrediction() {
        float[] v1 = measures.get(Sensor.TYPE_ACCELEROMETER).collect();
        float[] v2 = measures.get(Sensor.TYPE_GYROSCOPE).collect();
        float[] v3 = measures.get(Sensor.TYPE_GRAVITY).collect();
        float[] v4 = measures.get(Sensor.TYPE_ROTATION_VECTOR).collect();

        return new float[]{v1[0], v1[1], v1[2], v2[0], v2[1], v2[2], v3[0], v3[1], v3[2], v4[0], v4[1], v4[2]};
    }
}

