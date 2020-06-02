package org.activityrecognition.measure;

import android.annotation.SuppressLint;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import java.util.ArrayList;
import java.util.List;

public class SensorPacketCollector implements SensorEventListener {
    private final int PACKET_SIZE = 20;
    private final int SAMPLE_RATE_MILLIS = 20;
    private SensorManager sensorManager;
    private PacketListener listener;
    private List<String> packet;
    private MeasureGroup measureGroup;
    private long lastReportTime;

    SensorPacketCollector(SensorManager sensorManager) {
        this.sensorManager = sensorManager;
        this.packet = new ArrayList<>();
        this.measureGroup = new MeasureGroup();

        lastReportTime = System.currentTimeMillis();
    }

    void start() {
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
                packet.add(measureGroup.toString());
                lastReportTime = now;
            }

            // check if we should send the packet
            if (packet.size() == PACKET_SIZE) {
                notifyListeners(packet);
                packet = new ArrayList<>();
            }
        }
    }

    private void notifyListeners(List<String> packet) {
        if (this.listener != null) {
            this.listener.onPackageComplete(packet);
        }
    }

    void registerListener(PacketListener listener) {
        this.listener = listener;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    void stop() {
        sensorManager.unregisterListener(this);
    }
}
