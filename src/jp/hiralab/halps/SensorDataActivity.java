package jp.hiralab.halps;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.List;

import android.app.Activity;
import android.graphics.Color;
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

import jp.hiralab.halps.MySensor;

public class SensorDataActivity extends Activity implements SensorEventListener
{
    private SensorManager mSensorManager;
    String strFileNameTrunk = new String("");
    String strVerAcc = new String("");
    boolean recording = false;
    long recStartTime;
    float[] previousValues;
    double minThreshold, minRelevantAcc;
    double[] gravity, acceleration;
    float sampleMin,sampleMax,threshold;
    float oldRelevantValue,newRelevantValue;
    int sampleCounter, sampleInterval, stepsTaken, spikingAxis;
    private static int sensorDelay = SensorManager.SENSOR_DELAY_UI;
    MySensor[] mySensors = new MySensor[4];

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sensordata);

        // Initialize views
        TextView vwSensorInfo = (TextView) findViewById(R.id.sensorinfo);

        // Query sensor services
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        List<Sensor> sensorList = mSensorManager.getSensorList(Sensor.TYPE_ALL);
        // Populate list with wanted sensors and their associated text view
        if(mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION) != null) {
            mySensors[0] = new MySensor(
                        mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION),
                        (TextView) findViewById(R.id.accdata));
        }
        else 
            vwSensorInfo.append("No sensor for linear acc!\n");
        if(mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY) != null) {
            mySensors[1] = new MySensor(
                        mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY),
                        (TextView) findViewById(R.id.gradata));
        }
        else 
            vwSensorInfo.append("No sensor for gravity!\n");
        if(mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR) != null) {
            mySensors[2] = new MySensor(
                        mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR),
                        (TextView) findViewById(R.id.oridata));
        }
        else 
            vwSensorInfo.append("No sensor for rotation!\n");
        if(mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE) != null) {
            mySensors[3] = new MySensor(
                        mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE),
                        (TextView) findViewById(R.id.gyrdata));
        }
        else 
            vwSensorInfo.append("No sensor for gyroscope!\n");

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

        // Register sensor listeners
        for (MySensor mySensor : mySensors)
            mSensorManager.registerListener(this, mySensor.getSensor(), sensorDelay);

        minThreshold = 1.5;
        // Set spiking axis to non-axis
        spikingAxis = 3;
        // Sampling variables
        sampleCounter = 0;
        sampleInterval = 50;
        // Set relevant values
        oldRelevantValue = 0;
        newRelevantValue = 0;
        minRelevantAcc = 1.5;

        gravity = new double[] {0,0,0};
        acceleration = new double[] {0,0,0};

    }

    /** Called when user clicks button_rec.
     * Records data to string and records it to file */
    public void recordData(View view) {
        Button btn = (Button) findViewById(R.id.button_rec);
        EditText fileNameInput = (EditText) findViewById(R.id.filenameinput);
        if(recording) {
            // Stop recording and write to file
            //write the files & reset the recording variables
            for(MySensor mySensor : mySensors) {
                writeData(mySensor.getFilename(), mySensor.getCsv());
                mySensor.resetCsv();
            }
            //reset the ui & values
            recStartTime = 0;
            fileNameInput.setHint("filename trunk");
            btn.setText("Start");
        }
        else {
            // Start recording
            // make a record of current time
            recStartTime = System.nanoTime();
            // set filenames for sensors
            for(MySensor mySensor : mySensors) 
                mySensor.setFilename(fileNameInput.getText().toString(), sensorDelay);
            // update UI
            fileNameInput.setHint("recording to " + fileNameInput.getText().toString());
            fileNameInput.setText("");
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
        for(MySensor mySensor : mySensors) {
            if(mySensor.getSensor().getType() == event.sensor.getType())
                // Display the new data
                mySensor.newValues(event.timestamp - recStartTime, event.values, recording);
        }

        if(sensor.getType() == Sensor.TYPE_GRAVITY) {
            if(event.values[0] > event.values[1] &&
                    event.values[0] > event.values[2])
                spikingAxis = 0;
            else if(event.values[1] > event.values[0] &&
                    event.values[1] > event.values[2])
                spikingAxis = 1;
            else if(event.values[2] > event.values[1] &&
                    event.values[2] > event.values[0])
                spikingAxis = 2;
            else
                spikingAxis = 3;

            for(int i=0;i<3;i++)
                gravity[i] = event.values[i] / 9.81;
        }
        if(sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {

            // Get biggest and smallest values for x,y,z
            /*
            for(int i=0; i < 3; i++) {
                if(event.values[i] < accMin[i])
                    accMin[i] = event.values[i];
                else if(event.values[i] > accMax[i])
                    accMax[i] = event.values[i];

                acceleration[i] = event.values[i] * gravity[i];
            }
            */
            /*
            // Find largest spikes that are over the minimum threshold
            if(accMax[0]-accMin[0] > accMax[1]-accMin[1] &&
                accMax[0]-accMin[0] > accMax[2]-accMin[2] &&
                accMax[0]-accMin[0] > minThreshold) {
                //vwAccData.append("\nBiggest spikes on x-axis!\n");
                spikingAxis = 0;
            }
            else if(accMax[1]-accMin[1] > accMax[0]-accMin[0] &&
                accMax[1]-accMin[1] > accMax[2]-accMin[2] &&
                accMax[1]-accMin[1] > minThreshold) {
                //vwAccData.append("\nBiggest spikes on y-axis!\n");
                spikingAxis = 1;
            }
            else if(accMax[2]-accMin[2] > accMax[0]-accMin[0] &&
                accMax[2]-accMin[2] > accMax[1]-accMin[1] &&
                accMax[2]-accMin[2] > minThreshold) {
                //vwAccData.append("\nBiggest spikes on z-axis!\n");
                spikingAxis = 2;
            }
            */
            ////// RELEVANT CHANGES IN VALUES //////
            oldRelevantValue = newRelevantValue;
            if(spikingAxis != 3) {
                if(Math.abs(event.values[spikingAxis] - oldRelevantValue) >= minRelevantAcc)
                    newRelevantValue = event.values[spikingAxis];
                //vwAccData.append("Old rel: " + oldRelevantValue + "\nNew rel: " + newRelevantValue + "\n");
            }

            ////// CALCULATE STEPS //////
            //if(oldRelevantValue > threshold && newRelevantValue < threshold)
            //    stepsTaken += 1;

            if(spikingAxis != 3 && previousValues != null) {
                if(previousValues[spikingAxis] > threshold &&
                    event.values[spikingAxis] < threshold &&
                    Math.abs(event.values[spikingAxis] - previousValues[spikingAxis]) >= minRelevantAcc) {
                    stepsTaken += 1;
                }
            }
            /*
            if(spikingAxis != 3) {
                if(previousValues != null &&
                        event.values[spikingAxis] < threshold &&
                        (previousValues[spikingAxis] - event.values[spikingAxis]) > 0.3)
                    stepsTaken += 1;
            }
            */

            ////// SAMPLING //////
            //sampleCounter++;
            //if(sampleCounter >= sampleInterval) {
            //    /*
            //    double[] diff = new double[3];
            //    // Get difference between min & max for each axis
            //    for(int i=0;i<3;i++) 
            //        diff[i] = accMax[i] - accMin[i];
            //    if(diff[0] > diff[1] && diff[0] > diff[2] &&
            //            diff[0] > minThreshold)
            //        spikingAxis = 0; // x is spiking
            //    else if(diff[1] > diff[0] && diff[1] > diff[2] &&
            //            diff[1] > minThreshold)
            //        spikingAxis = 1; // y is spiking
            //    else if(diff[2] > diff[0] && diff[2] > diff[1] &&
            //            diff[2] > minThreshold)
            //        spikingAxis = 2; // z is spiking
            //    else
            //        spikingAxis = 3; // no axis is spiking over the min threshold
            //    */
            //    if(spikingAxis != 3) {
            //        // Assign dynamic threshold
            //        sampleMin = accMin[spikingAxis];
            //        sampleMax = accMax[spikingAxis];
            //        threshold = (sampleMin + sampleMax)/2;
            //    }
            //    for(int i=0;i<3;i++) {
            //        // re-init min & max for all axes
            //        accMin[i] = 0;
            //        accMax[i] = 0;
            //    }
            //    // re-init sampling time
            //    sampleCounter = 0;
            //}
            //vwAccData.append("\nSteps taken: " + stepsTaken + "\n");
            //vwAccData.append("\nSpiking axis: " + spikingAxis + "\n");
            //previousValues = Arrays.copyOf(event.values, event.values.length);
        }

    }
    /** Prints data of sensors to screen */
    /*
    public void displayData(SensorEvent event) {

        Sensor sensor = event.sensor;
        for(MySensor mySensor : mySensors) {
        }
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
            strSensorFile += (event.timestamp - recStartTime) + ";" +
                    event.values[0] + ";" +
                    event.values[1] + ";" +
                    event.values[2] + "\n";

            switch(sensor.getType()) {
                //case Sensor.TYPE_ACCELEROMETER:
                case Sensor.TYPE_LINEAR_ACCELERATION:
                    // Print data on screen
                    vwAccData.setText(strSensorData);
                    vwAccData.append("time: " + event.timestamp + "\n");
                    vwAccData.append("min threshold: " + minThreshold + "\n");
                    vwAccData.append("sampling interval: " + sampleInterval + "\n");
                    vwAccData.append("min rel acc: " + minRelevantAcc + "\n");
                    // Write data to string if recording
                    if(recording) {
                        strVerAcc += (event.timestamp - recStartTime) + ";" +
                            acceleration[0] + ";" +
                            acceleration[1] + ";" +
                            acceleration[2] + "\n"; 

                        strAccFile += strSensorFile;
                    }
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
    */
    @Override
    protected void onResume() {
        super.onResume();
        for(MySensor mySensor : mySensors)
            mSensorManager.registerListener(this, mySensor.getSensor(), sensorDelay);
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
    /** Changes the minimum threshold */
    public void changeMinimumThreshold(View view) {
        EditText input = (EditText) findViewById(R.id.thresholdinput);
        if(input.getText().toString() == null || input.getText().toString().isEmpty()) {
            // Do nothing
        }
        else {
            minThreshold = Double.parseDouble(input.getText().toString());
        }
    }
    /** Changes the sampling interval */
    public void changeSampleInterval(View view) {
        EditText input = (EditText) findViewById(R.id.intervalinput);
        if(input.getText().toString() == null || input.getText().toString().isEmpty()) {
            // Do nothing
        }
        else {
            sampleInterval = Integer.parseInt(input.getText().toString());
        }
    }
    /** Changes the minimum relevant acceleration change */
    public void changeRelevantAcc(View view) {
        EditText input = (EditText) findViewById(R.id.relevantaccinput);
        if(input.getText().toString() == null || input.getText().toString().isEmpty()) {
            // Do nothing
        }
        else {
            minRelevantAcc = Double.parseDouble(input.getText().toString());
        }
    }
    /** Resets the step count */
    public void resetStepCount(View view) {
        stepsTaken = 0;
    }
    /** Changes sensor delay */
    public void changeSensorDelay(View view) {
        Button btn;
        switch(sensorDelay) {
            case SensorManager.SENSOR_DELAY_NORMAL:
                btn = (Button) findViewById(R.id.delaynormal);
                btn.setTextColor(Color.WHITE);
                break;
            case SensorManager.SENSOR_DELAY_UI:
                btn = (Button) findViewById(R.id.delayui);
                btn.setTextColor(Color.WHITE);
                break;
            case SensorManager.SENSOR_DELAY_GAME:
                btn = (Button) findViewById(R.id.delaygame);
                btn.setTextColor(Color.WHITE);
                break;
            case SensorManager.SENSOR_DELAY_FASTEST:
                btn = (Button) findViewById(R.id.delayfastest);
                btn.setTextColor(Color.WHITE);
                break;
        }
        switch(view.getId()) {

            case R.id.delaynormal:
                sensorDelay = SensorManager.SENSOR_DELAY_NORMAL;
                btn = (Button) findViewById(R.id.delaynormal);
                btn.setTextColor(Color.CYAN);
                break;
            case R.id.delayui:
                sensorDelay = SensorManager.SENSOR_DELAY_UI;
                btn = (Button) findViewById(R.id.delayui);
                btn.setTextColor(Color.CYAN);
                break;
            case R.id.delaygame:
                sensorDelay = SensorManager.SENSOR_DELAY_GAME;
                btn = (Button) findViewById(R.id.delaygame);
                btn.setTextColor(Color.CYAN);
                break;
            case R.id.delayfastest:
                sensorDelay = SensorManager.SENSOR_DELAY_FASTEST;
                btn = (Button) findViewById(R.id.delayfastest);
                btn.setTextColor(Color.CYAN);
                break;
        }
        mSensorManager.unregisterListener(this);
        for(MySensor mySensor : mySensors)
            mSensorManager.registerListener(this, mySensor.getSensor(), sensorDelay);
    }

    /** Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }
}
