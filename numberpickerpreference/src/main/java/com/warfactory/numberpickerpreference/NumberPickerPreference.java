package com.warfactory.numberpickerpreference;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.NumberPicker;

public class NumberPickerPreference extends DialogPreference {

    NumberPicker picker;
    int pickerMinVal;
    int pickerMaxVal;
    Integer initialValue;

    public NumberPickerPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setDialogLayoutResource(R.layout.number_picker_pref);
        readCustomAttrs(attrs);
    }

    private void readCustomAttrs(AttributeSet attrs) {
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
        if (this.initialValue != null) picker.setValue(initialValue);
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        super.onClick(dialog, which);
        if (which == DialogInterface.BUTTON_POSITIVE) {
            // make the picker save user input value
            picker.clearFocus();
            this.initialValue = picker.getValue();
            persistInt(initialValue);
            callChangeListener(initialValue);
        }
    }

    @Override
    protected void onSetInitialValue(boolean restorePersistedValue,
                                     Object defaultValue) {
        int def = (defaultValue instanceof Number) ? (Integer) defaultValue
                : (defaultValue != null) ? Integer.parseInt(defaultValue.toString()) : 1;
        if (restorePersistedValue) {
            this.initialValue = getPersistedInt(def);
        } else this.initialValue = (Integer) defaultValue;
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getInt(index, 1);
    }
}