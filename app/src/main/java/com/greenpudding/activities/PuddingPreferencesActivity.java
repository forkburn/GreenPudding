package com.greenpudding.activities;

import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.text.method.DigitsKeyListener;

import com.greenpudding.R;


public class PuddingPreferencesActivity extends PreferenceActivity implements OnPreferenceClickListener,
        OnPreferenceChangeListener {


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

    private void initNumericEditTextPreference(int resId) {
        EditTextPreference textEditPref = (EditTextPreference) findPreference(getString(resId));
        // check if input is numeric when user updates the text
        textEditPref.getEditText().setKeyListener(DigitsKeyListener.getInstance());
        textEditPref.setOnPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceClick(Preference pref) {
        // we don't handle the click here
        return false;
    }

    @Override
    public boolean onPreferenceChange(Preference pref, Object value) {

        // check which pref has changed
        if (pref.getKey().equals(getString(R.string.pref_pudding_radius_key))) {
            // the radius pref
            Integer inputRadius = Integer.parseInt(value.toString());

            // save the pref
            pref.getEditor().putString(pref.getKey(), inputRadius.toString()).commit();

        } else if (pref.getKey().equals(getString(R.string.pref_number_of_nodes_key))) {
            // the number of nodes pref
            Integer numberOfNodes = Integer.parseInt(value.toString());

            // save the pref
            pref.getEditor().putString(pref.getKey(), numberOfNodes.toString()).commit();

        }

        return false;
    }
}