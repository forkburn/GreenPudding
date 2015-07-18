package com.greenpudding.pro.services;

import android.content.SharedPreferences;
import android.graphics.Rect;
import android.hardware.SensorManager;
import android.service.wallpaper.WallpaperService;
import android.view.MotionEvent;
import android.view.SurfaceHolder;

import com.greenpudding.PuddingFacade;

public class GreenPuddingWallpaperService extends WallpaperService {

    public static final String SHARED_PREFS_NAME = "greenPuddingWallpaperSettings";
    private PuddingWallpaperEngine engine;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public Engine onCreateEngine() {
        engine = new PuddingWallpaperEngine(this);
        return engine;
    }


    private class PuddingWallpaperEngine extends WallpaperService.Engine implements SharedPreferences.OnSharedPreferenceChangeListener {

        private PuddingFacade puddingFacade = new PuddingFacade();
        /*// the physical model of the pudding
        private PuddingModel pudding;

        private SurfaceHolder surfaceHolder;

        private SensorManager sensorManager;
        private Sensor accelerometer;
        private boolean isAccelerometerPresent;

        private PuddingRunner puddingRunner;
        private Thread puddingRunnerThread;

        private SharedPreferences prefs;*/

        public PuddingWallpaperEngine(GreenPuddingWallpaperService wallpaperService) {
            wallpaperService.super();
            // check the accelerometer
            puddingFacade.setSensorManager((SensorManager) wallpaperService.getSystemService(GreenPuddingWallpaperService.SENSOR_SERVICE));

            // get the prefs
            SharedPreferences prefs = wallpaperService.getSharedPreferences(GreenPuddingWallpaperService.SHARED_PREFS_NAME, 0);
            prefs.registerOnSharedPreferenceChangeListener(this);
            puddingFacade.setPrefs(prefs);
        }

        @Override
        public void onCreate(SurfaceHolder surfaceHolder) {
            super.onCreate(surfaceHolder);
            puddingFacade.setSurfaceHolder(surfaceHolder);

            // user can play with the pudding. so we need touch event
            setTouchEventsEnabled(true);
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            if (visible) {
                puddingFacade.start();
            } else {
                puddingFacade.stop();
            }
        }

        @Override
        public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            super.onSurfaceChanged(holder, format, width, height);
            puddingFacade.setBoundingRect(new Rect(0, 0, width, height));
        }

        @Override
        public void onSurfaceCreated(SurfaceHolder holder) {
            super.onSurfaceCreated(holder);
        }

        @Override
        public void onSurfaceDestroyed(SurfaceHolder holder) {
            super.onSurfaceDestroyed(holder);
        }


        @Override
        public void onTouchEvent(MotionEvent event) {
            puddingFacade.onTouchEvent(event);
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences arg0, String arg1) {
            puddingFacade.applyPrefs();
        }
    }
}
