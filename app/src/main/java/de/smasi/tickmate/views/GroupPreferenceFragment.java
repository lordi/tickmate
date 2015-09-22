package de.smasi.tickmate.views;


import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;

import de.smasi.tickmate.R;
import de.smasi.tickmate.database.TracksDataSource;
import de.smasi.tickmate.models.Group;

public class GroupPreferenceFragment extends PreferenceFragment implements
        OnSharedPreferenceChangeListener {

    private static String TAG = "GroupPreferenceFragment";
    private int group_id;
    private Group group;
    private EditTextPreference name;
    private EditTextPreference description;

    public GroupPreferenceFragment() {
        super();
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.group_preferences);

        group_id = getArguments().getInt("group_id");
        group = TracksDataSource.getInstance().getGroup(group_id);
        loadTrack();
    }

    private void loadTrack() {
        // Consider adding more features here, such as those in Track and TrackPreferenceFragment
        name = (EditTextPreference) findPreference("name");
        name.setText(group.getName());
        name.setSummary(group.getName());

        description = (EditTextPreference) findPreference("description");
        description.setText(group.getDescription());
        description.setSummary(group.getDescription());
    }

    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
                                          String key) {
        Preference pref = findPreference(key);

        if (pref instanceof EditTextPreference) {
            EditTextPreference etp = (EditTextPreference) pref;
            if (pref.equals(name)) {
                group.setName(name.getText());
            }
            if (pref.equals(description)) {
                group.setDescription(description.getText());
            }
            pref.setSummary(etp.getText());

            TracksDataSource.getInstance().storeGroup(group);
        }
    }
}