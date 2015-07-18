package com.greenpudding.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;

import com.greenpudding.PuddingFacade;
import com.greenpudding.R;
import com.greenpudding.view.PuddingSurfaceView;

public class MainActivity extends Activity {

    private PuddingFacade puddingFacade = new PuddingFacade();
    private static Context context;
    public static Context getContext() {
        return context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
    }

    protected void init() {
        context = getApplicationContext();

        // hide title
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        // hide status bar
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // check the accelerometer
        puddingFacade.setSensorManager((SensorManager) getSystemService(SENSOR_SERVICE));

        // get the preferences
        puddingFacade.setPrefs(PreferenceManager.getDefaultSharedPreferences(this));

        // create instance of the PuddingView, and show the view
        setContentView(R.layout.pudding_layout);

        // get a handle to the view
        PuddingSurfaceView puddingView = (PuddingSurfaceView) findViewById(R.id.puddingView);

        // give a handle of the pudding to the view
        puddingView.setPuddingFacade(puddingFacade);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // in case user has changed something in pref page, need to apply new prefs here
        puddingFacade.applyPrefs();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        // do nothing on orientation change
        super.onConfigurationChanged(newConfig);
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