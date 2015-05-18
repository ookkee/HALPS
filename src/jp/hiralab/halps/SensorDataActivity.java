package jp.hiralab.halps;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.List;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.TextView;

public class SensorDataActivity extends Activity implements SensorEventListener
{
    private SensorManager mSensorManager;
    private Sensor mAccelerometer, mGravity, mOrientation;
    TextView vwAccData, vwGraData, vwOriData;
    File fAcc, fGra, fOri;
    String strAccFile;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sensordata);

        // Initialize views
        TextView vwSensorInfo = (TextView) findViewById(R.id.sensorinfo);
        vwAccData = (TextView) findViewById(R.id.accdata);
        vwGraData = (TextView) findViewById(R.id.gradata);
        vwOriData = (TextView) findViewById(R.id.oridata);

        // Query sensor services
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        List<Sensor> sensorList = mSensorManager.getSensorList(Sensor.TYPE_ALL);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        mGravity = mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        mOrientation = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);

        // Print list of sensors
        String strSensorList = new String("");
        Sensor tmp;

        for(int i = 0; i < sensorList.size(); i++) {
            tmp = sensorList.get(i);
            strSensorList += tmp.getName() + "\n";
        }
        if(sensorList.size() > 0) {
            vwSensorInfo.setText(strSensorList);
        }
        else {
            vwSensorInfo.setText("No sensors found!");
        }

        /*
        if (mAccelerometer != null) {
            // Found accelerometer!
            vwSensorInfo.append("Found accelerometer!"); 
        }
        else {
            // No accelerometer!
            vwSensorInfo.append("Could not find accelerometer!");
        }
        */
        // Register sensor listeners
        mSensorManager.registerListener(this, mGravity, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mOrientation, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);

        // Get files for writing data
        if(isExternalStorageWritable()) {
            fAcc = new File(getExternalFilesDir(null), "acc_test.csv");
            vwSensorInfo.append("\next storg is writable!\n");
        }
    }

    /** Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        Sensor sensor = event.sensor;
        if (sensor.getType() == Sensor.TYPE_GRAVITY) {
            String strGraData = new String("");
            strGraData += "GRAVITY\n";
            strGraData += "x : " + event.values[0] + "\n";
            strGraData += "y : " + event.values[1] + "\n";
            strGraData += "z : " + event.values[2] + "\n";
            strGraData += event.timestamp + ";" +
                event.values[0] + ";" +
                event.values[1] + ";" +
                event.values[2] + "\n";
            vwGraData.setText(strGraData);
        }
        if(sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
            String strAccData = new String("");
            /*
            strAccData += "LINEAR ACCELERATION\n";
            strAccData += "x : " + event.values[0] + "\n";
            strAccData += "y : " + event.values[1] + "\n";
            strAccData += "z : " + event.values[2] + "\n";
            */
            strAccData = event.timestamp + ";" +
                event.values[0] + ";" +
                event.values[1] + ";" +
                event.values[2] + "\n";
            vwAccData.setText(strAccData);
            strAccFile += strAccData;
        }
        if (sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
            String strOriData = new String("");
            strOriData += "ORIENTATION\n";
            strOriData += "x : " + event.values[0] + "\n";
            strOriData += "y : " + event.values[1] + "\n";
            strOriData += "z : " + event.values[2] + "\n";
            vwOriData.setText(strOriData);
        }
        /*
        else {
            strAccData += "did not get sensor types!";
        }
        */

    }

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mGravity, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mOrientation, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    /** Called when user clicks write data button, writes sensor data to file */
    public void writeData(View view) {

        try {
            FileOutputStream f = new FileOutputStream(fAcc);
            PrintWriter pw = new PrintWriter(f);
            pw.print(strAccFile);
            pw.flush();
            pw.close();
            f.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
