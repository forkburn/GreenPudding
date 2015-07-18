package com.greenpudding.view;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.greenpudding.PuddingFacade;

/**
 * A surface view that starts a separate thread to do the rendering when surface
 * is created, and stops the rendering thread when surface is destroyed
 */
public class PuddingSurfaceView extends SurfaceView implements SurfaceHolder.Callback {
    private SurfaceHolder surfaceHolder;
//    private PuddingRunner puddingRunner;
//    private Thread puddingRunnerThread;
    private PuddingFacade puddingFacade;

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


    public void setPuddingFacade(PuddingFacade puddingFacade) {
        this.puddingFacade = puddingFacade;
        this.puddingFacade.setSurfaceHolder(surfaceHolder);

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
        puddingFacade.setBoundingRect(new Rect(0, 0, width, height));
    }

    private void startRendererThread() {
        puddingFacade.start();
    }

    private void stopRendererThread() {
        puddingFacade.stop();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return puddingFacade.onTouchEvent(event);
    }



}
