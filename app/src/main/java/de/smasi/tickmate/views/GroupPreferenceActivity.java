package de.smasi.tickmate.views;

import android.app.Activity;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;

import de.smasi.tickmate.database.TracksDataSource;
import de.smasi.tickmate.models.Group;

public class GroupPreferenceActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
 
		int group_id = getIntent().getExtras().getInt("group_id");

        GroupPreferenceFragment fragment = new GroupPreferenceFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("group_id", group_id);
        fragment.setArguments(bundle);
        getFragmentManager().beginTransaction().replace(android.R.id.content, fragment).commit();
    }
}