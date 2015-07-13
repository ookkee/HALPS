package jp.hiralab.halps;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends Activity {
    /** Called when the activity is first created.
     * Currently this activity is simply a portal for other activities, like a menu
     * */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
    }

    /** Called when the user clicks the Sensor Data button
     */
    public void startSensorData(View view) {
        Intent intent = new Intent(this, SensorDataActivity.class);
        startActivity(intent);
    }

    /** Starts the step and distance calculating app
     */
    public void startStepCounter(View view) {
        Intent intent = new Intent(this, StepCounterActivity.class);
        startActivity(intent);
    }
}
