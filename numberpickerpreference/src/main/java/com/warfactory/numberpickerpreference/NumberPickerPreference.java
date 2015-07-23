package com.warfactory.numberpickerpreference;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.NumberPicker;

public class NumberPickerPreference extends DialogPreference {

    private static int DEFAULT_VAL = 0;
    NumberPicker picker;
    int pickerMinVal;
    int pickerMaxVal;
    int currentValue;

    public NumberPickerPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setDialogLayoutResource(R.layout.number_picker_pref);
        parseCustomAttrs(attrs);
    }

    private void parseCustomAttrs(AttributeSet attrs) {
        TypedArray array = getContext().getTheme().obtainStyledAttributes(
                attrs, R.styleable.NumberPickerPref, 0, 0);
        try {
            pickerMinVal = array.getInteger(R.styleable.NumberPickerPref_minValue, 0);
            pickerMaxVal = array.getInteger(R.styleable.NumberPickerPref_maxValue, 1000);
        } finally {
            array.recycle();
        }
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        this.picker = (NumberPicker) view.findViewById(R.id.pref_num_picker);
        picker.setMaxValue(pickerMaxVal);
        picker.setMinValue(pickerMinVal);
        picker.setValue(currentValue);
        // do not allow wrap, otherwise input a value smaller than minVal will result in bug
        picker.setWrapSelectorWheel(false);
    }


    @Override
    protected void onDialogClosed(boolean positiveResult) {
        // When the user selects "OK", persist the new value
        if (positiveResult) {
            // clear focus on the picker so that getValue returns the updated input value
            picker.clearFocus();
            currentValue = picker.getValue();
            persistInt(currentValue);
        }
    }

    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
        if (restorePersistedValue) {
            this.currentValue = getPersistedInt(DEFAULT_VAL);
        } else {
            this.currentValue = (Integer) defaultValue;
            persistInt(currentValue);
        }
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getInt(index, DEFAULT_VAL);
    }
}