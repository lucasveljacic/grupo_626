package org.activityrecognition.measure;

public class Measure {
    private float[] values = new float[3];
    private int size;

    void setValues(float[] v) {
        values[0] += v[0];
        values[1] += v[1];
        values[2] += v[2];
        size++;
    }

    public float[] getValues() {
        return values;
    }

    public String collectAsString() {
        values[0] /= size;
        values[1] /= size;
        values[2] /= size;
        size = 1;
        return values[0] + "," + values[1] + "," + values[2];
    }
}
