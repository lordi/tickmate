package de.smasi.tickmate;

import android.app.ListActivity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import de.smasi.tickmate.database.DataSource;
import de.smasi.tickmate.models.Group;
import de.smasi.tickmate.models.Track;
import de.smasi.tickmate.widgets.MultiTickButton;
import de.smasi.tickmate.widgets.TickButton;

import java.text.SimpleDateFormat;
import java.util.*;

public class TickAdapter extends BaseAdapter {

    private final Context context;
    private Calendar activeDay;  // When set, the display will be fixed to this day.
    // Null value is intentionally used to indicate display should follow the actual current day.
    private Calendar today, yday;
    int count, count_ahead;
    private Map<Calendar, View> mRowCache = new HashMap<>();

    private List<Track> mTracksCurrentlyDisplayed; // Determined by group selector

    private boolean isTodayAtTop = false;  // Reverses the date ordering - most recent dates at the top
    private static final String TAG = "TickAdapter";
    private static final int DEFAULT_COUNT_PAST = 21;
    private static final int DEFAULT_COUNT_AHEAD = 0;

    private int mCurrentGroupId;

    public TickAdapter(Context context, Calendar activeDay, Bundle restoreStateBundle) {
        // super(context, R.layout.rowlayout, days);
        this.context = context;
        this.count = DEFAULT_COUNT_PAST;
        this.count_ahead = DEFAULT_COUNT_AHEAD;

        setActiveDay(activeDay);
        isTodayAtTop = PreferenceManager.getDefaultSharedPreferences(context).
            getBoolean("reverse-date-order-key", false);
    }

    public void unsetActiveDay() {
        setActiveDay(null);
    }

    private void updateToday() {
        today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);


        yday = (Calendar) today.clone();
        yday.add(Calendar.DATE, -1);
    }

    public void setActiveDay(Calendar activeDay) {
        updateToday();

        // null is used to indicate 'not set'
        if (activeDay != null) {
            java.text.DateFormat dateFormat = android.text.format.DateFormat
                .getDateFormat(context);
            Log.d(TAG, "Active day set to " + dateFormat.format(activeDay.getTime()));
            activeDay.set(Calendar.HOUR_OF_DAY, 0);
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
            return (Calendar) this.today.clone();
        } else {
            return (Calendar) this.activeDay.clone();
        }
    }

    public void addCount(int num) {
        this.count += num;
        notifyDataSetChanged();
    }

    public int getCount() {
        // Return either 0 or this.count, depending on whether the empty view should be displayed.
        // Empty view is displayed if (a) no active tracks exist (b) a group is selected, and there
        // are no tracks for that group

        DataSource ds = DataSource.getInstance();

        if (ds.getActiveTracks().size() == 0) {
            return 0;
        }

        if ((mCurrentGroupId != Group.ALL_GROUP.getId())
            && (ds.getTracksForGroup(mCurrentGroupId).size() == 0)) {
            return 0;
        }

        return this.count;
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

    public void setCurrentGroup(int groupId) {
        mCurrentGroupId = groupId;
        notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        //        Log.d(TAG, "getView(" + position + ", " + convertView + ", " + parent);

        Integer days = (Integer) getItem(position);
        Calendar rowDay = getActiveDay();
        rowDay.add(Calendar.DATE, -days);

        View v;
        if (mRowCache.containsKey(rowDay)) {
            v = mRowCache.get(rowDay);
        } else {
            v = buildRow(rowDay);
            mRowCache.put(rowDay, v);
        }

        return v;
    }

    /**
     * Used to create and insert the week separator
     *
     * @param tickGrid the ViewGroup into which the week separator will be inserted
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
        java.text.DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(context);
        String dateFormatString = PreferenceManager.getDefaultSharedPreferences(context).
            getString("date_format", "");
        if(!dateFormatString.isEmpty()) {
            try{
                dateFormat = new SimpleDateFormat(dateFormatString, locale);
            } catch (IllegalArgumentException e) {
                Log.w(TAG, "Error parsing dateFormat: " + dateFormat + e.getMessage());
            }
        }

        //Log.v(TAG, "Inflating row " + dateFormat.format(cal.getTime()));

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
        for (Track track : mTracksCurrentlyDisplayed) {

            if (track.multipleEntriesEnabled()) {
                MultiTickButton counter = new MultiTickButton(this.context,
                    track, (Calendar) cal.clone());
                counter.setLayoutParams(new LayoutParams(
                    LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT,
                    (1.0f) / mTracksCurrentlyDisplayed.size()));
                l2.addView(counter);
            } else {
                TickButton checker = new TickButton(this.context, track, cal);
                checker.setLayoutParams(new LayoutParams(
                    LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT,
                    (1.0f) / mTracksCurrentlyDisplayed.size()));
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

        // Today may have changed, for instance when the App was running in the background
        // for a while and then resumed. So, update today!
        updateToday();

        mRowCache.clear();

        boolean previousIsTodayAtTop = isTodayAtTop;  // Used to determine if this value has been toggled since last data set change

        isTodayAtTop = PreferenceManager.getDefaultSharedPreferences(context).
            getBoolean("reverse-date-order-key", false);

        if (isTodayAtTop != previousIsTodayAtTop) {
            scrollToLatest();
        }

        DataSource ds = DataSource.getInstance();

        Calendar startday = this.getActiveDay();
        Calendar endday = (Calendar) startday.clone();
        startday.add(Calendar.DATE, -this.count);

        mTracksCurrentlyDisplayed = getTracksForCurrentGroup();
//            Log.d(TAG, "Tracks associated with current group (" + getCurrentGroupId() +
//                    ") are: (" + TextUtils.join(",", mTracksCurrentlyDisplayed) + ")");

        Log.v(TAG, "Data range has been updated: " + dateFormat.format(startday.getTime()) + " - " + dateFormat.format(today.getTime()));
        ds.retrieveTicks(startday, endday);

//        Log.d(TAG, "Tracks currently displayed: " + TextUtils.join("\n ", mTracksCurrentlyDisplayed));
//        for (Track t : mTracksCurrentlyDisplayed) { Log.d(TAG, t.getName()); }

        super.notifyDataSetChanged();
    }

    // TODO this should really live in a cache (also, it's duplicated in TickHeader)
    private List<Track> getTracksForCurrentGroup() {
        if (mCurrentGroupId == Group.ALL_GROUP.getId()) {
            return DataSource.getInstance().getActiveTracks();
        } else {
            return DataSource.getInstance().getTracksForGroup(mCurrentGroupId);
        }
    }
}