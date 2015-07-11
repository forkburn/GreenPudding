package com.greenpudding.free.activities;

import android.app.Activity;
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
import com.greenpudding.free.model.Pudding;
import com.greenpudding.free.model.RenderMode;
import com.greenpudding.free.view.PuddingSurfaceView;

public class MainActivity extends Activity implements SensorEventListener {

    // the physical model of the pudding
    private Pudding pudding;

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private boolean isAccelerometerPresent;

    private PuddingSurfaceView puddingView;

    // use to scale the hardware provided gravity
    private float gravityMultiplier = 0.5f;

    // a reference to the app preference
    SharedPreferences prefs;

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
        if (accelerometer != null) {
            isAccelerometerPresent = true;
        } else {
            isAccelerometerPresent = false;
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

        // apply the pudding size setting
        String radiusKey = getString(R.string.pref_pudding_radius_key);
        int radius = Integer.parseInt(prefs.getString(radiusKey, "100"));
        pudding.setRadius(radius, false);

        // apply the number of nodes setting
        String numOfNodesKey = getString(R.string.pref_number_of_nodes_key);
        int numOfNodes = Integer.parseInt(prefs.getString(numOfNodesKey, "12"));
        pudding.setNumOfNodes(numOfNodes);

        // apply the gravity setting
        String isGravityEnabledKey = getString(R.string.pref_is_gravity_enabled_key);
        boolean isGravityEnabled = prefs.getBoolean(isGravityEnabledKey, true);
        pudding.setIsGravityEnabled(isGravityEnabled);

        // apply the central pinning setting
        String isPinnedKey = getString(R.string.pref_is_pinned_key);
        boolean isPinned = prefs.getBoolean(isPinnedKey, false);
        pudding.setIsPinned(isPinned);

        // apply the render mode setting
        String renderModeKey = getString(R.string.pref_render_mode_key);
        String renderMode = prefs.getString(renderModeKey, "");
        String[] validRenderModes = getResources().getStringArray(R.array.pref_render_mode_value);
        if (renderMode.equals(validRenderModes[0])) {
            pudding.setRenderMode(RenderMode.NORMAL);
        } else if (renderMode.equals(validRenderModes[1])) {
            pudding.setRenderMode(RenderMode.WIREFRAME);
        }

        // apply the pudding color settings
        String puddingColorKey = getString(R.string.pref_pudding_color_key);
        int defaultPuddingColor = getResources().getColor(R.color.color_pudding_default);
        int puddingColor = prefs.getInt(puddingColorKey, defaultPuddingColor);
        pudding.setColor(puddingColor);

        // apply the pudding background color settings
        String backgroundColorKey = getString(R.string.pref_background_color_key);
        int defaultBackgroundColor = getResources().getColor(R.color.color_background_default);
        int backgroundColor = prefs.getInt(backgroundColorKey, defaultBackgroundColor);
        pudding.setBackgroundColor(backgroundColor);

        // refresh the position of all nodes
        pudding.refreshNodes();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // need to invert the x value since the sensor and the drawing api has
        // different coordinate system
        pudding.setGravity(-event.values[0] * gravityMultiplier, event.values[1] * gravityMultiplier);
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
        // Handle item selection
        int selectedId = item.getItemId();
        if (selectedId == R.id.menu_options) {
            // launch the preferences screen
            getFragmentManager().beginTransaction()
                    .replace(android.R.id.content, new PuddingPreferencesFragment())
                    .commit();
            return true;
        } else if (selectedId == R.id.menu_exit) {
            finish();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }
}