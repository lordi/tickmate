package de.smasi.tickmate;

import android.app.ListActivity;
import android.content.Context;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import de.smasi.tickmate.database.TracksDataSource;
import de.smasi.tickmate.models.Track;
import de.smasi.tickmate.widgets.MultiTickButton;
import de.smasi.tickmate.widgets.TickButton;
import de.smasi.tickmate.widgets.TrackButton;

public class TickAdapter extends BaseAdapter {

	private final Context context;
	private Calendar activeDay;  // When set, the display will be fixed to this day.
	    // Null value is intentionally used to indicate display should follow the actual current day.
    private Calendar today, yday;
	int count, count_ahead;
	private TracksDataSource ds;
    private List<Track> tracks;
    private Map<Calendar, View> mRowCache = new HashMap<>();

    private boolean isTodayAtTop = false;  // Reverses the date ordering - most recent dates at the top
    private static final String TAG = "TickAdapter";
    private static final int DEFAULT_COUNT_PAST = 21; // by default load 3 weeks of past ticks
    // (see comment by InfiniteScrollAdapter.SCROLL_DOWN_THRESHOLD)
    private static final int DEFAULT_COUNT_AHEAD = 0; // by default show zero days ahead

	public TickAdapter(Context context, Calendar activeDay) {
		// super(context, R.layout.rowlayout, days);
		this.context = context;
		this.count = DEFAULT_COUNT_PAST;
		this.count_ahead = DEFAULT_COUNT_AHEAD;

		// Initialize data source
		ds = new TracksDataSource(context);

        setActiveDay(activeDay);
        isTodayAtTop = PreferenceManager.getDefaultSharedPreferences(context).
                getBoolean("reverse-date-order-key", false);
	}

    public void unsetActiveDay() {
        setActiveDay(null);
    }

    private void updateToday() {
        today = Calendar.getInstance();
        today.set(Calendar.HOUR, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);
    }

	public void setActiveDay(Calendar activeDay) {
        updateToday();

		yday = (Calendar) today.clone();
		yday.add(Calendar.DATE, -1);

        // null is used to indicate 'not set'
		if (activeDay != null) {
			java.text.DateFormat dateFormat = android.text.format.DateFormat
					.getDateFormat(context);
			Log.d(TAG, "Active day set to " + dateFormat.format(activeDay.getTime()));
			activeDay.set(Calendar.HOUR, 0);
			activeDay.set(Calendar.MINUTE, 0);
			activeDay.set(Calendar.SECOND, 0);
			activeDay.set(Calendar.MILLISECOND, 0);
		}
		
		this.activeDay = activeDay;
        Log.d(TAG, "Active day set to " + activeDay);

        notifyDataSetChanged();
	}


    public Calendar getActiveDay() {
		if (this.activeDay == null) {
            updateToday();
			return (Calendar) this.today.clone();  // TODO remove redundant cloning elsewhere, when this method is called
		}
        else {
            return this.activeDay;
        }
    }


	public void addCount(int num) {
		this.count += num;
		notifyDataSetChanged();
	}

	public int getCount() {
		if (tracks.size() == 0) {
			return 0; // return 0 here if we have no tracks so that the empty view will get displayed
		}
		else {
			return this.count;
		}
	}

	public Object getItem(int position) {
        if (isTodayAtTop) {
            return position;
        } else {
            return getCount() - position;
        }
    }

	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Integer days = (Integer) getItem(position);
		Calendar rowDay = (Calendar) getActiveDay().clone();
		rowDay.add(Calendar.DATE, -days);

		View v;
		if (mRowCache.containsKey(rowDay)) {
			v = mRowCache.get(rowDay);
		}
		else {
			v = buildRow(rowDay);
			mRowCache.put(rowDay, v);
		}

		return v;
	}

	public View getHeader() {

		int rowHeight = -1;

		LinearLayout headertop = new LinearLayout(this.context);
		headertop.setOrientation(LinearLayout.HORIZONTAL);

		LinearLayout headerrow = new LinearLayout(this.context);
		headerrow.setOrientation(LinearLayout.HORIZONTAL);
		TextView b2 = new TextView(context);
		b2.setText("");

		b2.setPadding(0, 0, 0, 0);
		b2.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
				rowHeight, 0.8f));

		for (Track track : tracks) {
			TrackButton b = new TrackButton(context, track);

			b.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
					LayoutParams.MATCH_PARENT, (1.0f) / tracks.size()));
			headerrow.addView(b);
		}
		headerrow.setWeightSum(1.0f);
		headerrow.setPadding(5, 5, 10, 5);
		headerrow.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
				rowHeight, 0.2f));

		headertop.addView(b2);
		headertop.addView(headerrow);
		headertop.setWeightSum(1.0f);
		headertop.setPadding(10, 0, 10, 0);
		headertop.setBackgroundResource(R.drawable.bottom_line);
		return headertop;
	}

	/**
	 * Used to create and insert the week separator
	 *  @param tickGrid the ViewGroup into which the week separator will be inserted
	 */
	private void addStartWeekSeparator(ViewGroup tickGrid) {
		TextView splitter2 = new TextView(this.context);
		splitter2.setText("");
		splitter2.setHeight(5);
		tickGrid.addView(splitter2);
		TextView splitter = new TextView(this.context);
		splitter.setText("");
		splitter.setHeight(11);
		splitter.setBackgroundResource(R.drawable.center_line);
		splitter.setPadding(0, 20, 0, 0);
		tickGrid.addView(splitter);
	}

	public View buildRow(Calendar cal) {
		Locale locale = Locale.getDefault();
		Date date = cal.getTime();
		java.text.DateFormat dateFormat = android.text.format.DateFormat
				.getDateFormat(context);

		Log.v(TAG, "Inflating row " + dateFormat.format(cal.getTime()));

		LinearLayout tickgrid = new LinearLayout(this.context);
		tickgrid.setOrientation(LinearLayout.VERTICAL);

		String s = dateFormat.format(date);

		TextView t_weekday = new TextView(this.context);
		TextView t_date = new TextView(this.context);

		if (cal.compareTo(today) == 0)
			t_date.setText(context.getString(R.string.today));
		else if (cal.compareTo(yday) == 0)
			t_date.setText(context.getString(R.string.yesterday));
		else
			t_date.setText(s);

		// If the date order has not been reversed, then add the splitter above the first day of the week
		//  splitter for first weekday depends on current locale
		if (!isTodayAtTop &&  ( cal.get(Calendar.DAY_OF_WEEK) == cal.getFirstDayOfWeek())) {
			addStartWeekSeparator(tickgrid);
		}

		String day_name = cal.getDisplayName(Calendar.DAY_OF_WEEK,
				Calendar.SHORT, locale);
		t_weekday.setText(day_name.toUpperCase(locale));

		t_weekday.setTextAppearance(this.context,
				android.R.style.TextAppearance_Medium);
		t_date.setWidth(120);
		t_date.setTextAppearance(this.context,
				android.R.style.TextAppearance_Small);
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
		// float scaledDensity = context.getResources().getDisplayMetrics().scaledDensity;
		// int densityDpi = context.getResources().getDisplayMetrics().densityDpi;
		// Log.d("tickmate", t_weekday.getTextSize() + "|" +
		// t_date.getTextSize() + "|" + scaledDensity + "|" + densityDpi);
		// Small screen, normal font 27.0|16.5|1.5|240
		// Small screen, huge font 35.0|21.449999|1.9499999|240
		// Huge screen, normal font 24.0|14.643751|1.3312501|213
		// Huge screen, huge font 31.0|19.036875|1.730625|213

		int rowHeight = (int) (t_weekday.getTextSize() + t_date.getTextSize()) + 40;

		l.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
				rowHeight, 0.8f));
		l.setGravity(Gravity.CENTER_VERTICAL);

		LinearLayout l2 = new LinearLayout(this.context);
		l2.setOrientation(LinearLayout.HORIZONTAL);
		for (Track track : tracks) {

			if (track.multipleEntriesEnabled()) {
				MultiTickButton counter = new MultiTickButton(this.context,
						track, (Calendar) cal.clone(), ds);
				counter.setLayoutParams(new LayoutParams(
						LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT,
						(1.0f) / tracks.size()));
				l2.addView(counter);
			} else {
				TickButton checker = new TickButton(this.context, track, cal, ds);
				checker.setLayoutParams(new LayoutParams(
						LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT,
						(1.0f) / tracks.size()));
				l2.addView(checker);
			}

		}
		l2.setWeightSum(1.0f);
		l2.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
				rowHeight, 0.2f));

		row.addView(l);
		row.addView(l2);
		row.setGravity(Gravity.CENTER);

		if (cal.compareTo(this.getActiveDay()) == 0) {
			row.setBackgroundResource(android.R.drawable.dark_header);
			row.setPadding(0, 0, 0, 0);
		}

		tickgrid.addView(row);
		tickgrid.setPadding(10, 0, 10, 5);

		// With the date order reversed, add the splitter below the first day of the week
		//  splitter for first weekday depends on current locale
		if (isTodayAtTop &&  ( cal.get(Calendar.DAY_OF_WEEK) == cal.getFirstDayOfWeek())) {
			addStartWeekSeparator(tickgrid);
		}

		return tickgrid;
	}

    // Used for Jump To [Date|Today], and used when the toggling isTodayAtTop
    public void scrollToLatest() {
        int scrollposition = (isTodayAtTop) ? 0 : this.count - 1;
        ((ListActivity) context).getListView().smoothScrollToPositionFromTop(scrollposition, 1, 0);
    }

	@Override
	public void notifyDataSetChanged() {
		java.text.DateFormat dateFormat = android.text.format.DateFormat
				.getDateFormat(context);
		super.notifyDataSetChanged();

        mRowCache.clear();

        boolean previousIsTodayAtTop = isTodayAtTop;  // Used to determine if this value has been toggled since last data set change
        isTodayAtTop = PreferenceManager.getDefaultSharedPreferences(context).
                getBoolean("reverse-date-order-key", false);

        if (isTodayAtTop != previousIsTodayAtTop) {
            scrollToLatest();
        }

        ds.open();
		tracks = ds.getActiveTracks();

		Calendar startday = (Calendar)this.getActiveDay().clone();
		Calendar endday = (Calendar)startday.clone();
		startday.add(Calendar.DATE, -this.count);

		Log.v(TAG, "Data range has been updated: " + dateFormat.format(startday.getTime()) + " - " + dateFormat.format(endday.getTime()));

		// Limit ticks to range [activeDay, endday]
		ds.retrieveTicks(startday, endday);
		ds.close();
	}

}
