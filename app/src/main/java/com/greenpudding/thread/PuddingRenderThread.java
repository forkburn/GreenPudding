package com.greenpudding.thread;

import android.graphics.Canvas;
import android.view.SurfaceHolder;

import com.greenpudding.model.Pudding;

/**
 * A thread that repeatedly gets the canvas and ask the pudding to render on the
 * canvas.
 * 
 */
public class PuddingRenderThread implements Runnable {

	private SurfaceHolder surfaceHolder;
	private Pudding pudding;

	// flag indicating whether thread should continue to run
	private boolean stopFlag = false;

	public PuddingRenderThread(SurfaceHolder holder) {
		surfaceHolder = holder;
	}

	@Override
	public void run() {
		while (!stopFlag) {
			Canvas canvas = null;
			try {
				canvas = surfaceHolder.lockCanvas();
				if (canvas != null) {
					synchronized (surfaceHolder) {
						pudding.updatePhysics();
						pudding.render(canvas);
					}
				}
			} finally {
				if (canvas != null) {
					surfaceHolder.unlockCanvasAndPost(canvas);
				}
			}
		}
	}

	public Pudding getPudding() {
		return pudding;
	}

	public void setPudding(Pudding pudding) {
		this.pudding = pudding;
	}

	public void setStopFlag(boolean stopFlag) {
		this.stopFlag = stopFlag;
	}

	public boolean getStopFlag() {
		return stopFlag;
	}

}