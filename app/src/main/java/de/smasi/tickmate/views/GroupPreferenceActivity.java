package de.smasi.tickmate.views;

import android.app.Activity;
import android.os.Bundle;

import de.smasi.tickmate.prefs.GroupPreferenceFragment;

public class GroupPreferenceActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
 
		int group_id = getIntent().getExtras().getInt("group_id");
        boolean openTrackList = getIntent().getExtras().getBoolean("openTrackList", false);


        GroupPreferenceFragment fragment = new GroupPreferenceFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("group_id", group_id);
        bundle.putBoolean("openTrackList", openTrackList);

        fragment.setArguments(bundle);
        getFragmentManager().beginTransaction().replace(android.R.id.content, fragment).commit();
    }
}