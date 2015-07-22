package com.warfactory.numberpickerpreference;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.NumberPicker;

public class NumberPickerPreference extends DialogPreference {

    private static int DEFAULT_VAL = 0;
    NumberPicker picker;
    int pickerMinVal;
    int pickerMaxVal;
    int initialValue;

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
        picker.setValue(initialValue);
    }


    @Override
    protected void onDialogClosed(boolean positiveResult) {
        // When the user selects "OK", persist the new value
        if (positiveResult) {
            // clear focus on the picker so that getValue returns the updated input value
            picker.clearFocus();
            persistInt(picker.getValue());
        }
    }

    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
        if (restorePersistedValue) {
            this.initialValue = getPersistedInt(DEFAULT_VAL);
        } else {
            this.initialValue = (Integer) defaultValue;
            persistInt(initialValue);
        }
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getInt(index, DEFAULT_VAL);
    }
}