package de.smasi.tickmate.views;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import de.smasi.tickmate.R;
import de.smasi.tickmatedata.database.DataSource;
import de.smasi.tickmatedata.models.Tick;
import de.smasi.tickmatedata.models.Track;
import de.smasi.tickmate.widgets.SummaryGraph;
import de.smasi.tickmate.widgets.SummaryNumber;

public class ShowTrackActivity extends Activity {

	private Track track;
    DataSource ds;
	
	/* Statistics */
	private int tickCount;
	private List<Tick> ticks;
	private Calendar firstTickDate;
	private Calendar lastTickDate;	
	private Calendar today;
	
	/* Form fields */
	private TextView text_name;
	private TextView text_description;
	private ImageView image_icon;
	private SummaryGraph graph_weekdays;
	private SummaryGraph graph_weeks;
	private SummaryGraph graph_months;
	private SummaryGraph graph_quarters;
	private SummaryGraph graph_years;

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
	
	private List<Integer> quarterData;
	private List<String> quarterKeys;
	private int quarterMaximum;

	private List<Integer> yearsData;
	private List<String> yearsKeys;
	private int yearsMaximum;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_show_track);
		
		this.ds = DataSource.getInstance();
		Bundle extras = getIntent().getExtras();
		if (extras == null) {
			finish();
			return;
		}
		int track_id = extras.getInt("track_id");
		
		loadTrack(track_id);
				
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
		Locale locale = Locale.getDefault();
		
		// Collect week days
		this.weekdaysKeys = new LinkedList<String>();
		Calendar day = (Calendar) today.clone();
		day.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
		
		for (int i = 0; i < 7; i++) {
			this.weekdaysKeys.add(day.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, locale).toUpperCase(locale));
			day.add(Calendar.DATE, 1);
		}		
		this.weekdaysData = new LinkedList<Integer>();
		this.weekdaysData.add(0);
		this.weekdaysData.add(0);
		this.weekdaysData.add(0);
		this.weekdaysData.add(0);		
		this.weekdaysData.add(0);
		this.weekdaysData.add(0);	
		this.weekdaysData.add(0);

		// Prepare weeks
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


		// Prepare months
		Map<Integer, Integer> monthyear_to_index = new HashMap<Integer, Integer>(); 
		this.monthsKeys = new LinkedList<String>();
		this.monthsData = new LinkedList<Integer>();
		Calendar month = (Calendar) today.clone();
		month.clear(Calendar.HOUR);
		for (int i = 0; i < 7; i++) {
			this.monthsKeys.add(0, month.getDisplayName(Calendar.MONTH, Calendar.SHORT, locale));
			int index = month.get(Calendar.YEAR) + month.get(Calendar.MONTH) * 10000;
			monthyear_to_index.put(index, 6-i);
			month.add(Calendar.MONTH, -1);
			this.monthsData.add(0, 0);			
		}

		// Prepare quarters
		Map<Integer, Integer> quarteryear_to_index = new HashMap<Integer, Integer>(); 
		this.quarterKeys = new LinkedList<String>();
		this.quarterData = new LinkedList<Integer>();
		Calendar quarter = (Calendar) today.clone();
		quarter.clear(Calendar.HOUR);
		for (int i = 0; i < 7; i++) {
			//month.getDisplayName(Calendar.WEEK_OF_YEAR, Calendar.SHORT, Locale.getDefault())
			this.quarterKeys.add(0, "Q" + Integer.toString(quarter.get(Calendar.MONTH)/3+1));
			int index = quarter.get(Calendar.YEAR) * 4 + quarter.get(Calendar.MONTH) / 3;
			quarteryear_to_index.put(index, 6-i);
			quarter.add(Calendar.MONTH, -3);
			this.quarterData.add(0, 0);			
		}

		// Prepare years
		Map<Integer, Integer> year_to_index = new HashMap<Integer, Integer>();
		this.yearsKeys = new LinkedList<String>();
		this.yearsData = new LinkedList<Integer>();
		Calendar year = (Calendar) today.clone();
		year.clear(Calendar.HOUR);
		for (int i = 0; i < 7; i++) {
			this.yearsKeys.add(0, Integer.toString(year.get(Calendar.YEAR)));
			int index = year.get(Calendar.YEAR);
			year_to_index.put(index, 6-i);
			year.add(Calendar.YEAR, -1);
			this.yearsData.add(0, 0);
		}
		
		// Collect all data
		for (Tick tick : ticks) {
			int day_of_week = tick.getDate().get(Calendar.DAY_OF_WEEK) - 2;
			if (day_of_week < 0) day_of_week = 6;
			int newcount = this.weekdaysData.get(day_of_week)+1;
			if (newcount > this.weekdaysMaximum) {
				this.weekdaysMaximum = newcount;
			}
			this.weekdaysData.set(day_of_week, newcount);
			
			int weekyear = tick.getDate().get(Calendar.YEAR) + tick.getDate().get(Calendar.WEEK_OF_YEAR) * 10000;
			if (weekyear_to_index.containsKey(weekyear)) {
				int index = weekyear_to_index.get(weekyear);
				int newcount2 = this.weeksData.get(index)+1;
				if (newcount2 > this.weeksMaximum)
					this.weeksMaximum = newcount2;
				this.weeksData.set(index, newcount2);
			}
			
			int monthyear = tick.getDate().get(Calendar.YEAR) + tick.getDate().get(Calendar.MONTH) * 10000;
			if (monthyear_to_index.containsKey(monthyear)) {
				int index = monthyear_to_index.get(monthyear);
				int newcount2 = this.monthsData.get(index)+1;
				if (newcount2 > this.monthsMaximum)
					this.monthsMaximum = newcount2;
				this.monthsData.set(index, newcount2);
			}
			
			int quarteryear = tick.getDate().get(Calendar.YEAR) * 4 + tick.getDate().get(Calendar.MONTH) / 3;
			if (quarteryear_to_index.containsKey(quarteryear)) {
				int index = quarteryear_to_index.get(quarteryear);
				int newcount2 = this.quarterData.get(index)+1;
				if (newcount2 > this.quarterMaximum)
					this.quarterMaximum = newcount2;
				this.quarterData.set(index, newcount2);
			}

			int cyear = tick.getDate().get(Calendar.YEAR);
			if (year_to_index.containsKey(cyear)) {
				int index = year_to_index.get(cyear);
				int newcount2 = this.yearsData.get(index) + 1;
				if (newcount2 > this.yearsMaximum)
					this.yearsMaximum = newcount2;
				this.yearsData.set(index, newcount2);
			}
		}
		
		if (this.weeksMaximum < 7)
			this.weeksMaximum = 7;
		
		if (this.monthsMaximum < 31)
			this.monthsMaximum = 31;

		if (this.quarterMaximum < 31)
			this.quarterMaximum = 31;

		if (this.yearsMaximum < 31)
			this.yearsMaximum = 31;
	}
	
	private void loadTrack(int track_id) {
		track = ds.getTrack(track_id);
		tickCount = ds.getTickCount(track_id);
		ticks = ds.getTicks(track_id);
		
		if (ticks.size() > 0) {
			firstTickDate = ticks.get(0).getDate();
			lastTickDate = ticks.get(ticks.size() - 1).getDate();
		}
		else {
			firstTickDate = null;
			lastTickDate = null;
		}
		today = Calendar.getInstance();
	
		fillTrackUI();
		
	}
	
	private void fillTrackUI() {
		text_name = (TextView) findViewById(R.id.textView_name);
		text_name.setText(track.getName());
		text_description = (TextView) findViewById(R.id.TextView_description);
		text_description.setText(track.getDescription());
		SummaryNumber sn1 = (SummaryNumber) findViewById(R.id.summaryNumber1);
		SummaryNumber sn2 = (SummaryNumber) findViewById(R.id.summaryNumber2);
		SummaryNumber sn3 = (SummaryNumber) findViewById(R.id.summaryNumber3);

		double milliSecsInADay = 1000.0 * 3600 * 24;
		double weeklymean = -1;
		double days_since_last = -1;
		
		if (firstTickDate != null && lastTickDate != null) {
			double weeks = Math.ceil(((double)(today.getTimeInMillis() - firstTickDate.getTimeInMillis())) / milliSecsInADay / 7.0); 
			days_since_last = ((double)(today.getTimeInMillis() - lastTickDate.getTimeInMillis())) / milliSecsInADay; 
			weeklymean = tickCount/weeks;
		}
		
		sn1.setData(tickCount, 0, (String) getText(R.string.show_track_total));
		sn2.setData(weeklymean, 1, (String) getText(R.string.show_track_weeklymean));
		sn3.setData(days_since_last, 0, (String) getText(R.string.show_track_dayssincelast));

		retrieveGraphData();
		
		graph_weekdays = (SummaryGraph) findViewById(R.id.summaryGraph_weekdays);
		graph_weekdays.setCyclic(true);
		graph_weekdays.setData(this.weekdaysData, this.weekdaysKeys, this.weekdaysMaximum);
		graph_weeks = (SummaryGraph) findViewById(R.id.summaryGraph_weeks);
		graph_weeks.setData(this.weeksData, this.weeksKeys, this.weeksMaximum);
		graph_months = (SummaryGraph) findViewById(R.id.summaryGraph_months);
		graph_months.setData(this.monthsData, this.monthsKeys, this.monthsMaximum);
		graph_quarters = (SummaryGraph) findViewById(R.id.summaryGraph_quarters);
		graph_quarters.setData(this.quarterData, this.quarterKeys, this.quarterMaximum);
		graph_years = (SummaryGraph) findViewById(R.id.summaryGraph_years);
		graph_years.setData(this.yearsData, this.yearsKeys, this.yearsMaximum);

		image_icon = (ImageView) findViewById(R.id.image_icon);
		image_icon.setImageResource(track.getIconId(this));
	}
	
	private void deleteTrack() {
		this.ds.deleteTrack(this.track);
		NavUtils.navigateUpFromSameTask(this);
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
			Intent intent = new Intent(this, TrackPreferenceActivity.class);
			intent.putExtra("track_id", track.getId());
			startActivityForResult(intent, 1);				
			return true;
		case R.id.action_delete:
			new AlertDialog.Builder(this)
			.setTitle(R.string.alert_delete_track_title)
		    .setMessage(R.string.alert_delete_track_message)
		    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
		        public void onClick(DialogInterface dialog, int which) { 
		        	deleteTrack();
		        }
		     })
		    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
		        public void onClick(DialogInterface dialog, int which) { 
		            // do nothing
		        }
		     })
		    .show();			
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
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		loadTrack(track.getId());
		super.onActivityResult(requestCode, resultCode, data);
	}

}
