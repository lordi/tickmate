package de.smasi.tickmate.views;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Map;

import de.smasi.tickmate.R;
import de.smasi.tickmate.database.DataSource;
import de.smasi.tickmate.models.Tick;
import de.smasi.tickmate.models.Track;
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
    private SummaryGraph graph_streaks;

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

    private List<Integer> streaksData;
    private List<String> streaksKeys;
    private int streakOnMaximum;
    private int streakOffMaximum;

	private int trendRange;
	private String trendCaption;
	private int trendData[] = null;

	private final static int NUMBER_OF_CATEGORIES = 7;

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
        this.weekdaysData = new LinkedList<Integer>();
        Calendar day = (Calendar) today.clone();
		day.set(Calendar.DAY_OF_WEEK, today.getFirstDayOfWeek());
		
		for (int i = 0; i < 7; i++) {
			this.weekdaysKeys.add(day.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, locale).toUpperCase(locale));
			day.add(Calendar.DATE, 1);
            this.weekdaysData.add(0);
		}


		// Prepare weeks
		Map<Integer, Integer> weekyear_to_index = new HashMap<Integer, Integer>();
		this.weeksKeys = new LinkedList<String>();
		this.weeksData = new LinkedList<Integer>();
		Calendar week = (Calendar) today.clone();
		week.clear(Calendar.HOUR);
		for (int i = 0; i < NUMBER_OF_CATEGORIES; i++) {
			//month.getDisplayName(Calendar.WEEK_OF_YEAR, Calendar.SHORT, Locale.getDefault())
			this.weeksKeys.add(0, Integer.toString(week.get(Calendar.WEEK_OF_YEAR)));
			int index = week.get(Calendar.YEAR) + week.get(Calendar.WEEK_OF_YEAR) * 10000;
			weekyear_to_index.put(index, NUMBER_OF_CATEGORIES-1-i);
			week.add(Calendar.WEEK_OF_YEAR, -1);
			this.weeksData.add(0, 0);
		}


		// Prepare months
		Map<Integer, Integer> monthyear_to_index = new HashMap<Integer, Integer>(); 
		this.monthsKeys = new LinkedList<String>();
		this.monthsData = new LinkedList<Integer>();
		Calendar month = (Calendar) today.clone();
		month.clear(Calendar.HOUR);
		for (int i = 0; i < NUMBER_OF_CATEGORIES; i++) {
			this.monthsKeys.add(0, month.getDisplayName(Calendar.MONTH, Calendar.SHORT, locale));
			int index = month.get(Calendar.YEAR) + month.get(Calendar.MONTH) * 10000;
			monthyear_to_index.put(index, NUMBER_OF_CATEGORIES-1-i);
			month.add(Calendar.MONTH, -1);
			this.monthsData.add(0, 0);			
		}

		// Prepare quarters
		Map<Integer, Integer> quarteryear_to_index = new HashMap<Integer, Integer>(); 
		this.quarterKeys = new LinkedList<String>();
		this.quarterData = new LinkedList<Integer>();
		Calendar quarter = (Calendar) today.clone();
		quarter.clear(Calendar.HOUR);
		for (int i = 0; i < NUMBER_OF_CATEGORIES; i++) {
			//month.getDisplayName(Calendar.WEEK_OF_YEAR, Calendar.SHORT, Locale.getDefault())
			this.quarterKeys.add(0, "Q" + Integer.toString(quarter.get(Calendar.MONTH)/3+1));
			int index = quarter.get(Calendar.YEAR) * 4 + quarter.get(Calendar.MONTH) / 3;
			quarteryear_to_index.put(index, NUMBER_OF_CATEGORIES-1-i);
			quarter.add(Calendar.MONTH, -3);
			this.quarterData.add(0, 0);			
		}

		// Prepare years
		Map<Integer, Integer> year_to_index = new HashMap<Integer, Integer>();
		this.yearsKeys = new LinkedList<String>();
		this.yearsData = new LinkedList<Integer>();
		Calendar year = (Calendar) today.clone();
		year.clear(Calendar.HOUR);
		for (int i = 0; i < NUMBER_OF_CATEGORIES; i++) {
			this.yearsKeys.add(0, Integer.toString(year.get(Calendar.YEAR)));
			int index = year.get(Calendar.YEAR);
			year_to_index.put(index, NUMBER_OF_CATEGORIES-1-i);
			year.add(Calendar.YEAR, -1);
			this.yearsData.add(0, 0);
		}

        // Prepare streaks
        this.streaksData = new LinkedList<>();
        this.streaksKeys = new LinkedList<>();
        for (int i = 0; i < 2 * NUMBER_OF_CATEGORIES; i++) {
            this.streaksData.add(0);
            this.streaksKeys.add(0, "");
        }
        this.streakOnMaximum = ticks.size() > 0 ? 1 : 0;
        this.streakOffMaximum = 0;

		if (ticks.size() > 0) {
			Calendar last_on = firstTickDate;
			streaksData.set(streaksData.size() - 1, 1);

			// Collect all data
			for (Tick tick : ticks) {
				tick.date.set(Calendar.HOUR_OF_DAY,0);
				tick.date.set(Calendar.MINUTE,0);
				tick.date.set(Calendar.SECOND,0);
				int day_of_week = tick.date.get(Calendar.DAY_OF_WEEK) - today.getFirstDayOfWeek();
				if (day_of_week < 0) day_of_week += 7;
				int newcount = this.weekdaysData.get(day_of_week)+1;
				if (newcount > this.weekdaysMaximum) {
					this.weekdaysMaximum = newcount;
				}
				this.weekdaysData.set(day_of_week, newcount);

				int weekyear = tick.date.get(Calendar.YEAR) + tick.date.get(Calendar.WEEK_OF_YEAR) * 10000;
				if (weekyear_to_index.containsKey(weekyear)) {
					int index = weekyear_to_index.get(weekyear);
					int newcount2 = this.weeksData.get(index)+1;
					if (newcount2 > this.weeksMaximum)
						this.weeksMaximum = newcount2;
					this.weeksData.set(index, newcount2);
				}

				int monthyear = tick.date.get(Calendar.YEAR) + tick.date.get(Calendar.MONTH) * 10000;
				if (monthyear_to_index.containsKey(monthyear)) {
					int index = monthyear_to_index.get(monthyear);
					int newcount2 = this.monthsData.get(index)+1;
					if (newcount2 > this.monthsMaximum)
						this.monthsMaximum = newcount2;
					this.monthsData.set(index, newcount2);
				}

				int quarteryear = tick.date.get(Calendar.YEAR) * 4 + tick.date.get(Calendar.MONTH) / 3;
				if (quarteryear_to_index.containsKey(quarteryear)) {
					int index = quarteryear_to_index.get(quarteryear);
					int newcount2 = this.quarterData.get(index)+1;
					if (newcount2 > this.quarterMaximum)
						this.quarterMaximum = newcount2;
					this.quarterData.set(index, newcount2);
				}

				int cyear = tick.date.get(Calendar.YEAR);
				if (year_to_index.containsKey(cyear)) {
					int index = year_to_index.get(cyear);
					int newcount2 = this.yearsData.get(index) + 1;
					if (newcount2 > this.yearsMaximum)
						this.yearsMaximum = newcount2;
					this.yearsData.set(index, newcount2);
				}

				int days_since = (int)((tick.date.getTimeInMillis() - last_on.getTimeInMillis()) / (24*60*60*1000));
				if (days_since > this.streakOffMaximum) {
					this.streakOffMaximum = days_since - 1;
				}
				if (days_since == 1) {
					// increase last streak by one day
					int currentStreak = streaksData.get(streaksData.size() - 1) + 1;
					streaksData.set(streaksData.size() - 1, currentStreak);
					if (currentStreak > this.streakOnMaximum) {
						this.streakOnMaximum = currentStreak;
					}
				}
				else if (days_since > 1){
					streaksData.add(-days_since + 1);	// add a new pause
					streaksData.add(1);					// add a new streak
				}
				last_on = tick.date;
			}

			int days_since = (int)((today.getTimeInMillis() - last_on.getTimeInMillis()) / (24*60*60*1000));
			if (days_since > 0) {
				streaksData.add(-days_since);
				if (days_since > this.streakOffMaximum) {
					this.streakOffMaximum = days_since;
				}
			}

			// trend calculation (only if trend range is less than or equal data range)
			if ((int) ((today.getTimeInMillis() - firstTickDate.getTimeInMillis()) / (24 * 60 * 60 * 1000)) >= trendRange - 1) {
				this.trendData = new int[trendRange];
				ListIterator<Tick> tickReverseIterator = ticks.listIterator(ticks.size()); // credits to stackoverflow.com/a/15005226/3944322
				int days;       // number of days from today
				Tick tick;
				try {
					while (tickReverseIterator.hasPrevious()) {
						tick = tickReverseIterator.previous();
						tick.date.set(Calendar.HOUR_OF_DAY, 0); //TODO general solution for clearing time
						tick.date.set(Calendar.MINUTE, 0);
						tick.date.set(Calendar.SECOND, 0);
						days = (int) ((today.getTimeInMillis() - tick.date.getTimeInMillis()) / (24 * 60 * 60 * 1000));
						trendData[days]++;
					}
				} catch (IndexOutOfBoundsException stopHere) {
					// stop iterating over ordered list here, as we've left the trend range
				}
			}
		}

        if (streaksData.size() > 2 * NUMBER_OF_CATEGORIES) {
            streaksData = streaksData.subList(streaksData.size() - 2 * NUMBER_OF_CATEGORIES, streaksData.size());
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
			firstTickDate = ticks.get(0).date;
			firstTickDate.set(Calendar.HOUR_OF_DAY,0);
			firstTickDate.set(Calendar.MINUTE,0);
			firstTickDate.set(Calendar.SECOND,0);
			lastTickDate = ticks.get(ticks.size() - 1).date;
			lastTickDate.set(Calendar.HOUR_OF_DAY,0);
			lastTickDate.set(Calendar.MINUTE,0);
			lastTickDate.set(Calendar.SECOND,0);
		}
		else {
			firstTickDate = null;
			lastTickDate = null;
		}
		today = Calendar.getInstance();
		today.set(Calendar.HOUR_OF_DAY,0);
		today.set(Calendar.MINUTE,0);
		today.set(Calendar.SECOND,0);

		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		trendRange = Integer.parseInt(sharedPrefs.getString("trend-range-key", "14"));
		String[] trendRangeValues = getResources().getStringArray(R.array.pref_values_trend_range);
		int trendRangeIndex;
		for (trendRangeIndex = 0; trendRangeIndex < trendRangeValues.length; trendRangeIndex++){
			if (Integer.parseInt(trendRangeValues[trendRangeIndex]) == trendRange)
				break;
		}
		String[] trendRangeTitles = getResources().getStringArray(R.array.pref_titles_trend_range);
		trendCaption = trendRangeTitles[trendRangeIndex];

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

		retrieveGraphData();

		double milliSecsInADay = 1000.0 * 3600 * 24;
		double weeklymean = -1;

		if (firstTickDate != null && lastTickDate != null) {
			double days = Math.ceil(((double)(today.getTimeInMillis() - firstTickDate.getTimeInMillis())) / milliSecsInADay); 
			weeklymean = (tickCount/days)*7.0;
		}

		sn1.setData(tickCount, 0, (String) getText(R.string.show_track_total));
		sn1.setColor(track.getTickColor().getColorValue());
		sn2.setData(weeklymean, 1, (String) getText(R.string.show_track_weeklymean));
		sn2.setColor(track.getTickColor().getColorValue());

		// Trend indicator
		if (trendData != null) {
			int n = trendData.length - 1;
			long sx = n * (n + 1) / 2;        // some shortcuts for sequential x values, although
			long sxx = sx * (2 * n + 1) / 3;  // time saving is negligible for our small values of n
			long sy = 0L, sxy = 0L;
			for (int i = 0; i <= n; i++) {
				sy += trendData[i];
				sxy += i * trendData[i];
			}
			// the slope is -b in a + bx as time runs backwards in trendData
			double slope = (double) (sx * sy - trendData.length * sxy) / (trendData.length * sxx - sx * sx);
			sn3.setData(90.0 * slope, -1, trendCaption);
		} else {
			sn3.setData(-1.0, 0, trendCaption);
		}
		sn3.setColor(track.getTickColor().getColorValue());


        /* Streaks */
        SummaryNumber streakOnNumber = (SummaryNumber) findViewById(R.id.summaryNumberStreakOn);
        SummaryNumber streakOffNumber = (SummaryNumber) findViewById(R.id.summaryNumberStreakOff);

        streakOnNumber.setData(streakOnMaximum, 0, (String) getText(R.string.longest_streak_on));
        streakOnNumber.setColor(track.getTickColor().getColorValue());

        streakOffNumber.setData(streakOffMaximum, 0, (String) getText(R.string.longest_streak_off));
        streakOffNumber.setColor(track.getTickColor().getColorValue());

        graph_streaks = (SummaryGraph) findViewById(R.id.summaryGraph_streaks);
        graph_streaks.setData(this.streaksData, this.streaksKeys, this.streakOnMaximum, this.streakOffMaximum);
        graph_streaks.setColor(track.getTickColor().getColorValue());

        graph_weekdays = (SummaryGraph) findViewById(R.id.summaryGraph_weekdays);
        graph_weekdays.setCyclic(true);
        graph_weekdays.setData(this.weekdaysData, this.weekdaysKeys, this.weekdaysMaximum);
        graph_weekdays.setColor(track.getTickColor().getColorValue());
		graph_weeks = (SummaryGraph) findViewById(R.id.summaryGraph_weeks);
		graph_weeks.setData(this.weeksData, this.weeksKeys, this.weeksMaximum);
        graph_weeks.setColor(track.getTickColor().getColorValue());
        graph_months = (SummaryGraph) findViewById(R.id.summaryGraph_months);
		graph_months.setData(this.monthsData, this.monthsKeys, this.monthsMaximum);
        graph_months.setColor(track.getTickColor().getColorValue());
        graph_quarters = (SummaryGraph) findViewById(R.id.summaryGraph_quarters);
		graph_quarters.setData(this.quarterData, this.quarterKeys, this.quarterMaximum);
        graph_quarters.setColor(track.getTickColor().getColorValue());
        graph_years = (SummaryGraph) findViewById(R.id.summaryGraph_years);
		graph_years.setData(this.yearsData, this.yearsKeys, this.yearsMaximum);
        graph_years.setColor(track.getTickColor().getColorValue());

		image_icon = (ImageView) findViewById(R.id.image_icon);
		image_icon.setImageResource(track.getIconId(this));

        LinearLayout header = (LinearLayout) findViewById(R.id.show_track_header);

        // Unfortunately, getTickColor().getColorValue() doesn't work here :(
        //header.setBackgroundColor(Color.parseColor(track.getTickColor().hex()));
        header.setBackgroundDrawable(track.getTickColor().getDrawable(128));

        header.invalidate();
        getActionBar().setBackgroundDrawable(track.getTickColor().getDrawable(255));
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
