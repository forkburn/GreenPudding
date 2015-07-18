package com.greenpudding;


import android.content.SharedPreferences;
import android.graphics.Rect;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.view.MotionEvent;
import android.view.SurfaceHolder;

import com.greenpudding.model.PuddingModel;
import com.greenpudding.thread.PuddingRunner;
import com.greenpudding.util.PuddingConfigurator;

public class PuddingFacade implements SensorEventListener {

    // use to scale the hardware provided gravity
    public static float GRAVITY_SCALER = 0.5f;
    // a reference to the app preference
    private SharedPreferences prefs;
    // the physical model of the pudding
    private PuddingModel pudding = new PuddingModel();
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private boolean isAccelerometerPresent;

    private SurfaceHolder surfaceHolder;

    private PuddingRunner puddingRunner;
    private Thread puddingRunnerThread;


    public SharedPreferences getPrefs() {
        return prefs;
    }

    public void setPrefs(SharedPreferences prefs) {
        this.prefs = prefs;
        applyPrefs();
    }

    public SensorManager getSensorManager() {
        return sensorManager;
    }

    public void setSensorManager(SensorManager sensorManager) {
        this.sensorManager = sensorManager;
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if (accelerometer == null) {
            isAccelerometerPresent = false;
        } else {
            isAccelerometerPresent = true;
        }

    }


    public SurfaceHolder getSurfaceHolder() {
        return surfaceHolder;
    }

    public void setSurfaceHolder(SurfaceHolder surfaceHolder) {
        this.surfaceHolder = surfaceHolder;
    }

    public void setBoundingRect(Rect rect) {
        pudding.setBoundingRect(rect);
    }

    public void start() {
        // start listening on the sensor
        if (isAccelerometerPresent && pudding.getIsGravityEnabled()) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        }

        puddingRunner = new PuddingRunner(surfaceHolder);
        puddingRunner.setPudding(pudding);
        puddingRunnerThread = new Thread(puddingRunner);
        puddingRunnerThread.start();
    }

    public void stop() {
        // stop monitoring the sensor
        if (isAccelerometerPresent) {
            sensorManager.unregisterListener(this);
        }
        boolean retry = true;
        puddingRunner.setStopFlag(true);
        while (retry) {
            try {
                puddingRunnerThread.join();
                retry = false;
            } catch (InterruptedException e) {
            }
        }
    }


    public boolean onTouchEvent(MotionEvent event) {
        synchronized (surfaceHolder) {
            // the below code modifies the pudding object, so we lock onto the surface holder
            // and make sure the rendering thread don't interfere with pudding obj
            processTouchEvent(event);
        }
        return true;
    }

    private void processTouchEvent(MotionEvent event) {
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
                // an additional touch ended
                int pointerIndex = event.getActionIndex();
                pudding.stopDragging(event.getPointerId(pointerIndex));
                break;
            }
            case (MotionEvent.ACTION_MOVE): {
                // a finger has moved across screen
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

    public void applyPrefs() {
        PuddingConfigurator.applyPrefs(pudding, prefs);
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

}
