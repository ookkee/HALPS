package jp.hiralab.halps;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
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
    String strFileName = new String("");
    boolean recording = false;
    long referenceTime;
    float[] accMin,accMax,previousValues;
    double minThreshold = 2;
    float sampleMin,sampleMax,threshold;
    int sampleCounter, stepsTaken, spikingAxis;
    //Timer timer = new Timer();
    /*TimerTask myTask = new TimerTask(){
        @Override
        public void run() {
            // Reset the min&max values
            for(int i=0;i<3;i++){
                accMin[i] = 0;
                accMax[i] = 0;
            }
        }
    };*/

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sensordata);

        //timer.schedule(myTask, 10000, 10000);

        // Initialize views
        TextView vwSensorInfo = (TextView) findViewById(R.id.sensorinfo);
        vwAccData = (TextView) findViewById(R.id.accdata);
        vwGraData = (TextView) findViewById(R.id.gradata);
        vwOriData = (TextView) findViewById(R.id.oridata);

        // Query sensor services
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        List<Sensor> sensorList = mSensorManager.getSensorList(Sensor.TYPE_ALL);
        //mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
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
        if(isExternalStorageWritable())
            vwSensorInfo.append("\next storage is writable!\n");

        // initialize min&max values for acceleration axes
        accMin = new float[]{0,0,0};
        accMax = new float[]{0,0,0};
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

        // Set spiking axis to non-axis
        spikingAxis = 3;

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
            referenceTime = 0;
            writeData(strFileName + "_acc.csv", strAccFile);
            writeData(strFileName + "_gra.csv", strGraFile);
            writeData(strFileName + "_ori.csv", strOriFile);
            strAccFile = "";
            strGraFile = "";
            strOriFile = "";
            fileNameInput.setText(strFileName);
            btn.setText("Start");
        }
        else {
            // Start recording
            referenceTime = System.nanoTime();
            strFileName = fileNameInput.getText().toString();
            fileNameInput.setText("");
            fileNameInput.setHint("recording to " + strFileName);
            btn.setText("Stop");
        }
        recording = !recording;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        displayData(event);
        Sensor sensor = event.sensor;
        if(sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {

            // Get biggest and smallest values for x,y,z
            for(int i=0; i < 3; i++) {
                if(event.values[i] < accMin[i])
                    accMin[i] = event.values[i];
                else if(event.values[i] > accMax[i])
                    accMax[i] = event.values[i];
            }
            /*
            // Find largest spikes that are over the minimum threshold
            if(accMax[0]-accMin[0] > accMax[1]-accMin[1] &&
                accMax[0]-accMin[0] > accMax[2]-accMin[2] &&
                accMax[0]-accMin[0] > minThreshold) {
                vwAccData.append("\nBiggest spikes on x-axis!\n");
                spikingAxis = 0;
            }
            else if(accMax[1]-accMin[1] > accMax[0]-accMin[0] &&
                accMax[1]-accMin[1] > accMax[2]-accMin[2] &&
                accMax[1]-accMin[1] > minThreshold) {
                vwAccData.append("\nBiggest spikes on y-axis!\n");
                spikingAxis = 1;
            }
            else if(accMax[2]-accMin[2] > accMax[0]-accMin[0] &&
                accMax[2]-accMin[2] > accMax[1]-accMin[1] &&
                accMax[2]-accMin[2] > minThreshold) {
                vwAccData.append("\nBiggest spikes on z-axis!\n");
                spikingAxis = 2;
            }
            */
            if(spikingAxis != 3) {
                if(previousValues != null &&
                        previousValues[spikingAxis] < threshold &&
                        event.values[spikingAxis] >= threshold &&
                        (event.values[spikingAxis] - previousValues[spikingAxis]) > 0.3)
                    stepsTaken += 1;
            }

            // Sampling
            sampleCounter++;
            if(sampleCounter >= 50) {
                double[] diff = new double[3];
                for(int i=0;i<3;i++) 
                    diff[i] = accMax[i] - accMin[i];
                if(diff[0] > diff[1] && diff[0] > diff[2] &&
                        diff[0] > minThreshold)
                    spikingAxis = 0;
                else if(diff[1] > diff[0] && diff[1] > diff[2] &&
                        diff[1] > minThreshold)
                    spikingAxis = 1;
                else if(diff[2] > diff[0] && diff[2] > diff[1] &&
                        diff[2] > minThreshold)
                    spikingAxis = 2;
                else
                    spikingAxis = 3;
                if(spikingAxis != 3) {
                    sampleMin = accMin[spikingAxis];
                    sampleMax = accMax[spikingAxis];
                    threshold = (sampleMin + sampleMax)/2;
                }
                for(int i=0;i<3;i++) {
                    accMin[i] = 0;
                    accMax[i] = 0;
                }
                sampleCounter = 0;
            }
            vwAccData.append("\nSteps taken: " + stepsTaken + "\n");
            previousValues = Arrays.copyOf(event.values, event.values.length);
        }

    }

    /** Prints data of sensors to screen */
    public void displayData(SensorEvent event) {

        Sensor sensor = event.sensor;
        if (sensor.getType() == Sensor.TYPE_GRAVITY ||
                //sensor.getType() == Sensor.TYPE_ACCELEROMETER ||
                sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION ||
                sensor.getType() == Sensor.TYPE_ROTATION_VECTOR)
        {
            String strSensorData = new String("");
            String strSensorFile = new String("");
            strSensorData += sensor.getName() + "\n";
            strSensorData += "x : " + event.values[0] + "\n";
            strSensorData += "y : " + event.values[1] + "\n";
            strSensorData += "z : " + event.values[2] + "\n";
            strSensorFile += (event.timestamp - referenceTime) + ";" +
                    event.values[0] + ";" +
                    event.values[1] + ";" +
                    event.values[2] + "\n";

            switch(sensor.getType()) {
                //case Sensor.TYPE_ACCELEROMETER:
                case Sensor.TYPE_LINEAR_ACCELERATION:
                    // Print data on screen
                    vwAccData.setText(strSensorData);
                    vwAccData.append(referenceTime + "\n");
                    vwAccData.append(event.timestamp + "\n");
                    vwAccData.append((event.timestamp-referenceTime) + "\n");
                    // Write data to string if recording
                    if(recording)
                        strAccFile += strSensorFile;
                    break;
                case Sensor.TYPE_GRAVITY:
                    vwGraData.setText(strSensorData);
                    if(recording)
                        strGraFile += strSensorFile;
                    break;
                case Sensor.TYPE_ROTATION_VECTOR:
                    vwOriData.setText(strSensorData);
                    if(recording)
                        strOriFile += strSensorFile;
                    break;
            }
        }
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
        //mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        //mSensorManager.registerListener(this, mGravity, SensorManager.SENSOR_DELAY_NORMAL);
        //mSensorManager.registerListener(this, mOrientation, SensorManager.SENSOR_DELAY_NORMAL);
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
    /** Changes the minimum threshold */
    public void changeMinimumThreshold(View view) {
        EditText input = (EditText) findViewById(R.id.thresholdinput);
        if(input.getText().toString() == null || input.getText().toString().isEmpty()) {
            minThreshold = 1;
        }
        else {
            minThreshold = Double.parseDouble(input.getText().toString());
        }
    }
}
