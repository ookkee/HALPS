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
import android.text.method.KeyListener;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class SensorDataActivity extends Activity implements SensorEventListener
{
    private SensorManager mSensorManager;
    private Sensor mAccelerometer, mGravity, mOrientation;
    TextView vwAccData, vwGraData, vwOriData;
    String strAccFile, strGraFile, strOriFile;
    boolean recording = false;

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

    }

    /** Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    /** Called when user clicks button_rec.
     * Records data to string and records it to file */
    public void recordData(View view) {
        Button btn = (Button) findViewById(R.id.button_rec);
        EditText fileNameInput = (EditText) findViewById(R.id.filenameinput);
        if(recording) {
            // Stop recording and write to file
            String strFileName = new String(fileNameInput.getText().toString());
            writeData(strFileName + "_acc.csv", strAccFile);
            strAccFile = "";
            writeData(strFileName + "_gra.csv", strGraFile);
            strGraFile = "";
            writeData(strFileName + "_ori.csv", strOriFile);
            strOriFile = "";
            fileNameInput.setKeyListener((KeyListener)fileNameInput.getTag());
            btn.setText("Start");
        }
        else {
            // Start recording
            fileNameInput.setTag(fileNameInput.getKeyListener());
            btn.setText("Stop");
        }
        recording = !recording;
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

            if(recording) {
                strGraFile += event.timestamp + ";" +
                    event.values[0] + ";" +
                    event.values[1] + ";" +
                    event.values[2] + "\n";
            }
        }
        if(sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
            String strAccData = new String("");
            strAccData += "LINEAR ACCELERATION\n";
            strAccData += "x : " + event.values[0] + "\n";
            strAccData += "y : " + event.values[1] + "\n";
            strAccData += "z : " + event.values[2] + "\n";
            vwAccData.setText(strAccData);

            if(recording) {
                strAccFile += event.timestamp + ";" +
                    event.values[0] + ";" +
                    event.values[1] + ";" +
                    event.values[2] + "\n";
            }
        }
        if (sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
            String strOriData = new String("");
            strOriData += "ORIENTATION\n";
            strOriData += "x : " + event.values[0] + "\n";
            strOriData += "y : " + event.values[1] + "\n";
            strOriData += "z : " + event.values[2] + "\n";
            vwOriData.setText(strOriData);

            if(recording) {
                strOriFile += event.timestamp + ";" +
                    event.values[0] + ";" +
                    event.values[1] + ";" +
                    event.values[2] + "\n";
            }
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

    /** Writes data to external storage if available */
    public void writeData(String filename, String data) {

        // Get files for writing data
        if(isExternalStorageWritable()) {
            File f = new File(getExternalFilesDir(null), filename);
            // Write data
            try {
                FileOutputStream fOutStream = new FileOutputStream(f);
                PrintWriter pw = new PrintWriter(fOutStream);
                pw.print(data);
                pw.flush();
                pw.close();
                fOutStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
