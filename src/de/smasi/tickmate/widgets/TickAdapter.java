package de.smasi.tickmate.widgets;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import de.smasi.tickmate.R;
import de.smasi.tickmate.views.ShowTrackActivity;
import de.smasi.tickmate.widgets.MultiTickButton;
import de.smasi.tickmate.widgets.TickButton;
import de.smasi.tickmate.widgets.TrackButton;
import de.smasi.tickmate.database.TracksDataSource;
import de.smasi.tickmate.models.Track;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;

public class TickAdapter extends BaseAdapter {

	private final Context context;
	private Calendar startday, today, yday;
	int count, count_ahead;
	  
	public TickAdapter(Context context, Calendar startday) {
		//super(context, R.layout.rowlayout, days);
		this.context = context;
		//this.values = days;
		this.count = 14; // by default load 2 weeks
		this.count_ahead = 0; // by default show zero days ahead

		today = Calendar.getInstance();
		today.set(Calendar.HOUR, 0);
		today.set(Calendar.MINUTE, 0);
		today.set(Calendar.SECOND, 0);
		today.set(Calendar.MILLISECOND, 0);
		
		yday = (Calendar)today.clone();
		yday.add(Calendar.DATE, -1);

		setDate(startday);
	}
	
	public void setDate(Calendar startday) {
		startday.set(Calendar.HOUR, 0);
		startday.set(Calendar.MINUTE, 0);
		startday.set(Calendar.SECOND, 0);
		startday.set(Calendar.MILLISECOND, 0);
		this.startday = startday;
	}
	
	public Calendar getDate() {
		return this.startday;
	}

    public void addCount(int num) {
        this.count += num;
        notifyDataSetChanged();
    }
	
    public int getCount() {
        return this.count; //values.size();
    }

    public Object getItem(int position) {
        return getCount() - position;
    }

    public long getItemId(int position) {
        return position;
    }
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		java.text.DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(context);

		Integer days = (Integer) getItem(position);
		Calendar thisday = (Calendar)startday.clone();
		thisday.add(Calendar.DATE, -days);
		Button tv = new Button(this.context);
		tv.setText("muahah " + getItem(position) + " ; " + dateFormat.format(thisday.getTime()));
		//.getTime()) + " - " + dateFormat.format(endday.getTime()));
		
		//TextView tv = new TextView(this.context);
		//tv.setText("muahah" + Integer.toString(position));
		/*
		Log.d("TICKI", "REDRAW position=" + Integer.toString(position) +
				" must_set="+ Boolean.toString(convertView==null));
		return buildGrid(position, values.get(position).getStartDay(), values.get(position).getEndDay());
		*/
		//return tv;
		return buildRow(thisday);
		/*	if (convertView == null) {
			return buildGrid(position, values.get(position).getStartDay(), values.get(position).getEndDay());
		}
		else {
			return convertView;
		}*/
	}
	

	public View getHeader() {

		TracksDataSource ds = new TracksDataSource(context);
		List<Track> tracks = ds.getActiveTracks();
		int rowHeight = -1;	

		LinearLayout headertop = new LinearLayout(this.context);
		headertop.setOrientation(LinearLayout.HORIZONTAL);
		
		LinearLayout headerrow = new LinearLayout(this.context);
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
		return headertop;
	}
	
	private View buildGrid(int position, Calendar startday, Calendar endday) {

		java.text.DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(context);

		Button tv = new Button(this.context);
		tv.setText("muahah " + Integer.toString(position) + " ; " + dateFormat.format(startday.getTime()) + " - " + dateFormat.format(endday.getTime()));
		
		return tv;
	}
		
	public View buildRow(Calendar startday) {
		int rows = 1;
		int rowHeight = -1;	
		java.text.DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(context);

		Calendar endday = (Calendar) startday.clone();
		endday.add(Calendar.DATE, 1);

		Context context = this.context;
		Locale locale = Locale.getDefault();

		TracksDataSource ds = new TracksDataSource(context);
		List<Track> tracks = ds.getActiveTracks();
		ds.open();
		// This will be the Calendar object will use for iteration
		Calendar cal = (Calendar)startday.clone();
		
		// Limit ticks to range [startday, endday]
		ds.retrieveTicks(startday, startday);		
		ds.close();
		LinearLayout tickgrid = new LinearLayout(this.context);
		tickgrid.setOrientation(LinearLayout.VERTICAL);


			Date date = cal.getTime();
			String s = dateFormat.format(date);
			
			TextView t_weekday = new TextView(this.context);
			TextView t_date = new TextView(this.context);
			
			if (cal.compareTo(today) == 0)
				t_date.setText(context.getString(R.string.today));
			else if (cal.compareTo(yday) == 0)
				t_date.setText(context.getString(R.string.yesterday));
			else
				t_date.setText(s);
			
			// add splitter for first weekday depending on current locale
			if (cal.get(Calendar.DAY_OF_WEEK) == cal.getFirstDayOfWeek()) {
				TextView splitter2 = new TextView(this.context);
				splitter2.setText("");
				splitter2.setHeight(5);
				tickgrid.addView(splitter2);
				TextView splitter = new TextView(this.context);
				splitter.setText("");
				splitter.setHeight(11);
				splitter.setBackgroundResource(R.drawable.center_line);
				splitter.setPadding(0, 20, 0, 0);
				tickgrid.addView(splitter);
			}			
			
			String day_name=cal.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, locale);
			t_weekday.setText(day_name.toUpperCase(locale));
			
			t_weekday.setTextAppearance(this.context, android.R.style.TextAppearance_Medium);
			t_date.setWidth(120);
			t_date.setTextAppearance(this.context, android.R.style.TextAppearance_Small);
			t_date.setTextSize((float) 11.0);
			t_date.setTextColor(Color.GRAY);
			t_weekday.setWidth(120);
			LinearLayout row = new LinearLayout(this.context);
			row.setOrientation(LinearLayout.HORIZONTAL);
			LinearLayout l = new LinearLayout(this.context);
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
					
			LinearLayout l2 = new LinearLayout(this.context);
			l2.setOrientation(LinearLayout.HORIZONTAL);
			for (Track track : tracks) {
				
				if (track.multipleEntriesEnabled()) {
					MultiTickButton counter = new MultiTickButton(this.context, track, (Calendar) cal.clone());
					//counter.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT, (1.0f)/tracks.size()));
					counter.setTickCount(ds.getTickCountForDay(track, cal));
					l2.addView(counter);
				} else {
					TickButton checker = new TickButton(this.context, track, (Calendar) cal.clone());
					checker.setChecked(ds.isTicked(track, (Calendar) cal.clone(), false));
					//checker.setLayoutParams(new LayoutParams(32, 32, 0.2f));
					//checker.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, (1.0f-0.2f)/tracks.size()));
					//checker.setLayoutParams(new LayoutParams(0,0, (1.0f-0.2f)/tracks.size()));
					//checker.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT, (1.0f)/tracks.size()));
					l2.addView(checker);
				}
				
			}
			l2.setWeightSum(1.0f);
			l2.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, rowHeight, 0.2f));

			row.addView(l);
			row.addView(l2);
			row.setGravity(Gravity.CENTER);
			
			if (cal.compareTo(this.startday) == 0) {
				row.setBackgroundResource(android.R.drawable.dark_header);
				row.setPadding(0, 0, 0, 0);
			}
			
			tickgrid.addView(row);	
	
		
		//tickgrid.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		tickgrid.setPadding(10, 0, 10, 5);
		return tickgrid;
				
	}

}
