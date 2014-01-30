package de.smasi.tickmate;

import java.util.Locale;

import android.app.ActionBar;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import de.smasi.tickmate.views.EditTracksActivity;

public class Tickmate extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.fragment_tickmate_ticks);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_edit_tracks:
				this.editTracks(null);
	            return true;
	        default:
	            return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.tickmate, menu);
		return true;
	}

	@Override
	public void onResume() {
		super.onResume();
		TickMatrix tm = (TickMatrix)findViewById(R.id.tickMatrix1);
		tm.buildView();
	}
	
	public void editTracks(View view) {
		Intent intent = new Intent(this, EditTracksActivity.class);
	    startActivity(intent);
	}
}
