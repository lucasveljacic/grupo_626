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
        measures.get(sensorType).setValues(values);
        sensorsCollectedSet.add(sensorType);
    }

    boolean ready() {
        if (!ready) {
            ready = sensorsCollectedSet.size() == sensorTypes.size();
        }
        return ready;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public Map<Integer, Measure> getMeasures() {
        return measures;
    }

    @Override
    public String toString() {
        return timestamp + "," + measures.entrySet().stream()
                .map(entry -> entry.getKey()+","+entry.getValue().collectAsString())
                .collect(Collectors.joining(","));
    }

}

