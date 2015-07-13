package jp.hiralab.halps;

import android.hardware.Sensor;

public class SensorData {

    private static final float FILTER = 0.25f; // 0 < filter amount < 1

    private Sensor sensor;
    private float[] currentValues, filteredValues;
    private float valueTime;

    public SensorData(Sensor s) {
        sensor = s;

        currentValues = new float[]{0,0,0};
        filteredValues = new float[]{0,0,0};
        valueTime = 0f;
    }

    public void newValues(float time, float[] val) {
        currentValues = val;
        valueTime = time;
        filteredValues = lowPassFilter(val, filteredValues);
    }

    public float[] lowPassFilter(float[] input, float[] output) {
        if(output == null)
            return input;

        for(int i=0; i<3; i++)
            output[i] = output[i] + FILTER*(input[i] - output[i]);
        return output;
    }

    public Sensor getSensor() {
        return sensor;
    }

    public float[] getCurrentValues() {
        return currentValues;
    }

    public float[] getFilteredValues() {
        return filteredValues;
    }

    public float getValueTime() {
        return valueTime;
    }

}
