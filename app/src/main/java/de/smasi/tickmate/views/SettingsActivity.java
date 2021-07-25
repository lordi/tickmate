// This file was derived from one using a fragment-based approach to settings.

package de.smasi.tickmate.views;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;

import android.widget.Toast;
import de.smasi.tickmate.R;
import de.smasi.tickmate.notifications.TickmateNotificationBroadcastReceiver;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class SettingsActivity extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
    private static final String TAG = "Tickmate";

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        setupSimplePreferencesScreen();
    }

    /**
     * Shows the simplified settings UI if the device configuration if the
     * device configuration dictates that a simplified, single-pane UI should be
     * shown.
     */
    private void setupSimplePreferencesScreen() {
        addPreferencesFromResource(R.xml.pref_general);
        bindPreferenceSummaryToValue(findPreference("active-date-key"));
        bindPreferenceSummaryToValue(findPreference("long-click-key"));
        bindPreferenceSummaryToValue(findPreference("trend-range-key"));
    }


    /**
     * A preference value change listener that updates the preference's summary
     * to reflect its new value.
     */
    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();

            if (preference instanceof ListPreference) {
                // For list preferences, look up the correct display value in
                // the preference's 'entries' list.
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);

                // Set the summary to reflect the new value.
                preference
                    .setSummary(index >= 0 ? listPreference.getEntries()[index]
                        : null);


            } else {
                // For all other preferences, set the summary to the value's
                // simple string representation.
                preference.setSummary(stringValue);
            }
            return true;
        }
    };

    /**
     * Binds a preference's summary to its value. More specifically, when the
     * preference's value is changed, its summary (line of text below the
     * preference title) is updated to reflect the value. The summary is also
     * immediately updated upon calling this method. The exact display format is
     * dependent on the type of preference.
     *
     * @see #sBindPreferenceSummaryToValueListener
     */
    private static void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference
            .setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        // Trigger the listener immediately with the preference's
        // current value.
        sBindPreferenceSummaryToValueListener.onPreferenceChange(
            preference,
            PreferenceManager.getDefaultSharedPreferences(
                preference.getContext()).getString(preference.getKey(),
                ""));
    }


    @Override
    public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
        Log.d(TAG, "Settings key changed: " + key);
        if (key.equals("date_format")) {
            Locale locale = Locale.getDefault();
            String dateFormatString = PreferenceManager.getDefaultSharedPreferences(this).
                getString("date_format", "");
            if(!dateFormatString.isEmpty()) {
                try{
                    new SimpleDateFormat(dateFormatString, locale);
                } catch (IllegalArgumentException e) {
                    String javaErrorMessage = e.getMessage();
                    Toast.makeText(this, "Date Format: " + javaErrorMessage, Toast.LENGTH_SHORT).show();
                }
            }
        }
        if (key.equals("notification-enabled") || key.equals("notification-time"))
            TickmateNotificationBroadcastReceiver.updateAlarm(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Update Notification alarm whenever the relevant preferences change
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.registerOnSharedPreferenceChangeListener(this);
        Log.d(TAG, "Installed listener");
    }

    @Override
    public void onPause() {
        // Update Notification alarm whenever the relevant preferences change
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.unregisterOnSharedPreferenceChangeListener(this);
        Log.d(TAG, "Deinstalled listener");
        super.onPause();
    }
}
