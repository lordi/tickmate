package de.smasi.tickmate.widgets;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import de.smasi.tickmate.R;
import de.smasi.tickmate.widgets.MultiTickButton;
import de.smasi.tickmate.widgets.TickButton;
import de.smasi.tickmate.database.TracksDataSource;
import de.smasi.tickmate.models.DayRange;
import de.smasi.tickmate.models.Track;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CursorAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;

public class DayRangeAdapter extends ArrayAdapter<DayRange> {

	private List<DayRange> values;
	private final Context context;
	  
	public DayRangeAdapter(Context context, List<DayRange> days) {
		super(context, R.layout.rowlayout, days);
		this.context = context;
		this.values = days;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		TextView tv = new TextView(this.context);
		tv.setText("muahah" + Integer.toString(position));
		return buildGrid(values.get(position).startday, values.get(position).endday);
	}
	
	private LinearLayout buildGrid(Calendar startday, Calendar endday) {
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
		List<Track> tracks = ds.getActiveTracks();
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
							//REDOcounter.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT, (1.0f)/tracks.size()));
							counter.setTickCount(ds.getTickCountForDay(track, cal));
							l2.addView(counter);
						} else {
							TickButton checker = new TickButton(getContext(), track, (Calendar) cal.clone());
							checker.setChecked(ds.isTicked(track, (Calendar) cal.clone(), false));
							//REDO checker.setOnCheckedChangeListener(this);
							//checker.setLayoutParams(new LayoutParams(32, 32, 0.2f));
							//checker.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, (1.0f-0.2f)/tracks.size()));
							//checker.setLayoutParams(new LayoutParams(0,0, (1.0f-0.2f)/tracks.size()));
							//REDO checker.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT, (1.0f)/tracks.size()));
							l2.addView(checker);
						}
						
					}
					l2.setWeightSum(1.0f);
					l2.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, rowHeight, 0.2f));

					row.addView(l);
					row.addView(l2);
					row.setGravity(Gravity.CENTER);
					/*
					if (cal.compareTo(displayDay) == 0) {
						row.setBackgroundResource(android.R.drawable.dark_header);
						row.setPadding(0, 0, 0, 0);
					}
					*/
					
					tickgrid.addView(row);	
				}
				
				
				//tickgrid.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
				tickgrid.setPadding(10, 0, 10, 5);
				return tickgrid;
				
	}

}
