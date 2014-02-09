package de.smasi.tickmate;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import de.smasi.tickmate.database.TracksDataSource;
import de.smasi.tickmate.models.Tick;
import de.smasi.tickmate.models.Track;
import de.smasi.tickmate.views.ShowTrackActivity;

public class TickMatrix extends LinearLayout implements OnCheckedChangeListener {
	
	ScrollView sv = null;
	Calendar displayDay = null;
	
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
		
		List<Track> tracks = ds.getMyTracks(); 
		
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
		
		// This will be the Calendar object will use for iteration
		Calendar cal = (Calendar)startday.clone();
		
		// TODO: Limit ticks to range [startday, endday]
		ds.retrieveTicks();		
		ds.close();
		
		java.text.DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(context);
		
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
		sv = new ScrollView(getContext());
		sv.addView(tickgrid);
		addView(headertop);
		addView(sv);
		
		sv.post(new Runnable() { 
	        public void run() { 
	        	sv.fullScroll(View.FOCUS_DOWN);
	        } 
		});
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
	
	
	public class MultiTickButton extends Button implements OnClickListener, OnLongClickListener {
		Track track;
		Calendar date;

		public MultiTickButton(Context context, Track track, Calendar date) {
			super(context);
			this.setOnClickListener(this);
			this.setOnLongClickListener(this);
			this.track = track;
			this.date = date;
			int size = 32;
			this.setWidth(size);
			this.setMinWidth(size);
			this.setMaxWidth(size);
			this.setHeight(size);
			this.setMinHeight(size);
			this.setPadding(0, 0, 0, 0);
			
			this.updateText();
		}
		
		Track getTrack () {
			return track;
		}
		
		Calendar getDate () {
			return date;		
		}
		
		private void updateText() {
			TracksDataSource ds = new TracksDataSource(this.getContext());
			List<Tick> ticks = ds.getTicksForDay(this.getTrack(), this.getDate());
			
			if (ticks.size() > 0) {
				this.setBackgroundResource(R.drawable.counter_positive);
				this.setText(Integer.toString(ticks.size()));
			} else {
				this.setBackgroundResource(R.drawable.counter_neutral);
				this.setText("");
			}
		}
		
		@Override
		public void onClick(View v) {
			TracksDataSource ds = new TracksDataSource(this.getContext());
			
			Calendar c = Calendar.getInstance();
			c.set(Calendar.MILLISECOND, 0);

			ds.open();
			if (c.get(Calendar.DAY_OF_MONTH) == this.date.get(Calendar.DAY_OF_MONTH)) {
				ds.setTick(this.getTrack(), c, false);
			} else {
				ds.setTick(this.getTrack(), this.date, false);
			}
			ds.close();
			
			this.updateText();
		}
		
		@Override
		public boolean onLongClick(View v) {
			TracksDataSource ds = new TracksDataSource(this.getContext());
			ds.open();
			boolean success = ds.removeLastTickOfDay(this.getTrack(), this.getDate());
			ds.close();
			
			if (success) {
				this.updateText();
				Toast.makeText(this.getContext(), R.string.tick_deleted, Toast.LENGTH_SHORT).show();
			}
			
			return true;
		}
	}

	
	public class TickButton extends ToggleButton {
		Track track;
		Calendar date;

		public TickButton(Context context, Track track, Calendar date) {
			super(context);		
			this.track = track;
			this.date = date;
			//this.setLayoutParams(new android.widget.LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, 20));
			this.setBackgroundResource(R.drawable.toggle_button);
			int size = 32;
			this.setWidth(size);
			this.setMinWidth(size);
			this.setMaxWidth(size);
			this.setHeight(size);
			this.setMinHeight(size);
			this.setPadding(0, 0, 0, 0);
			this.setTextOn("");
			this.setTextOff("");

		}
		
		Track getTrack () {
			return track;
		}
		
		Calendar getDate () {
			return date;		
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
}
