package com.greenpudding.activities;

import android.os.Bundle;
import android.preference.PreferenceActivity;

import com.greenpudding.R;


public class PuddingPreferencesActivity extends PreferenceActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initSharedPreference();

        //  populate the pref screen with xml content
        addPreferencesFromResource(R.xml.preferences);
    }

    protected void initSharedPreference() {
        // to be overidden
    }

}