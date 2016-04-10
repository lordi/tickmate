package de.smasi.tickmate.views;

import android.app.Activity;
import android.os.Bundle;

import de.smasi.tickmate.prefs.TrackPreferenceFragment;

public class TrackPreferenceActivity extends Activity {
		
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
 
		int track_id = getIntent().getExtras().getInt("track_id");

        TrackPreferenceFragment fragment = new TrackPreferenceFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("track_id", track_id);
        fragment.setArguments(bundle);
        getFragmentManager().beginTransaction().replace(android.R.id.content, fragment).commit();
    }
}