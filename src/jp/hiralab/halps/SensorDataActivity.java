package jp.hiralab.halps;

import java.util.List;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.TextView;

public class SensorDataActivity extends Activity implements SensorEventListener
{
    private SensorManager mSensorManager;
    private Sensor mAccelerometer, mGravity, mOrientation;
    TextView vwAccData, vwGraData, vwOriData;

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
        mSensorManager.registerListener(this, mGravity, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mOrientation, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
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
            vwGraData.setText(strGraData);
        }
        if(sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
            String strAccData = new String("");
            strAccData += "LINEAR ACCELERATION\n";
            strAccData += "x : " + event.values[0] + "\n";
            strAccData += "y : " + event.values[1] + "\n";
            strAccData += "z : " + event.values[2] + "\n";
            vwAccData.setText(strAccData);
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
}
