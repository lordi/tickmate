package de.smasi.tickmate.views;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.core.app.NavUtils;

import de.smasi.tickmate.R;
import de.smasi.tickmate.database.DataSource;
import de.smasi.tickmate.models.Track;

public class EditTracksActivity extends ListActivity {

	ArrayAdapter<Track> tracksAdapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//setContentView(R.layout.activity_edit_tracks);
		// Show the Up button in the action bar.
		setupActionBar();
				
		loadTracks();
		registerForContextMenu(this.getListView());
	}
	
	protected void loadTracks()  {
		DataSource ds = DataSource.getInstance();
		Track[] ms = new Track[0];
		ms = ds.getTracks().toArray(ms);
		tracksAdapter = new TrackListAdapter(this, ms);
		this.getListView().setAdapter(tracksAdapter);
	}

	/**
	 * Set up the {@link android.app.ActionBar}.
	 */
	private void setupActionBar() {
		getActionBar().setDisplayHomeAsUpEnabled(true);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.edit_tracks, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown. Use NavUtils to allow users
			// to navigate up one level in the application structure. For
			// more details, see the Navigation pattern on Android Design:
			//
			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
			//
			NavUtils.navigateUpFromSameTask(this);
			return true;
		case R.id.action_choose_track:
		case R.id.action_choose_track_menu:
			Intent intent = new Intent(this, ChooseTrackActivity.class);
		    startActivityForResult(intent, 1);			
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		editTrack((Track)tracksAdapter.getItem(position));
	}
	
	private void showTrack(Track t) {
		Intent intent = new Intent(this, ShowTrackActivity.class);
		intent.putExtra("track_id", t.getId());
	    startActivityForResult(intent, 1);				
	}
	
	private void editTrack(Track t) {
		Intent intent = new Intent(this, TrackPreferenceActivity.class);
		intent.putExtra("track_id", t.getId());
	    startActivityForResult(intent, 1);				
	}	

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (data != null && data.getExtras() != null) {
			//Log.v("Tickmate", "tracks sub activity returned." + resultCode + ":"+data.getExtras().getColorValue("insert_id"));
			
			int insert_id = data.getExtras().getInt("insert_id");
			DataSource ds = DataSource.getInstance();
			Track t = ds.getTrack(insert_id);
			if (t.isCustomTrack()) {
				editTrack(t);								
			}
		}
		
		loadTracks();
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
	    final AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
	    
		switch (item.getItemId()) {
		
		case R.id.edit_tracks_edit: {
				Track t = (Track)tracksAdapter.getItem((int)info.id);
				editTrack(t);
				return true;
			}
		
		case R.id.edit_tracks_moveup: {
				Track t = (Track)tracksAdapter.getItem((int)info.id);
				DataSource ds = DataSource.getInstance();
				ds.moveTrack(t, DataSource.DIRECTION_UP);
				loadTracks();
				return true;
			}	
		
		case R.id.edit_tracks_movedown: {
				Track t = (Track)tracksAdapter.getItem((int)info.id);
				DataSource ds = DataSource.getInstance();
				ds.moveTrack(t, DataSource.DIRECTION_DOWN);
				loadTracks();
				return true;
			}
		
		case R.id.edit_tracks_activate: {
				Track t = (Track)tracksAdapter.getItem((int)info.id);
				DataSource ds = DataSource.getInstance();
				t.setEnabled(true);
				ds.storeTrack(t);
				loadTracks();
				return true;
			}		
		
		case R.id.edit_tracks_deactivate: {
				Track t = (Track)tracksAdapter.getItem((int)info.id);
				DataSource ds = DataSource.getInstance();
				t.setEnabled(false);
				ds.storeTrack(t);
				loadTracks();
				return true;
			}
			
		case R.id.edit_tracks_delete: {
			new AlertDialog.Builder(this)
			.setTitle(R.string.alert_delete_track_title)
			.setMessage(R.string.alert_delete_track_message)
			.setIcon(android.R.drawable.ic_dialog_alert)
			.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

			    public void onClick(DialogInterface dialog, int whichButton) {
			    	Track t = (Track)tracksAdapter.getItem((int)info.id);
					DataSource ds = DataSource.getInstance();
					ds.deleteTrack(t);
					loadTracks();
			    }})
			 .setNegativeButton(android.R.string.no, null).show();
			
			return true;
		}
		}		
		return super.onContextItemSelected(item);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
	    AdapterContextMenuInfo info = (AdapterContextMenuInfo)menuInfo;

		super.onCreateContextMenu(menu, v, menuInfo);
	    MenuInflater inflater = getMenuInflater();
	    
		Track t = (Track)tracksAdapter.getItem((int)info.id);

	    inflater.inflate(R.menu.edit_tracks_context_menu, menu);
	    
	    menu.findItem(R.id.edit_tracks_deactivate).setVisible(t.isEnabled());
	    menu.findItem(R.id.edit_tracks_activate).setVisible(!t.isEnabled());

	}

}
