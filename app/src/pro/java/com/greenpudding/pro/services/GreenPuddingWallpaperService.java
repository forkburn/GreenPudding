package com.greenpudding.pro.services;

import android.content.SharedPreferences;
import android.graphics.Rect;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.service.wallpaper.WallpaperService;
import android.view.MotionEvent;
import android.view.SurfaceHolder;

import com.greenpudding.activities.MainActivity;
import com.greenpudding.model.Pudding;
import com.greenpudding.thread.PuddingRenderThread;
import com.greenpudding.util.PuddingConfigurator;

public class GreenPuddingWallpaperService extends WallpaperService {

    public static final String SHARED_PREFS_NAME = "greenPuddingSettings";
    private PuddingWallpaperEngine engine;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (engine != null) {
            engine.stopRendererThread();
        }
    }

    @Override
    public Engine onCreateEngine() {
        engine = new PuddingWallpaperEngine(this);
        return engine;
    }


    private class PuddingWallpaperEngine extends WallpaperService.Engine implements SensorEventListener,
            SharedPreferences.OnSharedPreferenceChangeListener {

        // the physical model of the pudding
        private Pudding pudding;

        private SurfaceHolder surfaceHolder;

        private SensorManager sensorManager;
        private Sensor accelerometer;
        private boolean isAccelerometerPresent;

        private PuddingRenderThread puddingRenderThread;
        private Thread puddingRendererThread;

        private SharedPreferences prefs;

        public PuddingWallpaperEngine(GreenPuddingWallpaperService wallpaperService) {
            wallpaperService.super();
            // check the accelerometer
            sensorManager = (SensorManager) wallpaperService.getSystemService(GreenPuddingWallpaperService.SENSOR_SERVICE);
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            if (accelerometer != null) {
                isAccelerometerPresent = true;
            } else {
                isAccelerometerPresent = false;
            }

            // create the pudding model
            pudding = new Pudding();
            pudding.refreshNodes();

            // get the prefs
            prefs = wallpaperService.getSharedPreferences(GreenPuddingWallpaperService.SHARED_PREFS_NAME, 0);
            prefs.registerOnSharedPreferenceChangeListener(this);
            applyPrefs();
        }

        @Override
        public void onCreate(SurfaceHolder surfaceHolder) {
            super.onCreate(surfaceHolder);
            this.surfaceHolder = surfaceHolder;

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
                // start listening on the sensor
                if (isAccelerometerPresent && pudding.getIsGravityEnabled()) {
                    sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
                }
                startRendererThread();
            } else {
                // stop listening on the sensor
                if (isAccelerometerPresent) {
                    sensorManager.unregisterListener(this);
                }
                // stop the renderer thread
                stopRendererThread();
            }
        }

        @Override
        public void onSensorChanged(SensorEvent event) {
            // need to invert the x value since the sensor and the drawing api
            // has different coordinates
            pudding.setGravity(-event.values[0] * MainActivity.GRAVITY_SCALER,
                    event.values[1] * MainActivity.GRAVITY_SCALER);
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            // do nothing
        }

        @Override
        public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            super.onSurfaceChanged(holder, format, width, height);
            pudding.setBoundingRect(new Rect(0, 0, width, height));
        }

        @Override
        public void onSurfaceCreated(SurfaceHolder holder) {
            super.onSurfaceCreated(holder);
        }

        @Override
        public void onSurfaceDestroyed(SurfaceHolder holder) {
            super.onSurfaceDestroyed(holder);
            stopRendererThread();
        }

        public void startRendererThread() {
            puddingRenderThread = new PuddingRenderThread(surfaceHolder);
            puddingRenderThread.setPudding(pudding);
            puddingRendererThread = new Thread(puddingRenderThread);
            puddingRendererThread.start();
        }

        public void stopRendererThread() {
            boolean retry = true;
            puddingRenderThread.setStopFlag(true);
            while (retry) {
                try {
                    puddingRendererThread.join();
                    retry = false;
                } catch (InterruptedException e) {
                }
            }
        }

        @Override
        public void onTouchEvent(MotionEvent event) {

            int action = event.getActionMasked();

            switch (action) {

                case (MotionEvent.ACTION_DOWN): {
                    // First touch begins
                    pudding.startDragging(event.getX(), event.getY(), event.getPointerId(0));
                    pudding.setMousePos(event.getX(), event.getY(), event.getPointerId(0));
                    break;
                }

                case (MotionEvent.ACTION_UP): {
                    // all touch ended
                    pudding.stopDragging(event.getPointerId(0));
                    break;
                }
                case (MotionEvent.ACTION_POINTER_DOWN): {
                    // an additional touch begins
                    int idx = event.getActionIndex();
                    pudding.startDragging(event.getX(idx), event.getY(idx), event.getPointerId(idx));
                    pudding.setMousePos(event.getX(idx), event.getY(idx), event.getPointerId(idx));
                    break;
                }
                case (MotionEvent.ACTION_POINTER_UP): {
                    // additional touch begins
                    int pointerIndex = event.getActionIndex();
                    pudding.stopDragging(event.getPointerId(pointerIndex));
                    break;
                }
                case (MotionEvent.ACTION_MOVE): {
                    // Contact has moved across screen
                    for (int i = 0; i < event.getPointerCount(); i++) {
                        // save the position of all active pointers
                        pudding.setMousePos(event.getX(i), event.getY(i), event.getPointerId(i));
                    }
                    break;
                }
                case (MotionEvent.ACTION_CANCEL): {
                    // Touch event canceled
                    pudding.stopDragging(event.getPointerId(0));
                    break;
                }
                default:
            }
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences arg0, String arg1) {
            applyPrefs();
        }

        private void applyPrefs() {
            PuddingConfigurator.applyPrefs(pudding, prefs, GreenPuddingWallpaperService.this);
        }
    }
}
