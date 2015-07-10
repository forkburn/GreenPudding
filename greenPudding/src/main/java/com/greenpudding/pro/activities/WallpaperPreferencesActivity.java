package com.greenpudding.pro.activities;

import com.greenpudding.free.activities.PuddingPreferencesActivity;
import com.greenpudding.pro.services.GreenPuddingWallpaperService;

public class WallpaperPreferencesActivity extends PuddingPreferencesActivity {

	@Override
	protected void initSharedPreference() {
		// this preference is for the live wallpaper, so we store the it
		// separately
		getPreferenceManager().setSharedPreferencesName(GreenPuddingWallpaperService.SHARED_PREFS_NAME);
	}




}