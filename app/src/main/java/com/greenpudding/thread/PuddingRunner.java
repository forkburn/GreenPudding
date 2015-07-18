package com.greenpudding.thread;

import android.graphics.Canvas;
import android.view.SurfaceHolder;

import com.greenpudding.model.PuddingModel;

/**
 * A thread that repeatedly gets the canvas and ask the pudding to render on the
 * canvas.
 * 
 */
public class PuddingRunner implements Runnable {

	private SurfaceHolder surfaceHolder;
	private PuddingModel pudding;

	// flag indicating whether thread should continue to run
	private boolean stopFlag = false;

	public PuddingRunner(SurfaceHolder holder) {
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

	public PuddingModel getPudding() {
		return pudding;
	}

	public void setPudding(PuddingModel pudding) {
		this.pudding = pudding;
	}

	public void setStopFlag(boolean stopFlag) {
		this.stopFlag = stopFlag;
	}

	public boolean getStopFlag() {
		return stopFlag;
	}

}