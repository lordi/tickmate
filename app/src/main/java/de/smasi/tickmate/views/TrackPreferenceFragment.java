package de.smasi.tickmate.views;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.MultiSelectListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.smasi.tickmate.R;
import de.smasi.tickmate.database.TracksDataSource;
import de.smasi.tickmate.models.Group;
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
    private MultiSelectListPreference mGroupsPref;
    private static TracksDataSource mDataSource = TracksDataSource.getInstance();

    public TrackPreferenceFragment() {
        super();
    }

    // used by Preference.setSummary
	// Get the name of the Groups that this track belongs to
	private static CharSequence[] getGroupNamesForTrackAsCharSeq(Track track) {
		List<String> names = new ArrayList<>();
		for (Group group : mDataSource.getGroupsForTrack(track.getId())){
			names.add(group.getName());
		}
		return names.toArray(new CharSequence[names.size()]);
	}

    private static  Set<String> getGroupIdsForTrackAsSet(int id) {
        Set<String> ids = new HashSet<>();
        for (Group g : mDataSource.getGroupsForTrack(id)) {
            ids.add(Integer.toString(g.getId()));
        }
        //        Log.d(TAG, "getGroupIdsForTrackAsSet is returning: " + TextUtils.join(",", ids));
        return ids;
    }

    // Preference.setEntries requires a CharSequence[]
    private static CharSequence[] getAllGroupNamesAsCharSeq() {
        List<Group> groups = mDataSource.getGroups();
        List<String> names = new ArrayList<>();
        for (Group g : groups) {
            names.add(g.getName());
        }
        CharSequence[] cs = names.toArray(new CharSequence[names.size()]);
        //        Log.d(TAG, "getAllGroupNamesAsCharSeq is returning: " + TextUtils.join(",", cs));
        return cs;
    }

    // Preferences.setEntryValues requires a CharSequence[]
    private static CharSequence[] getAllGroupIdsAsCharSeq() {
        List<Group> groups = mDataSource.getGroups();
        List<String> ids = new ArrayList<>();
        for (Group g : groups) {
            ids.add(String.valueOf(g.getId()));
        }
        CharSequence[] cs = ids.toArray(new CharSequence[ids.size()]);
        //        Log.d(TAG, "getAllGroupIdsAsCharSeq is returning: " + TextUtils.join(",", cs));
        return cs;
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

        // TODO (Done? js) Rewrite the methods and method names used for this stanza:
        // AVP:TracksDataSource? Utility class? Maybe have 3 methods in TPF which sensibly massage
        //      data returned by TDS methods.
        // js - I've refactored so all of these methods are now local to TPF, are private static,
        //      and call TDS directly to get their data.  (I'm not opposed to making a separate
        //      utility class just for these methods if you like, but this seems clean enough for
        //      today; though I'm still not a fan of these extra long names
        mGroupsPref = (MultiSelectListPreference) findPreference("groups");
        mGroupsPref.setValues(getGroupIdsForTrackAsSet(track.getId()));

        mGroupsPref.setEntries(getAllGroupNamesAsCharSeq());
        mGroupsPref.setEntryValues(getAllGroupIdsAsCharSeq());
        mGroupsPref.setSummary(getGroupNamesForSummary());
//        Log.d(TAG, "setValues (0) track.getGroupIdsAsSet() - " + TextUtils.join("; ", track.getGroupIdsAsSet()));
//        Log.d(TAG, "setEntries (1) getAllGroupNamesAsCharSeq()- " + getAllGroupNamesAsCharSeq() + " -- " + TextUtils.join("; ", getAllGroupNamesAsCharSeq()));
//        Log.d(TAG, "setEntryValues (2) getAllGroupIdsAsCharSeq()- " + getAllGroupIdsAsCharSeq() + " -- " + TextUtils.join("; ", getAllGroupIdsAsCharSeq()));
//        Log.d(TAG, "setSummary (3)with " + track.getGroupNamesAsCharSeq() + " -- " + TextUtils.join("; ", track.getGroupNamesAsCharSeq()));
    }

    // Concatenates the names of the groups associated with this track into a readable comma separated list.
    // Consider internationalizing this better - how are lists done in other languages? Joined with other characters?
    private String getGroupNamesForSummary() {
        return TextUtils.join(", ", getGroupNamesForTrackAsCharSeq(track));
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
//        Log.v(TAG, "onSharedPreferenceChanged -- " + pref.getTitle());

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
        } else if (pref instanceof MultiSelectListPreference) {
            MultiSelectListPreference mp = (MultiSelectListPreference) pref;
//            Log.d(TAG, "MultiSelectListPreference changed, with groupIds: " + TextUtils.join(",", mp.getValues()));

            // Convert the Set returned by getValues into a List, as expected by setGroupIdsUsingStrings:
            List<Integer> groupIds = new ArrayList<>();
            for (String value : mp.getValues()) {
                groupIds.add(Integer.valueOf(value));
            }

            mDataSource.linkOneTrackManyGroups(track.getId(), groupIds);
//            Log.d(TAG, "\tUser selected: " + TextUtils.join(",", groupIds));

            mGroupsPref.setSummary(getGroupNamesForSummary());
//                    + "  \n" + TextUtils.join("\n", mDataSource.getGroups())); // Leaving here for future debugging, until tests are written
//            Log.d(TAG, "Confirm that the group IDs are correct: " + TextUtils.join(",", track.getGroupIdsAsSet()));
//            Log.d(TAG, "Confirm that the group NAMES are correct: " + TextUtils.join(",", track.getGroupNamesAsSet()));

        }
		mDataSource.storeTrack(track);
    }
}
