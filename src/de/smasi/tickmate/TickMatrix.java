package de.smasi.tickmate;

import android.R.color;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.awt.font.TextAttribute;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import de.smasi.tickmate.database.TracksDataSource;
import de.smasi.tickmate.models.Track;
import de.smasi.tickmate.views.EditTrackActivity;
import de.smasi.tickmate.views.ShowTrackActivity;

public class TickMatrix extends LinearLayout implements OnCheckedChangeListener {
	
	ScrollView sv = null;
	
	public TickMatrix(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.setOrientation(VERTICAL);
		int rows=14;
				
		TracksDataSource ds = new TracksDataSource(context);
		ds.open();
		List<Track> tracks = ds.getMyTracks();
		ds.retrieveTicks();
		ds.close();
		
		if (tracks.size() == 0) {
			TextView tv = new TextView(context);
			tv.setText(R.string.no_tracks_found);
			tv.setGravity(Gravity.CENTER);
			tv.setPadding(20, 20, 20, 20);
			tv.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
			tv.setTextColor(context.getResources().getColor(android.R.color.secondary_text_dark));
			setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
			this.addView(tv);
			return;			
		}
		
		LinearLayout headertop = new LinearLayout(getContext());
		headertop.setOrientation(LinearLayout.HORIZONTAL);
		
		LinearLayout headerrow = new LinearLayout(getContext());
		headerrow.setOrientation(LinearLayout.HORIZONTAL);
	
		TextView b2 = new TextView(context);
		b2.setText("");
		
		b2.setPadding(0, 0, 0, 0);
		b2.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, 0.8f));
		
		//headertop.addView(b2);
		for (Track track : tracks) {
			TrackButton b = new TrackButton(context, track);
			/*b.setText(track.getName());
			b.setWidth(80); 
			b.setImageResource(track.getIconId(context));
			b.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
					
					Intent intent = new Intent(getContext(), ShowTrackActivity.class);
					intent.putExtra("track_id", track.getId());
					startActivityForResult(intent, 1);
					
				}
			});
			*/
			/*
			b.setMaxWidth(32);
			b.setMinimumWidth(32);
			b.setMaxHeight(32);
			b.setMinimumHeight(32);
			
			b.setPadding(32, 32, 32, 32); */
			b.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, (1.0f)/tracks.size()));
			//b.setPadding(0, 0, 0, 0);
			headerrow.addView(b);
		}
		headerrow.setWeightSum(1.0f);
		headerrow.setPadding(0, 5, 10, 5);
		headerrow.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, 90, 0.2f));
		
		headertop.addView(b2);
		headertop.addView(headerrow);
		headertop.setWeightSum(1.0f);
		
		
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
				
		Calendar today = (Calendar)cal.clone();
		Calendar yday = (Calendar)cal.clone();
		yday.add(Calendar.DATE, -1);
		cal.add(Calendar.DATE, -rows);
		java.text.DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(context);
		
		LinearLayout tickgrid = new LinearLayout(getContext());
		tickgrid.setOrientation(LinearLayout.VERTICAL);
		/* TODO find correct color */
		/*
		tickgrid.setBackgroundResource(android.R.color.holo_blue_dark);
		setBackgroundResource(android.R.color.background_dark);
		
		tickgrid.setBackgroundColor(0xff111111);
		tickgrid.setVerticalGravity(Gravity.CENTER);
		tickgrid.setHorizontalGravity(Gravity.LEFT);
		*/
		for (int y=0; y < rows; y++) {
			cal.add(Calendar.DATE, 1);
			Date date = cal.getTime();
			String s = dateFormat.format(date);
			
			TextView t_weekday = new TextView(getContext());
			TextView t_date = new TextView(getContext());
			
			if (cal.compareTo(today) >= 0)
				t_date.setText(context.getString(R.string.today));
			else if (cal.compareTo(yday) >= 0)
				t_date.setText(context.getString(R.string.yesterday));
			else
				t_date.setText(s);
			
			if (cal.get(Calendar.DAY_OF_WEEK) == 2) {
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
			String day_name=cal.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, Locale.getDefault());
			t_weekday.setText(day_name.toUpperCase());
			
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
			
			l.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, 0.8f));
			l.setGravity(Gravity.CENTER_VERTICAL);
			//l2.addView(l);			
			
			
			LinearLayout l2 = new LinearLayout(getContext());
			l2.setOrientation(LinearLayout.HORIZONTAL);
			for (Track track : tracks) {
				TickButton checker = new TickButton(getContext(), track, (Calendar) cal.clone());
				checker.setChecked(ds.isTicked(track, (Calendar)cal.clone()));
				checker.setOnCheckedChangeListener(this);
				//checker.setLayoutParams(new LayoutParams(32, 32, 0.2f));
				Button b = new Button(getContext());
				//checker.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, (1.0f-0.2f)/tracks.size()));
				//checker.setLayoutParams(new LayoutParams(0,0, (1.0f-0.2f)/tracks.size()));
				checker.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT, (1.0f)/tracks.size()));
				l2.addView(checker);
			}
			l2.setWeightSum(1.0f);
			l2.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, 90, 0.2f));
			//row.addView(l2);

			row.addView(l);
			row.addView(l2);
			row.setGravity(Gravity.CENTER);
			
			if (y == rows - 1) { // cal.get(Calendar.DAY_OF_WEEK) == 1 || cal.get(Calendar.DAY_OF_WEEK) == 7) {
				row.setBackgroundResource(android.R.drawable. dark_header);
				row.setPadding(0, 0, 0, 0);
			}
			
			
			tickgrid.addView(row);	
		}
		
		
		tickgrid.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		
		tickgrid.setPadding(10, 0, 10, 5);
		headertop.setPadding(10, 0, 10, 0);
		headertop.setBackgroundResource(R.drawable.bottom_line);
		sv = new ScrollView(getContext());
		sv.addView(tickgrid);
		addView(headertop);
		addView(sv);
	}
	
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		// TODO Auto-generated method stub
		if (sv != null) {
			sv.fullScroll(View.FOCUS_DOWN);
		}
		super.onLayout(changed, l, t, r, b);
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
		//Log.v("Tickmatrix", "C " + arg0.getId() +": "+  ticked + " in " + tb.getTrack().getName());
		TracksDataSource ds = new TracksDataSource(this.getContext());
		ds.open();
		if (ticked) {
			ds.setTick(tb.getTrack(), tb.getDate());
		}
		else {
			ds.removeTick(tb.getTrack(), tb.getDate());
		}
		ds.close();		
	}


}
