package com.greenpudding.pro.services;

import android.service.wallpaper.WallpaperService;

public class GreenPuddingWallpaperService extends WallpaperService {

	public static final String SHARED_PREFS_NAME = "greenPuddingSettings";
	private PuddingEngine engine;

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
		engine = new PuddingEngine(this);
		return engine;
	}
}
