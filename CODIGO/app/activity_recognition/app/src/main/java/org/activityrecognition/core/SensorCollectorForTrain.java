package org.activityrecognition.core;

import android.annotation.SuppressLint;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import java.util.ArrayList;
import java.util.List;

public class SensorCollectorForTrain implements SensorEventListener {
    private final int PACKET_SIZE = 50;
    private final int SAMPLE_RATE_MILLIS = 20;
    private SensorManager sensorManager;
    private PacketListenerTrain listener;
    private List<String> packet;
    private MeasureGroup measureGroup;
    private long lastReportTime;

    public SensorCollectorForTrain(SensorManager sensorManager) {
        this.sensorManager = sensorManager;
        this.packet = new ArrayList<>();
        this.measureGroup = new MeasureGroup();

        lastReportTime = System.currentTimeMillis();
    }

    public void start() {
        sensorManager.registerListener(this,
                sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this,
                sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE),
                SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this,
                sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR),
                SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this,
                sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY),
                SensorManager.SENSOR_DELAY_NORMAL);
    }

    @SuppressLint("DefaultLocale")
    @Override
    public void onSensorChanged(SensorEvent event) {
        synchronized (this) {
            switch(event.sensor.getType()) {
                case Sensor.TYPE_ACCELEROMETER:
                case Sensor.TYPE_GYROSCOPE:
                case Sensor.TYPE_GRAVITY:
                case Sensor.TYPE_ROTATION_VECTOR:
                    // collecting new measure
                    measureGroup.addMeasure(event.timestamp, event.sensor.getType(), event.values);
                    break;
                default:
                    return;
            }

            // check if we should close the measure after collecting and averaging
            long now = System.currentTimeMillis();
            if (measureGroup.ready() && now > lastReportTime + SAMPLE_RATE_MILLIS) {
                packet.add(measureGroup.toPacket());
                lastReportTime = now;
            }

            // check if we should send the packet
            if (packet.size() == PACKET_SIZE) {
                notifyListener(packet);
                packet = new ArrayList<>();
            }
        }
    }

    private void notifyListener(List<String> packet) {
        if (this.listener != null) {
            this.listener.onPackageComplete(packet);
        }
    }

    public void registerListener(PacketListenerTrain listener) {
        this.listener = listener;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public void stop() {
        sensorManager.unregisterListener(this);
    }

    public void unregisterListener() {
        sensorManager.unregisterListener(this);
        this.listener = null;
    }
}
