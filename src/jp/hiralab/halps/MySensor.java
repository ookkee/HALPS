package jp.hiralab.halps;

import android.hardware.Sensor;
import android.widget.TextView;

public class MySensor {

    // Sensor related variables
    private Sensor sensor;
    private float[] min, max;
    public float[] currentValues, filteredValues;
    public float currentTime;

    // data displaying related variables
    private TextView tv;
    private String filename, dataUi, dataCsv, dataCsvFiltered;
    public String name;

    public MySensor() {
        this(null);
    }
    public MySensor(TextView t) {
        this(t,null);
    }

    public MySensor(TextView t, Sensor s) {
        tv = t;
        sensor = s;

        // initialize min & max values
        /////// TODO: NOT SURE IF ALL ARE A SIZE 3 ARRAY ///////
        min = new float[]{0,0,0};
        max = new float[]{0,0,0};
        currentValues = new float[]{0,0,0};
    }

    static float ALPHA = 0.25f; // ALPHA = 1 OR 0 -> no filtering
    public float[] lowPassFilter(float[] input, float[] output) {
        if(output == null) {
            System.out.println("Filter: output was null");
            return input;
        }
        System.out.println("Filter: NOT NULL");
        for(int i=0; i < 3; i++)
            output[i] = output[i] + ALPHA * (input[i] - output[i]);
        return output;
    }

    public void newValues(float time, float[] val, boolean recording) {

        currentValues = val;
        filteredValues = lowPassFilter(val, filteredValues);
        currentTime = time;

        //check min and max values
        setMin(val);
        setMax(val);

        //dataUi = sensor.getName() + "\n";
        dataUi = name + "\n";
        dataUi += "x : " + val[0] + "\n\tmin : " + min[0] + "max : " + max[0] + "\n";
        dataUi += "y : " + val[1] + "\n\tmin : " + min[1] + "max : " + max[1] + "\n";
        dataUi += "z : " + val[2] + "\n\tmin : " + min[2] + "max : " + max[2] + "\n";

        setText(dataUi);
        
        if(recording) {
            dataCsvFiltered += time + ";" + 
                filteredValues[0] + ";" + 
                filteredValues[1] + ";" +
                filteredValues[2] + "\n";
            dataCsv += time + ";" + 
                currentValues[0] + ";" + 
                currentValues[1] + ";" +
                currentValues[2] + "\n";
        }
    }

    public void display() {
        tv.setText(dataUi);
    }

    public Sensor getSensor() {
        return sensor;
    }

    /** Sets the filename string for this sensor */
    public void setFilename(String trunk, int delay) {
        //String sensorName = sensor.getName().replaceAll(" ","").toLowerCase();
        //sensorName = sensorName.replaceAll("sensor","");
        filename = trunk + "_delay" + delay + "_" + name.replaceAll(" ","") + ".csv";
    }

    public void setText(String s) {
        tv.setText(s);
    }

    public void append(String s) {
        tv.append(s);
    }

    public void resetCsv() {
        dataCsv = "";
        dataCsvFiltered = "";
    }

    public String getCsv(boolean filtered) {
        if(filtered)
            return dataCsvFiltered.replaceAll("null","");
        else
            return dataCsv.replaceAll("null","");
    }

    public String getFilename() {
        return filename;
    }

    public void setMin(float[] f) {
        for(int i=0; i<3; i++)
            if(min[i] > f[i])
                min[i] = f[i];
    }

    public float[] getMin() {
        return min;
    }

    public void setMax(float[] f) {
        for(int i=0; i<3; i++)
            if(max[i] < f[i])
                max[i] = f[i];
    }

    public float[] getMax() {
        return max;
    }
}
