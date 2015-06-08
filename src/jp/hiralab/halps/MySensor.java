package jp.hiralab.halps;

import android.hardware.Sensor;
import android.widget.TextView;

public class MySensor {

    // Sensor related variables
    private Sensor sensor;
    private float[] min, max;

    // data displaying related variables
    private TextView tv;
    private String filename, dataUi, dataCsv;

    public MySensor(Sensor s, TextView t) {
        sensor = s;
        tv = t;

        // initialize min & max values
        /////// TODO: NOT SURE IF ALL ARE A SIZE 3 ARRAY ///////
        min = new float[]{0,0,0};
        max = new float[]{0,0,0};
    }

    public void newValues(float time, float[] val, boolean recording) {

        //check min and max values
        setMin(val);
        setMax(val);

        dataUi = sensor.getName() + "\n";
        dataUi += "x : " + val[0] + "\tmin : " + min[0] + "\tmax : " + max[0] + "\n";
        dataUi += "y : " + val[1] + "\tmin : " + min[1] + "\tmax : " + max[1] + "\n";
        dataUi += "z : " + val[2] + "\tmin : " + min[2] + "\tmax : " + max[2] + "\n";

        setText(dataUi);
        
        if(recording) {
            dataCsv += time + ";" + 
                val[0] + ";" + 
                val[1] + ";" +
                val[2] + "\n";
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
        String sensorName = sensor.getName().replaceAll(" ","").toLowerCase();
        sensorName = sensorName.replaceAll("sensor","");
        filename = trunk + "_delay" + delay + "_" + sensorName + ".csv";
    }

    public void setText(String s) {
        tv.setText(s);
    }

    public void append(String s) {
        tv.append(s);
    }

    public void resetCsv() {
        dataCsv = "";
    }

    public String getCsv() {
        return dataCsv;
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
