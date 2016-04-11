package de.smasi.tickmate.prefs;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import de.smasi.tickmate.R;
import de.smasi.tickmate.database.DataSource;
import de.smasi.tickmate.models.Track;

public class TrackPreferenceFragment extends PreferenceFragment implements
OnSharedPreferenceChangeListener  {

    private static String TAG = "TrackPreferenceFragment";
	private int track_id;
    private Track track;
    private EditTextPreference name;

    private EditTextPreference description;
    private CheckBoxPreference enabled;
    private CheckBoxPreference multiple_entries_enabled;
	private IconPreference icon;
    private GroupListPreference mGroupsPref;
    private static DataSource mDataSource = DataSource.getInstance();
    private TickColorPreference mTickColorPreference;

    public TrackPreferenceFragment() {
        super();
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.track_preferences);


        track_id = getArguments().getInt("track_id");
        Log.d(TAG, "onCreate given track id " + track_id);
        track = mDataSource.getTrack(track_id);
        Log.d(TAG, " retrieved track with id = " + track.getId());
		loadTrack();
    }

	private void loadTrack() {
        Log.d(TAG, "Loading track #" + track.getId());
        icon = (IconPreference) findPreference("icon");
        icon.setText(track.getIcon());

        name = (EditTextPreference) findPreference("name");
        name.setText(track.getName());
        name.setSummary(track.getName());

        description = (EditTextPreference) findPreference("description");
        description.setText(track.getDescription());
        description.setSummary(track.getDescription());

        enabled = (CheckBoxPreference) findPreference("enabled");
        enabled.setChecked(track.isEnabled());

        multiple_entries_enabled = (CheckBoxPreference) findPreference("multiple_entries_enabled");
        multiple_entries_enabled.setChecked(track.multipleEntriesEnabled());

        mGroupsPref = (GroupListPreference) findPreference("groups");
        mGroupsPref.setTrack(track);
        mGroupsPref.populate();

        mTickColorPreference = (TickColorPreference) findPreference("tick_button_color");
        mTickColorPreference.setColor(track.getTickColor());
    }

    public void onResume() {
        super.onResume();

        // Repopulate GroupListPreference because we might resume from Edit groups activity
        mGroupsPref = (GroupListPreference) findPreference("groups");
        mGroupsPref.setTrack(track);
        mGroupsPref.populate();

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
        Log.v(TAG, "onSharedPreferenceChanged -- " + pref.getTitle());

        if (pref instanceof IconPreference) {
            if (pref.equals(icon)) {
        		track.setIcon(icon.getText());
                //icon.setSummary(track.getIcon());
        	}
        }
        if (pref instanceof TickColorPreference) {  // TickColorPreference instances are also EditTextPreference
            track.getTickColor().setColorValue(mTickColorPreference.getColorValue());
        } else if (pref instanceof EditTextPreference) {
            EditTextPreference etp = (EditTextPreference) pref;
            if (pref.equals(name)) {
            	track.setName(name.getText());
            }
            if (pref.equals(description)) {
            	track.setDescription(description.getText());
            }
            pref.setSummary(etp.getText());
        }
        else if (pref instanceof CheckBoxPreference) {
        	if (pref.equals(enabled)) {
        		track.setEnabled(enabled.isChecked());
        	}
        	if (pref.equals(multiple_entries_enabled)) {
                track.setMultipleEntriesEnabled(multiple_entries_enabled.isChecked());
            }
        } else if (pref instanceof GroupListPreference) {
            if (pref.equals(mGroupsPref)) {
                // Convert the Set returned by getValues into a List, as expected by setGroupIdsUsingStrings:
                List<Integer> groupIds = new ArrayList<>();
                for (String value : mGroupsPref.getValues()) {
                    groupIds.add(Integer.valueOf(value));
                }
                mDataSource.linkOneTrackManyGroups(track.getId(), groupIds);
                mGroupsPref.populate();
            }
        }
        mDataSource.storeTrack(track);
    }
}
