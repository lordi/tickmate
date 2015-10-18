package de.smasi.tickmate.views;

import android.app.ListActivity;
import android.content.Intent;
import android.content.res.XmlResourceParser;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import de.smasi.tickmate.R;
import de.smasi.tickmate.database.DataSource;
import de.smasi.tickmate.models.Track;

public class ChooseTrackActivity extends ListActivity {
	
	ArrayAdapter<Track> tracks;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//setContentView(R.layout.activity_edit_tracks);
		// Show the Up button in the action bar.
		setupActionBar();
		
		XmlResourceParser xrp = getResources().getXml(getResources().getIdentifier("tracks", "xml", getPackageName()));
		
        int eventType;
        List<Track> listOfTracks = new LinkedList<Track>();

		try {
			eventType = xrp.getEventType();
	        while (eventType != XmlPullParser.END_DOCUMENT) {
	         if(eventType == XmlPullParser.START_DOCUMENT) {
	             Log.v("XML", "Start document");
	         } else if(eventType == XmlPullParser.START_TAG) {
	        	 Log.v("XML", "Start tag "+xrp.getName());
	        	 if (xrp.getName().equals("track")) {
	        		 String name = xrp.getAttributeValue(null, "name");
	        		 String description = xrp.getAttributeValue(null, "description");
	        		 if (name == null || description == null) {
	        			 Log.w("Tickmate", "tracks.xml. Ignoring entry.");
	        		 }
	        		 Track t = new Track(name, description);
	        		 t.setIcon(getResources().getResourceEntryName(xrp.getAttributeResourceValue(null, "icon", R.drawable.glyphicons_000_glass_white)));
	        		 listOfTracks.add(t);
	        	 }
	        	 else if (xrp.getName().equals("section")) {
	        		 Track t = new Track("--- " + xrp.getAttributeValue(null, "name"));
	        		 listOfTracks.add(t);
	        	 }
	         } else if(eventType == XmlPullParser.END_TAG) {
	        	 Log.v("XML", "End tag "+xrp.getName());
	         } else if(eventType == XmlPullParser.TEXT) {
	        	 Log.v("XML", "Text "+xrp.getText());
	         }
	         eventType = xrp.next();
	        }
		} catch (XmlPullParserException e) {
			// TODO Auto-generated catch block
			Log.v("XML", "exception");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			Log.v("XML", "exception");
		}

		Log.v("XML", listOfTracks.size() + " tracks loaded");
				
		Track[] ms = new Track[listOfTracks.size()];
		listOfTracks.toArray(ms);
		tracks = new TrackListAdapter(this, ms);
		this.getListView().setAdapter(tracks);
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		
		Track t = (Track) getListView().getAdapter().getItem(position);
		
		if (!t.isSectionHeader()) {
			DataSource ds = DataSource.getInstance();
			ds.storeTrack(t);
			Intent data = new Intent();
			data.putExtra("insert_id", t.getId());
			setResult(RESULT_OK, data);     
			finish();
		}
	}

	/**
	 * Set up the {@link android.app.ActionBar}.
	 */
	private void setupActionBar() {

	}


}
