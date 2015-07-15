package jp.hiralab.halps;

import java.text.DecimalFormat;
import java.util.LinkedList;
import java.util.Queue;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import jp.hiralab.halps.SensorData;

public class StepCounterActivity extends Activity implements SensorEventListener
{

    private static final float MIN_SLOPE = 2.5f;
    private static final int SEN_DELAY = SensorManager.SENSOR_DELAY_UI;

    private SensorManager sm; 
    private boolean peak, valley;
    private Queue<float[]> slopeQueue = new LinkedList<float[]>();
    private SensorData[] sensors = new SensorData[3];
    private double stepLength;
    private int stepsTaken;
    private float distanceTraveled;

    private TextView vwStepsTaken;
    private TextView vwDistanceTraveled;

    private DecimalFormat f = new DecimalFormat("#,##0.0");

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.stepcounter);

        // Set up sensor services
        sm = (SensorManager) getSystemService(SENSOR_SERVICE);
        // Linear acceleration
        if(sm.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION) != null) 
            sensors[0] = new SensorData(sm.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION));
        
        else {
            // TODO: ERROR
        }
        // Gravity
        if(sm.getDefaultSensor(Sensor.TYPE_GRAVITY) != null) 
            sensors[1] = new SensorData(sm.getDefaultSensor(Sensor.TYPE_GRAVITY));
        
        else {
            // TODO: ERROR
        }
        // Pseudo-sensor: vertical acceleration - uses gravity and lin.acc.
        sensors[2] = new SensorData(null);
        // Register listeners
        for(int i=0; i<sensors.length; i++)
            sm.registerListener(this, sensors[i].getSensor(), SEN_DELAY);

        stepLength = 0;
        stepsTaken = 0;
        distanceTraveled = 0;
        peak = false;
        valley = false;

        vwStepsTaken = (TextView) findViewById(R.id.stepstaken);
        vwDistanceTraveled = (TextView) findViewById(R.id.distancetraveled);

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        for(int i=0; i<sensors.length; i++) {
            // save the current values for each sensor
            if(sensors[i].getSensor() != null) {
                if(sensors[i].getSensor().getType() == event.sensor.getType())
                    sensors[i].newValues(event.timestamp, event.values);
            }
            else {
                // if both lin.acc. and gravity sensors have been called,
                // calculate vertical acceleration and step calculation
                if(event.sensor.getType() == Sensor.TYPE_GRAVITY) {
                    float[] verticalValues = new float[3];
                    for(int j=0; j<3; j++) {
                        verticalValues[j] = (sensors[1].getCurrentValues()[j] / 9.81f)
                            * sensors[0].getCurrentValues()[j];
                    }
                    sensors[i].newValues(event.timestamp, verticalValues);
                    stepCalculation();
                }
            }
        }
    }

    /** Called when new values for all necessary sensors have been aquired.
     *  This step calculation method assumes we know the amount of vertical
     *  acceleration of the user. The slope of the acceleration over time is
     *  calculated and a step is added when the slope has reached a steep enough
     *  amount both upwards and downwards.
     */
    public void stepCalculation() {
        float[] newSlopeData = new float[2];
        newSlopeData[0] = sensors[2].getValueTime() * (float)Math.pow(10,-9);
        // sum of all vertical acceleration
        newSlopeData[1] = sensors[2].getFilteredValues()[0] +
            sensors[2].getFilteredValues()[1] +
            sensors[2].getFilteredValues()[2];
        slopeQueue.add(newSlopeData);
        if(slopeQueue.size() >= 5) {
            //slope = (y2-y1)/(x2-x1)
            float[] oldSlopeData = slopeQueue.remove();
            float slope = (newSlopeData[1]-oldSlopeData[1])/(newSlopeData[0]-oldSlopeData[0]);
            if(slope >= MIN_SLOPE && !valley) 
                peak = true;
            else if(slope * -1 >= MIN_SLOPE && peak && newSlopeData[1] < -0.33)
                valley = true;
            if(peak && valley) {
                stepsTaken++;
                distanceTraveled = (float)stepLength * stepsTaken;
                peak = false;
                valley = false;
                vwStepsTaken.setText(String.valueOf(stepsTaken));
                vwDistanceTraveled.setText(f.format(distanceTraveled) + " m");
            }
        }
    }

    public void setStepLength(View view) {
        EditText input = (EditText) findViewById(R.id.steplengthinput);
        if(!input.getText().toString().isEmpty() &&
                input.getText().toString() != null) {
            stepLength = Double.parseDouble(input.getText().toString());
            input.setHint(input.getText().toString() + ", 0.4 * height");
            input.setText("");
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        //TODO
    }
    @Override
    protected void onResume() {
        super.onResume();
        for(int i=0; i<sensors.length; i++)
            sm.registerListener(this, sensors[i].getSensor(), SEN_DELAY);
    }
    @Override
    protected void onPause() {
        super.onPause();
        sm.unregisterListener(this);
    }
}
