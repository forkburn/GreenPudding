package com.greenpudding.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;

import com.greenpudding.R;
import com.greenpudding.model.Pudding;
import com.greenpudding.util.PuddingConfigurator;
import com.greenpudding.view.PuddingSurfaceView;

public class MainActivity extends Activity implements SensorEventListener {

    // use to scale the hardware provided gravity
    public static float GRAVITY_SCALER = 0.5f;
    private final String PREF_FRAGMENT_TAG = "PREF_FRAGMENT_TAG";
    // a reference to the app preference
    SharedPreferences prefs;
    // the physical model of the pudding
    private Pudding pudding;
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private boolean isAccelerometerPresent;
    private PuddingSurfaceView puddingView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
    }

    protected void init() {

        // hide title
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        // hide status bar
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // check the accelerometer
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if (accelerometer == null) {
            isAccelerometerPresent = false;
        } else {
            isAccelerometerPresent = true;
        }

        // get the preferences
        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        // create instance of the PuddingView, and show the view
        setContentView(R.layout.pudding_layout);

        // get a handle to the view
        puddingView = (PuddingSurfaceView) findViewById(R.id.puddingView);

        // create the pudding model
        pudding = new Pudding();

        // give a handle of the pudding to the view
        puddingView.setPudding(pudding);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // apply the pref to the pudding
        applyPrefs();

        // start listening on the sensor
        if (isAccelerometerPresent && pudding.getIsGravityEnabled()) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        // stop monitoring the sensor
        if (isAccelerometerPresent) {
            sensorManager.unregisterListener(this);
        }
    }

    private void applyPrefs() {
        PuddingConfigurator.applyPrefs(pudding, prefs, this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // need to invert the x value since the sensor and the drawing api has
        // different coordinate system
        pudding.setGravity(-event.values[0] * GRAVITY_SCALER, event.values[1] * GRAVITY_SCALER);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // do nothing
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // do nothing on orientation change
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // init the options menu on menu button press
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int selectedId = item.getItemId();
        if (selectedId == R.id.menu_options) {
            // launch the preferences screen
            startActivity(new Intent(this, PuddingPreferencesActivity.class));
            return true;
        } else if (selectedId == R.id.menu_exit) {
            finish();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

}