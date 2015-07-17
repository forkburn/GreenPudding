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

    private static final int MAX_PUDDING_RADIUS = 1000;
    private static final int MIN_PUDDING_RADIUS = 50;
    private static final int MAX_NUMBER_OF_NODES = 20;
    private static final int MIN_NUMBER_OF_NODES = 5;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initSharedPreference();

        //  populate the pref screen with xml content
        addPreferencesFromResource(R.xml.preferences);
//        initNumericEditTextPreference(R.string.pref_pudding_radius_key);
//        initNumericEditTextPreference(R.string.pref_number_of_nodes_key);
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

            // check that the input is within valid range
            if (inputRadius < MIN_PUDDING_RADIUS) {
                inputRadius = MIN_PUDDING_RADIUS;
            } else if (inputRadius > MAX_PUDDING_RADIUS) {
                inputRadius = MAX_PUDDING_RADIUS;
            }
            // save the pref
            pref.getEditor().putString(pref.getKey(), inputRadius.toString()).commit();

        } else if (pref.getKey().equals(getString(R.string.pref_number_of_nodes_key))) {
            // the number of nodes pref
            Integer numberOfNodes = Integer.parseInt(value.toString());

            // check that the input is within valid range
            if (numberOfNodes < MIN_NUMBER_OF_NODES) {
                numberOfNodes = MIN_NUMBER_OF_NODES;
            } else if (numberOfNodes > MAX_NUMBER_OF_NODES) {
                numberOfNodes = MAX_NUMBER_OF_NODES;
            }
            // save the pref
            pref.getEditor().putString(pref.getKey(), numberOfNodes.toString()).commit();

        }

        return false;
    }
}