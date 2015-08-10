package de.smasi.tickmate.views;

import de.smasi.tickmate.R;
import de.smasi.tickmate.database.TracksDataSource;
import de.smasi.tickmate.models.Track;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.text.TextPaint;

public class TrackPreferenceFragment extends PreferenceFragment implements
OnSharedPreferenceChangeListener  {

	private int track_id;
    private Track track;
    private EditTextPreference name;
    private EditTextPreference description;
    private CheckBoxPreference enabled;
    private CheckBoxPreference multiple_entries_enabled;
	private IconPreference icon;

    public TrackPreferenceFragment() {
        super();
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.track_preferences);

        TracksDataSource ds = new TracksDataSource(this.getActivity());

        track_id = getArguments().getInt("track_id");
		ds.open();
		track = ds.getTrack(track_id);
		loadTrack();
		ds.close();
    }

	private void loadTrack() {
        icon = (IconPreference) findPreference("icon");
        icon.setText(track.getIcon());

        name = (EditTextPreference) findPreference("name");
        name.setText(track.getName());
        name.setSummary(track.getName());

		description = (EditTextPreference) findPreference("description");
        description.setText(track.getDescription());
        description.setSummary(track.getDescription());

		enabled = (CheckBoxPreference ) findPreference("enabled");
        enabled.setChecked(track.isEnabled());

		multiple_entries_enabled = (CheckBoxPreference ) findPreference("multiple_entries_enabled");
		multiple_entries_enabled.setChecked(track.multipleEntriesEnabled());
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
        if (pref instanceof IconPreference) {
        	if (pref.equals(icon)) {
        		track.setIcon(icon.getText());
                //icon.setSummary(track.getIcon());
        	}
        }            
        else if (pref instanceof EditTextPreference) {
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
        }    
        TracksDataSource ds = new TracksDataSource(this.getActivity());

		ds.open();
		ds.storeTrack(track);
		ds.close();        
    }
}
