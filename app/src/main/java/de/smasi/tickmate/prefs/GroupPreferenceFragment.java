package de.smasi.tickmate.prefs;


import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.MultiSelectListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.smasi.tickmate.R;
import de.smasi.tickmate.database.DataSource;
import de.smasi.tickmate.models.Group;
import de.smasi.tickmate.models.Track;

public class GroupPreferenceFragment extends PreferenceFragment implements
        OnSharedPreferenceChangeListener {

    private static String TAG = "GroupPreferenceFragment";
    private int group_id;
    private Group group;
    private EditTextPreference name;
    private EditTextPreference description;
    private MultiSelectListPreference mTracksPref;
    private static DataSource mDataSource = DataSource.getInstance();
    private boolean mOpenTrackList = false;


    public GroupPreferenceFragment() {
        super();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.group_preferences);

        group_id = getArguments().getInt("group_id");
        mOpenTrackList = getArguments().getBoolean("openTrackList");
        group = DataSource.getInstance().getGroup(group_id);
        loadGroup();
        // openTrackList indicates that this preference was created for the purpose of immediately
        //   opening up the track multi-select list
        if (mOpenTrackList) {
            PreferenceScreen screen = (PreferenceScreen) findPreference("group_preference_screen");
            int tracksPrefPosition = findPreference("tracks").getOrder();
            // make the tracks multi-select preference open up
            screen.onItemClick( null, null, tracksPrefPosition, 0 );
        }
    }

    // used by Preference.setSummary
    // Get the name of the Tracks that this Group is linked with
    private static CharSequence[] getTrackNamesForGroupAsCharSeq(Group group) {
        List<String> names = new ArrayList<>();
        for (Track track : mDataSource.getTracksForGroup(group.getId())) {
            names.add(track.getName());
        }
        return names.toArray(new CharSequence[names.size()]);
    }


    private static Set<String> getTrackIdsForGroupAsSet(int id) {
        Set<String> ids = new HashSet<>();
        for (Track t : mDataSource.getTracksForGroup(id)) {
            ids.add(Integer.toString(t.getId()));
        }
        //        Log.d(TAG, "getTrackIdsForGroupAsSet is returning: " + TextUtils.join(",", ids));
        return ids;
    }

    // Preference.setEntries requires a CharSequence[]
    private static CharSequence[] getAllTrackNamesAsCharSeq() {
        List<Track> tracks = mDataSource.getTracks();
        List<String> names = new ArrayList<>();
        for (Track t : tracks) {
            names.add(t.getName());
        }
        CharSequence[] cs = names.toArray(new CharSequence[names.size()]);
        //        Log.d(TAG, "getAllTrackNamesAsCharSeq is returning: " + TextUtils.join(",", cs));
        return cs;
    }

    // Preferences.setEntryValues requires a CharSequence[]
    private static CharSequence[] getAllTrackIdsAsCharSeq() {
        List<Track> tracks = mDataSource.getTracks();
        List<String> ids = new ArrayList<>();
        for (Track t : tracks) {
            ids.add(String.valueOf(t.getId()));
        }
        CharSequence[] cs = ids.toArray(new CharSequence[ids.size()]);
        //        Log.d(TAG, "getAllTrackIdsAsCharSeq is returning: " + TextUtils.join(",", cs));
        return cs;
    }

    private String getTrackNamesForSummary() {
        return TextUtils.join(", ", getTrackNamesForGroupAsCharSeq(group));
    }


    private void loadGroup() {
        // Consider adding more features here, such as those in Track and TrackPreferenceFragment
        name = (EditTextPreference) findPreference("name");
        name.setText(group.getName());
        name.setSummary(group.getName());

        description = (EditTextPreference) findPreference("description");
        description.setText(group.getDescription());
        description.setSummary(group.getDescription());

        mTracksPref = (MultiSelectListPreference) findPreference("tracks");
        mTracksPref.setValues(getTrackIdsForGroupAsSet(group.getId()));

        mTracksPref.setEntries(getAllTrackNamesAsCharSeq());
        mTracksPref.setEntryValues(getAllTrackIdsAsCharSeq());
        mTracksPref.setSummary(getTrackNamesForSummary());

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

            DataSource.getInstance().storeGroup(group);
        } else if (pref instanceof MultiSelectListPreference) {
            MultiSelectListPreference mp = (MultiSelectListPreference) pref;
//            Log.d(TAG, "MultiSelectListPreference changed, with trackIds: " + TextUtils.join(",", mp.getValues()));
            // Convert the Set returned by getValues into a List, as expected by setTrackIdsUsingStrings:
            List<Integer> trackIds = new ArrayList<>();
            for (String value : mp.getValues()) {
                trackIds.add(Integer.valueOf(value));
            }
            mDataSource.linkManyTracksOneGroup(trackIds, group.getId());
//            Log.d(TAG, "\tUser selected: " + TextUtils.join(",", trackIds));

            mTracksPref.setSummary(getTrackNamesForSummary());
//                    + "  \n" + TextUtils.join("\n", mDataSource.getGroups())); // Leaving here for future debugging, until tests are written
//            Log.d(TAG, "Confirm that the group IDs are correct: " + TextUtils.join(",", track.getGroupIdsAsSet()));
//            Log.d(TAG, "Confirm that the group NAMES are correct: " + TextUtils.join(",", track.getGroupNamesAsSet()));

        }
        mDataSource.storeGroup(group);

        // If this was launched simply to edit the track list, then exit at this point.
        if (mOpenTrackList) {
            getActivity().onBackPressed();
        }
    }



}