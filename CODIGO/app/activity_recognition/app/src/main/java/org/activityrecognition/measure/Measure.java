package org.activityrecognition.measure;

public class Measure {
    private float[] values = new float[3];
    private int size = 0;
    private boolean isClosed = true;

    void addValues(float[] v) {
        if (isClosed) {
            values = v;
            isClosed = false;
        } else {
            values[0] += v[0];
            values[1] += v[1];
            values[2] += v[2];
        }
        size++;
    }

    String collectAsString() {
        if (!isClosed) {
            mean();
            isClosed = true;
        }
        return values[0] + "," + values[1] + "," + values[2];
    }

    float[] collect() {
        if (!isClosed) {
            mean();
            isClosed = true;
        }
        return values.clone();
    }

    private void mean() {
        values[0] /= size;
        values[1] /= size;
        values[2] /= size;
        size = 0;
    }
}
