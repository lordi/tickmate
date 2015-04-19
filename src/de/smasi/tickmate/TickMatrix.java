package de.smasi.tickmate;

import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.app.Activity;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.graphics.Color;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.CursorAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import de.smasi.tickmate.database.TracksDataSource;
import de.smasi.tickmate.models.DayRange;
import de.smasi.tickmate.models.Track;
import de.smasi.tickmate.views.ShowTrackActivity;
import de.smasi.tickmate.widgets.TickAdapter;
import de.smasi.tickmate.widgets.MultiTickButton;
import de.smasi.tickmate.widgets.TickButton;

public class TickMatrix extends LinearLayout implements OnCheckedChangeListener, OnScrollListener  {
	
	ListView sv = null;
	LinkedList<DayRange> mList = new LinkedList<DayRange>();
	Calendar displayDay = null;
    TickAdapter mAdapter;
	
	public TickMatrix(Context context, AttributeSet attrs) {
		super(context, attrs);
		unsetDate();
	}

	public void setDate(int year, int month, int day) {
		displayDay.set(year, month, day);
		Calendar today = Calendar.getInstance();
		today.set(Calendar.HOUR, 0);
		today.set(Calendar.MINUTE, 0);
		today.set(Calendar.SECOND, 0);
		today.set(Calendar.MILLISECOND, 0);
				
		// Make sure endday is not after today.
		if (displayDay.compareTo(today) > 0) { 
			displayDay = today;
		}
						
	}
	
	public Calendar getDate() {
		return displayDay;	
	}
	
	public void unsetDate() {
		displayDay = Calendar.getInstance();
	}
	
	public void buildView() {
		Context context = getContext();
		Locale locale = Locale.getDefault();
		this.setOrientation(VERTICAL);
		this.removeAllViews();
		int rows = 14; // number of days that will be displayed
		int rowHeight = -1;
		
		TracksDataSource ds = new TracksDataSource(context);
		ds.open();
		
		List<Track> tracks = ds.getActiveTracks(); 
		
		if (tracks.size() == 0) {
			TextView tv = new TextView(context);
			tv.setText(R.string.no_tracks_found);
			tv.setGravity(Gravity.CENTER);
			tv.setPadding(20, 20, 20, 20);
			tv.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
			tv.setTextColor(context.getResources().getColor(android.R.color.secondary_text_dark));
			this.addView(tv);
			ds.close();
			return;			
		}		
					
		displayDay.set(Calendar.HOUR, 0);
		displayDay.set(Calendar.MINUTE, 0);
		displayDay.set(Calendar.SECOND, 0);
		displayDay.set(Calendar.MILLISECOND, 0);
		

		Calendar today = Calendar.getInstance();
		today.set(Calendar.HOUR, 0);
		today.set(Calendar.MINUTE, 0);
		today.set(Calendar.SECOND, 0);
		today.set(Calendar.MILLISECOND, 0);
				
		Calendar yday = (Calendar)today.clone();
		yday.add(Calendar.DATE, -1);
		
		Calendar endday = (Calendar)displayDay.clone();
		endday.add(Calendar.DATE, rows/4);
		
		// Make sure endday is not after today.
		if (endday.compareTo(today) > 0) { 
			endday = (Calendar)today.clone();
		}
				
		Calendar startday = (Calendar)endday.clone();
		startday.add(Calendar.DATE, -rows);
		
		java.text.DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(context);
		
		LinearLayout headertop = new LinearLayout(getContext());
		headertop.setOrientation(LinearLayout.HORIZONTAL);
		
		LinearLayout headerrow = new LinearLayout(getContext());
		headerrow.setOrientation(LinearLayout.HORIZONTAL);
		TextView b2 = new TextView(context);
		b2.setText("");
		
		b2.setPadding(0, 0, 0, 0);
		b2.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, rowHeight, 0.8f));
		
		for (Track track : tracks) {
			TrackButton b = new TrackButton(context, track);

			b.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT, (1.0f)/tracks.size()));
			headerrow.addView(b);
		}
		headerrow.setWeightSum(1.0f);
		headerrow.setPadding(5, 5, 10, 5);
		headerrow.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, rowHeight, 0.2f));
		
		headertop.addView(b2);
		headertop.addView(headerrow);
		headertop.setWeightSum(1.0f);		
		headertop.setPadding(10, 0, 10, 0);
		headertop.setBackgroundResource(R.drawable.bottom_line);
		
		sv = new ListView(getContext());
		sv.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		/*
				LinearLayout svlin = new LinearLayout(getContext());
		svlin.setOrientation(LinearLayout.VERTICAL);

		// Create a progress bar to display while the list loads
        ProgressBar progressBar = new ProgressBar(getContext());
        progressBar.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT, Gravity.CENTER));
        progressBar.setIndeterminate(true);
        sv.setEmptyView(progressBar);
        */
		
		//LinearLayout tickgrid = buildGrid(tracks, startday, endday);

		/*TextView t = new TextView(context);
		t.setText("Loadmore");
		svlin.addView(t);
		svlin.addView(tickgrid);
		*/
		
		mList.add(new DayRange(startday, endday));
		//days.add(new DayRange(startday, endday));

        //mAdapter = new DayRangeAdapter(getContext(), mList); 
		//mAdapter.setNotifyOnChange(false);
        //sv.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
		sv.setStackFromBottom(true);
        sv.setAdapter(mAdapter);
        sv.setOnScrollListener(this);
			
		addView(headertop);
		addView(sv);
		
		/*
		sv.post(new Runnable() { 
	        public void run() { 
	        	sv.fullScroll(View.FOCUS_DOWN);
	        } 
		});*/
	}
	
	private LinearLayout buildGrid(List<Track> tracks, Calendar startday, Calendar endday) {
		int rows = 14;
		int rowHeight = -1;
		Calendar today = Calendar.getInstance();
		today.set(Calendar.HOUR, 0);
		today.set(Calendar.MINUTE, 0);
		today.set(Calendar.SECOND, 0);
		today.set(Calendar.MILLISECOND, 0);
				
		Calendar yday = (Calendar)today.clone();
		yday.add(Calendar.DATE, -1);
		Context context = getContext();
		Locale locale = Locale.getDefault();
		java.text.DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(context);

		TracksDataSource ds = new TracksDataSource(context);
		ds.open();
		// This will be the Calendar object will use for iteration
				Calendar cal = (Calendar)startday.clone();
				
				// Limit ticks to range [startday, endday]
				ds.retrieveTicks(startday, endday);		
				ds.close();
				LinearLayout tickgrid = new LinearLayout(getContext());
				tickgrid.setOrientation(LinearLayout.VERTICAL);

				for (int y=0; y < rows; y++) {
					cal.add(Calendar.DATE, 1);
					Date date = cal.getTime();
					String s = dateFormat.format(date);
					
					TextView t_weekday = new TextView(getContext());
					TextView t_date = new TextView(getContext());
					
					if (cal.compareTo(today) == 0)
						t_date.setText(context.getString(R.string.today));
					else if (cal.compareTo(yday) == 0)
						t_date.setText(context.getString(R.string.yesterday));
					else
						t_date.setText(s);
					
					// add splitter for first weekday depending on current locale
					if (cal.get(Calendar.DAY_OF_WEEK) == cal.getFirstDayOfWeek()) {
						TextView splitter2 = new TextView(getContext());
						splitter2.setText("");
						splitter2.setHeight(5);
						tickgrid.addView(splitter2);
						TextView splitter = new TextView(getContext());
						splitter.setText("");
						splitter.setHeight(11);
						splitter.setBackgroundResource(R.drawable.center_line);
						splitter.setPadding(0, 20, 0, 0);
						tickgrid.addView(splitter);
					}			
					
					String day_name=cal.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, locale);
					t_weekday.setText(day_name.toUpperCase(locale));
					
					t_weekday.setTextAppearance(getContext(), android.R.style.TextAppearance_Medium);
					t_date.setWidth(120);
					t_date.setTextAppearance(getContext(), android.R.style.TextAppearance_Small);
					t_date.setTextSize((float) 11.0);
					t_date.setTextColor(Color.GRAY);
					t_weekday.setWidth(120);
					LinearLayout row = new LinearLayout(getContext());
					row.setOrientation(LinearLayout.HORIZONTAL);
					LinearLayout l = new LinearLayout(getContext());
					l.setOrientation(LinearLayout.VERTICAL);
					l.addView(t_weekday);
					l.addView(t_date);
					t_date.setEllipsize(null);
					t_weekday.setEllipsize(null);
					
					// Some screen characteristics:
					//float scaledDensity = context.getResources().getDisplayMetrics().scaledDensity;
					//int densityDpi = context.getResources().getDisplayMetrics().densityDpi;
					//Log.d("tickmate", t_weekday.getTextSize() + "|" + t_date.getTextSize() + "|" + scaledDensity + "|" + densityDpi);
					// Small screen, normal font	27.0|16.5|1.5|240
					// Small screen, huge font  	35.0|21.449999|1.9499999|240
					// Huge screen, normal font 	24.0|14.643751|1.3312501|213
					// Huge screen, huge font   	31.0|19.036875|1.730625|213

					if (rowHeight <= 0) {
						rowHeight = (int)(t_weekday.getTextSize() + t_date.getTextSize()) + 40;
					}
					
					l.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, rowHeight, 0.8f));
					l.setGravity(Gravity.CENTER_VERTICAL);
							
					LinearLayout l2 = new LinearLayout(getContext());
					l2.setOrientation(LinearLayout.HORIZONTAL);
					for (Track track : tracks) {
						
						if (track.multipleEntriesEnabled()) {
							MultiTickButton counter = new MultiTickButton(getContext(), track, (Calendar) cal.clone());
							counter.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT, (1.0f)/tracks.size()));
							counter.setTickCount(ds.getTickCountForDay(track, cal));
							l2.addView(counter);
						} else {
							TickButton checker = new TickButton(getContext(), track, (Calendar) cal.clone());
							checker.setChecked(ds.isTicked(track, (Calendar) cal.clone(), false));
							checker.setOnCheckedChangeListener(this);
							//checker.setLayoutParams(new LayoutParams(32, 32, 0.2f));
							//checker.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, (1.0f-0.2f)/tracks.size()));
							//checker.setLayoutParams(new LayoutParams(0,0, (1.0f-0.2f)/tracks.size()));
							checker.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT, (1.0f)/tracks.size()));
							l2.addView(checker);
						}
						
					}
					l2.setWeightSum(1.0f);
					l2.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, rowHeight, 0.2f));

					row.addView(l);
					row.addView(l2);
					row.setGravity(Gravity.CENTER);
					
					if (cal.compareTo(displayDay) == 0) {
						row.setBackgroundResource(android.R.drawable.dark_header);
						row.setPadding(0, 0, 0, 0);
					}
					
					tickgrid.addView(row);	
				}
				
				
				tickgrid.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
				tickgrid.setPadding(10, 0, 10, 5);
				return tickgrid;
				
	}
	
	
	public class TrackButton extends ImageButton implements OnClickListener{
		Track track;
		
		public TrackButton(Context context, Track track) {
			super(context);
			this.track = track;
			this.setOnClickListener(this);
			this.setImageResource(track.getIconId(context));
		}

		@Override
		public void onClick(View v) {
			Intent intent = new Intent(getContext(), ShowTrackActivity.class);
			intent.putExtra("track_id", track.getId());
			getContext().startActivity(intent);
		}
	}
	
	

	
	@Override
	public void onCheckedChanged(CompoundButton arg0, boolean ticked) {
		TickButton tb = (TickButton)arg0;
		
		TracksDataSource ds = new TracksDataSource(this.getContext());
		ds.open();
		if (ticked) {
			ds.setTick(tb.getTrack(), tb.getDate(), true);
		}
		else {
			ds.removeTick(tb.getTrack(), tb.getDate());
		}
		ds.close();		
	}
	
	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		Log.v("TICKMATE", 
				"first: " + Integer.toString(firstVisibleItem) +
				", vis: " + Integer.toString(visibleItemCount) +
				", total: " + Integer.toString(totalItemCount));
		if (firstVisibleItem == 0 && totalItemCount < 100) {
			DayRange dr = mList.get(0); //mList.size() - 1);//(DayRange) view.getAdapter().getItem(0);
			
			Calendar prevstart = dr.getStartDay();
			Calendar end = (Calendar)prevstart.clone();
			end.add(Calendar.DATE, -1);
			Calendar start = (Calendar)end.clone();
			start.add(Calendar.DATE, -2);
			mList.add(0, new DayRange(start, end));
			((Activity) getContext()).runOnUiThread(new Runnable() {
			    public void run() {
			        mAdapter.notifyDataSetChanged();
			    }
			});
		}
	}
	
}
