package de.smasi.tickmate.views;

import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import de.smasi.tickmate.R;
import de.smasi.tickmate.database.TracksDataSource;
import de.smasi.tickmate.models.Tick;
import de.smasi.tickmate.models.Track;

public class ShowTrackActivity extends Activity {
	
	private Track track;
	private int tickCount;
	private List<Tick> ticks;
	
	/* Form fields */
	private TextView text_name;
	private ImageButton edit_icon;
	private TextView text_count;
	private TextView text_description;
	private ImageView image_icon;
	private SummaryGraph graph_weekdays;
	private SummaryGraph graph_weeks;
	private SummaryGraph graph_months;
	
	/* Graphs */
	private List<Integer> weekdaysData;
	private List<String> weekdaysKeys;
	private int weekdaysMaximum;

	private List<Integer> monthsData;
	private List<String> monthsKeys;
	private int monthsMaximum;

	private List<Integer> weeksData;
	private List<String> weeksKeys;
	private int weeksMaximum;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_show_track);
		
		TracksDataSource ds = new TracksDataSource(this);
		
		int track_id = getIntent().getExtras().getInt("track_id");
		
		ds.open();
		track = ds.getTrack(track_id);
		tickCount = ds.getTickCount(track_id);
		ticks = ds.getTicks(track_id);
		ds.close();
		loadTrack();		
		// Show the Up button in the action bar.
		setupActionBar();
	}

	/**
	 * Set up the {@link android.app.ActionBar}.
	 */
	private void setupActionBar() {

		getActionBar().setDisplayHomeAsUpEnabled(true);

	}
	
	private void retrieveGraphData() {
		this.weekdaysKeys = new LinkedList<String>();
		this.weekdaysKeys.add("MO");
		this.weekdaysKeys.add("DI");
		this.weekdaysKeys.add("MI");
		this.weekdaysKeys.add("DO");
		this.weekdaysKeys.add("FR");
		this.weekdaysKeys.add("SA");
		this.weekdaysKeys.add("SO");
		this.weekdaysData = new LinkedList<Integer>();
		this.weekdaysData.add(0);
		this.weekdaysData.add(0);
		this.weekdaysData.add(0);
		this.weekdaysData.add(0);		
		this.weekdaysData.add(0);
		this.weekdaysData.add(0);	
		this.weekdaysData.add(0);
		
		Calendar today = Calendar.getInstance();
		
		// Collect months
		Map<Integer, Integer> monthyear_to_index = new HashMap<Integer, Integer>(); 
		this.monthsKeys = new LinkedList<String>();
		this.monthsData = new LinkedList<Integer>();
		Calendar month = (Calendar) today.clone();
		month.clear(Calendar.HOUR);
		for (int i = 0; i < 7; i++) {
			this.monthsKeys.add(0, month.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.getDefault()));
			int index = month.get(Calendar.YEAR) + month.get(Calendar.MONTH) * 10000;
			monthyear_to_index.put(index, 6-i);
			month.add(Calendar.MONTH, -1);
			this.monthsData.add(0, 0);			
		}
		
		Map<Integer, Integer> weekyear_to_index = new HashMap<Integer, Integer>(); 
		this.weeksKeys = new LinkedList<String>();
		this.weeksData = new LinkedList<Integer>();
		Calendar week = (Calendar) today.clone();
		week.clear(Calendar.HOUR);
		for (int i = 0; i < 7; i++) {
			//month.getDisplayName(Calendar.WEEK_OF_YEAR, Calendar.SHORT, Locale.getDefault())
			this.weeksKeys.add(0, Integer.toString(week.get(Calendar.WEEK_OF_YEAR)));
			int index = week.get(Calendar.YEAR) + week.get(Calendar.WEEK_OF_YEAR) * 10000;
			weekyear_to_index.put(index, 6-i);
			week.add(Calendar.WEEK_OF_YEAR, -1);
			this.weeksData.add(0, 0);			
		}
		
		
		for (Tick tick : ticks) {
			int day_of_week = tick.date.get(Calendar.DAY_OF_WEEK) - 2;
			if (day_of_week < 0) day_of_week = 6;
			int newcount = this.weekdaysData.get(day_of_week)+1;
			if (newcount > this.weekdaysMaximum) {
				this.weekdaysMaximum = newcount;
			}
			this.weekdaysData.set(day_of_week, newcount);
			
			int monthyear = tick.date.get(Calendar.YEAR) + tick.date.get(Calendar.MONTH) * 10000;
			if (monthyear_to_index.containsKey(monthyear)) {
				int index = monthyear_to_index.get(monthyear);
				int newcount2 = this.monthsData.get(index)+1;
				this.monthsData.set(index, newcount2);
			}
			
			int weekyear = tick.date.get(Calendar.YEAR) + tick.date.get(Calendar.WEEK_OF_YEAR) * 10000;
			if (weekyear_to_index.containsKey(weekyear)) {
				int index = weekyear_to_index.get(weekyear);
				int newcount2 = this.weeksData.get(index)+1;
				this.weeksData.set(index, newcount2);
			}			
			
			//tick.date.get(Calendar.YEAR) tick.date.get(Calendar.MONTH);
			
		}
		
		
		this.monthsMaximum = 31;
		this.weeksMaximum = 7;
		
		
	}
	
	private void loadTrack() {
		text_name = (TextView) findViewById(R.id.textView_name);
		text_name.setText(track.getName());
		text_description = (TextView) findViewById(R.id.TextView_description);
		text_description.setText(track.getDescription());
		text_count = (TextView) findViewById(R.id.textView_count);
		text_count.setText(" " + Integer.toString(tickCount));
		
		retrieveGraphData();
		
		graph_weekdays = (SummaryGraph) findViewById(R.id.summaryGraph_weekdays);
		graph_weekdays.setData(this.weekdaysData, this.weekdaysKeys, this.weekdaysMaximum);
		graph_months = (SummaryGraph) findViewById(R.id.summaryGraph_months);
		graph_months.setData(this.monthsData, this.monthsKeys, this.monthsMaximum);
		graph_weeks = (SummaryGraph) findViewById(R.id.summaryGraph_weeks);
		graph_weeks.setData(this.weeksData, this.weeksKeys, this.weeksMaximum);

		image_icon = (ImageView) findViewById(R.id.image_icon);
		image_icon.setImageResource(track.getIconId(this));
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.show_track, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_edit:
		case R.id.action_edit_menu:
			Intent intent = new Intent(this, EditTrackActivity.class);
			intent.putExtra("track_id", track.getId());
			startActivityForResult(intent, 1);				
			return true;
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
		}
		return super.onOptionsItemSelected(item);
	}

}
