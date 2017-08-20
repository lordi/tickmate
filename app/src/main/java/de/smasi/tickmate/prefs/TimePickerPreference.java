package de.smasi.tickmate.prefs;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.preference.PreferenceManager;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TimeUtils;
import android.view.View;
import android.widget.TimePicker;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

public class TimePickerPreference extends DialogPreference {
    private int lastHour = 0;
    private int lastMinute = 0;
    private static final String TAG = "Tickmate";
    private TimePicker picker = null;

    public static int getHour(String time) {
        String[] pieces = time.split(":");

        return Integer.parseInt(pieces[0]);
    }

    public static int getMinute(String time) {
        String[] pieces = time.split(":");

        return Integer.parseInt(pieces[1]);
    }

    public TimePickerPreference(Context ctxt, AttributeSet attrs) {
        super(ctxt, attrs);

        setPositiveButtonText("Set");
        setNegativeButtonText("Cancel");
    }

    @Override
    protected View onCreateDialogView() {
        picker = new TimePicker(getContext());

        // Set the picker to the system default 24 hour format setting
        picker.setIs24HourView(new DateFormat().is24HourFormat(this.getContext()));
        return picker;
    }

    @Override
    protected void onBindDialogView(View v) {
        super.onBindDialogView(v);

        picker.setCurrentHour(lastHour);
        picker.setCurrentMinute(lastMinute);
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);

        if (positiveResult) {
            lastHour = picker.getCurrentHour();
            lastMinute = picker.getCurrentMinute();

            java.text.DateFormat df = java.text.DateFormat.getTimeInstance(java.text.DateFormat.SHORT);

            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.HOUR_OF_DAY, lastHour);
            cal.set(Calendar.MINUTE, lastMinute);
            cal.set(Calendar.SECOND, 0);

            String time = df.format(cal.getTime());

            updateSummary();

            Log.d(TAG, "Selected time: " + time);

            if (callChangeListener(time)) {
                persistString(time);
            }
        }
    }


    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getString(index);
    }

    private void updateSummary() {
        java.text.DateFormat df = java.text.DateFormat.getTimeInstance(java.text.DateFormat.SHORT);

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, lastHour);
        cal.set(Calendar.MINUTE, lastMinute);
        cal.set(Calendar.SECOND, 0);

        String time = df.format(cal.getTime());
        setSummary("Remind me at " + time);
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
        String time = null;

        if (restoreValue) {
            if (defaultValue == null) {
                time = getPersistedString("00:00");
            }
            else {
                time = getPersistedString(defaultValue.toString());
            }
        }
        else {
            time = defaultValue.toString();
        }

        Calendar cal = Calendar.getInstance();
        java.text.DateFormat timeFmt = java.text.DateFormat.getTimeInstance(java.text.DateFormat.SHORT);

        try {
            cal.setTime(timeFmt.parse(time));
        } catch (ParseException e) {
            Log.w(TAG, "Error parsing time: " + time);
        }

        lastHour = cal.get(Calendar.HOUR_OF_DAY);
        lastMinute = cal.get(Calendar.MINUTE);

        updateSummary();
    }
}