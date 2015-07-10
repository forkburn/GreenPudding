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

import com.greenpudding.R;
import com.greenpudding.free.model.Pudding;
import com.greenpudding.free.model.RenderMode;
import com.greenpudding.free.thread.PuddingRenderer;

/**
 * This implements the actual live wallpaper
 */
public class PuddingWallpaperEngine extends WallpaperService.Engine implements SensorEventListener,
        SharedPreferences.OnSharedPreferenceChangeListener {

    private final GreenPuddingWallpaperService wallpaperService;

    // the physical model of the pudding
    private Pudding pudding;

    private SurfaceHolder surfaceHolder;

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private boolean isAccelerometerPresent;

    // use to scale the hardware provided gravity
    private float gravityMultiplier = 0.5f;

    private PuddingRenderer puddingRenderer;
    private Thread puddingRendererThread;

    private SharedPreferences prefs;

    public PuddingWallpaperEngine(GreenPuddingWallpaperService wallpaperService) {
        wallpaperService.super();
        this.wallpaperService = wallpaperService;
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
        pudding.setGravity(-event.values[0] * gravityMultiplier, event.values[1] * gravityMultiplier);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // do nothing
    }

    @Override
    public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        super.onSurfaceChanged(holder, format, width, height);
        Rect boundingRect = new Rect(0, 0, width, height);
        pudding.setBoundingRect(boundingRect);
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
        puddingRenderer = new PuddingRenderer(surfaceHolder);
        puddingRenderer.setPudding(pudding);
        puddingRendererThread = new Thread(puddingRenderer);
        puddingRendererThread.start();
    }

    public void stopRendererThread() {
        boolean retry = true;
        puddingRenderer.setStopFlag(true);
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
                break;
            }

            case (MotionEvent.ACTION_UP): {
                // all touch ended
                pudding.stopDragging(event.getPointerId(0));
                break;
            }

            case (MotionEvent.ACTION_POINTER_UP): {
                // additional touch begins
                int pointerIndex = event.getActionIndex();
                pudding.stopDragging(event.getPointerId(pointerIndex));
                break;
            }

            case (MotionEvent.ACTION_POINTER_DOWN): {
                // an additional touch is ended
                int pointerIndex = event.getActionIndex();
                pudding.startDragging(event.getX(pointerIndex), event.getY(pointerIndex), event.getPointerId(pointerIndex));
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

        // apply the pudding size setting
        String radiusKey = wallpaperService.getString(R.string.pref_pudding_radius_key);
        int radius = Integer.parseInt(prefs.getString(radiusKey, "100"));
        pudding.setRadius(radius, false);

        // apply the number of nodes setting
        String numOfNodesKey = wallpaperService.getString(R.string.pref_number_of_nodes_key);
        int numOfNodes = Integer.parseInt(prefs.getString(numOfNodesKey, "12"));
        pudding.setNumOfNodes(numOfNodes);

        // apply the gravity setting
        String isGravityEnabledKey = wallpaperService.getString(R.string.pref_is_gravity_enabled_key);
        boolean isGravityEnabled = prefs.getBoolean(isGravityEnabledKey, true);
        pudding.setIsGravityEnabled(isGravityEnabled);

        // apply the central pinning setting
        String isPinnedKey = wallpaperService.getString(R.string.pref_is_pinned_key);
        boolean isPinned = prefs.getBoolean(isPinnedKey, false);
        pudding.setIsPinned(isPinned);

        // apply the render mode setting
        String renderModeKey = wallpaperService.getString(R.string.pref_render_mode_key);
        String renderMode = prefs.getString(renderModeKey, "");
        String[] validRenderModes = wallpaperService.getResources().getStringArray(R.array.pref_render_mode_value);
        if (renderMode.equals(validRenderModes[0])) {
            pudding.setRenderMode(RenderMode.NORMAL);
        } else if (renderMode.equals(validRenderModes[1])) {
            pudding.setRenderMode(RenderMode.WIREFRAME);
        }

        // apply the pudding color settings
        String puddingColorKey = wallpaperService.getString(R.string.pref_pudding_color_key);
        int defaultPuddingColor = wallpaperService.getResources().getColor(R.color.color_pudding_default);
        int puddingColor = prefs.getInt(puddingColorKey, defaultPuddingColor);
        pudding.setColor(puddingColor);

        // apply the pudding background color settings
        String backgroundColorKey = wallpaperService.getString(R.string.pref_background_color_key);
        int defaultBackgroundColor = wallpaperService.getResources().getColor(R.color.color_background_default);
        int backgroundColor = prefs.getInt(backgroundColorKey, defaultBackgroundColor);
        pudding.setBackgroundColor(backgroundColor);

        // refresh the position of all nodes
        pudding.refreshNodes();
    }
}