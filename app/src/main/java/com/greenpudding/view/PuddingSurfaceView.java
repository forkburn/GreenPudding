package com.greenpudding.view;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.greenpudding.model.Pudding;
import com.greenpudding.thread.PuddingRenderThread;

/**
 * A surface view that starts a separate thread to do the rendering when surface
 * is created, and stops the rendering thread when surface is destroyed
 */
public class PuddingSurfaceView extends SurfaceView implements SurfaceHolder.Callback {
    private Pudding pudding;

    private SurfaceHolder surfaceHolder;
    private PuddingRenderThread puddingRenderThread;
    private Thread puddingRendererThread;

    public PuddingSurfaceView(Context context) {
        super(context);
        init();
    }

    public PuddingSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        surfaceHolder = getHolder();
        surfaceHolder.addCallback(this);

    }

    public Pudding getPudding() {
        return pudding;
    }

    public void setPudding(Pudding mPudding) {
        this.pudding = mPudding;

    }

    @Override
    public void surfaceCreated(SurfaceHolder arg0) {
        startRendererThread();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder arg0) {
        stopRendererThread();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Rect boundingRect = new Rect(0, 0, width, height);
        pudding.setBoundingRect(boundingRect);
    }

    private void startRendererThread() {
        puddingRenderThread = new PuddingRenderThread(surfaceHolder);
        puddingRenderThread.setPudding(pudding);
        puddingRendererThread = new Thread(puddingRenderThread);
        puddingRendererThread.start();
    }

    private void stopRendererThread() {
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
    public boolean onTouchEvent(MotionEvent event) {

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
            case (MotionEvent.ACTION_POINTER_DOWN): {
                // an additional touch begins
                int pointerIndex = event.getActionIndex();
                pudding.startDragging(event.getX(pointerIndex), event.getY(pointerIndex), event.getPointerId(pointerIndex));
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
        return true;
    }

}
