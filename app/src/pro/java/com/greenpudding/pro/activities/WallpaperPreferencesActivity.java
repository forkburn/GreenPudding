package com.greenpudding.pro.activities;

import com.greenpudding.activities.PuddingPreferencesActivity;
import com.greenpudding.pro.services.GreenPuddingWallpaperService;

public class WallpaperPreferencesActivity extends PuddingPreferencesActivity {

    @Override
    protected void initSharedPreference() {
        // preference for the live wallpaper is store separately
        getPreferenceManager().setSharedPreferencesName(GreenPuddingWallpaperService.SHARED_PREFS_NAME);
    }


}