package de.smasi.tickmate;

import android.app.ListActivity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import de.smasi.tickmate.database.DataSource;
import de.smasi.tickmate.models.Group;
import de.smasi.tickmate.models.Track;
import de.smasi.tickmate.widgets.MultiTickButton;
import de.smasi.tickmate.widgets.TickButton;
import de.smasi.tickmate.widgets.TrackButton;

public class TickAdapter extends BaseAdapter implements AdapterView.OnItemSelectedListener, View.OnTouchListener {

	private final Context context;
    private Calendar activeDay;  // When set, the display will be fixed to this day.
    // Null value is intentionally used to indicate display should follow the actual current day.
    private Calendar today, yday;
    int count, count_ahead;
    private Map<Calendar, View> mRowCache = new HashMap<>();


    private List<Track> mTracksCurrentlyDisplayed; // All of the active tracks which should be currently visible (determined by group selector)
    private Spinner mGroupSpinner;
    private ArrayList<Integer> mSpinnerArrayGroupIds = new ArrayList<>();
    private int mSpinnerPosition = 0;  // Can we get rid of this, and simply update the value within the spinner itself?

    private boolean isTodayAtTop = false;  // Reverses the date ordering - most recent dates at the top
    private static final String TAG = "TickAdapter";
    private static final int DEFAULT_COUNT_PAST = 21; // by default load 3 weeks of past ticks
    // (see comment by InfiniteScrollAdapter.SCROLL_DOWN_THRESHOLD)
    private static final int DEFAULT_COUNT_AHEAD = 0; // by default show zero days ahead
    private final static int ALL_GROUPS_SPINNER_INDEX = 0; // Position within the group selector
        // Spinner which indicates that 'all groups' have been selected.  (Other positions indicate
        // a specific group has been selected)

    private GestureDetector mGestureDetector;

	public TickAdapter(Context context, Calendar activeDay, Bundle restoreStateBundle) {
		// super(context, R.layout.rowlayout, days);
		this.context = context;
		this.count = DEFAULT_COUNT_PAST;
		this.count_ahead = DEFAULT_COUNT_AHEAD;

        restoreState(restoreStateBundle); // Sequence is critical in this stanza
        initSpinnerArrayGroupIds();
        setActiveDay(activeDay);
        isTodayAtTop = PreferenceManager.getDefaultSharedPreferences(context).
                getBoolean("reverse-date-order-key", false);
        mGestureDetector = new GestureDetector(context, new GestureListener());
//        Log.d(TAG, "mSpinnerPosition: " + mSpinnerPosition);
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
            return (Calendar) this.today.clone();  // Prevents need to clone the returned value
        } else {
            return (Calendar) this.activeDay.clone();
        }
    }

    public void addCount(int num) {
        this.count += num;
        notifyDataSetChanged();
    }

    private boolean isAllGroupSelected() {
        if (mGroupSpinner == null) {
            Log.e(TAG, "mGroupSpinner is null in isAllGroupSelected. Returning: " +
                    (mSpinnerPosition == ALL_GROUPS_SPINNER_INDEX));
            return (mSpinnerPosition == ALL_GROUPS_SPINNER_INDEX);
        }
        return (mGroupSpinner.getSelectedItemPosition() == ALL_GROUPS_SPINNER_INDEX);
    }


    public int getCount() {
        // Return either 0 or this.count, depending on whether the empty view should be displayed.
        // Empty view is displayed if (a) no active tracks exist (b) a group is selected, and there
        // are no tracks for that group

        DataSource ds = DataSource.getInstance();

        if (ds.getActiveTracks().size() == 0) {
            return 0;
        }

        if (!isAllGroupSelected() && ds.getTracksForGroup(getCurrentGroupId()).size() == 0) {
            return 0;
        }

        return this.count;

        // TODO Should we make further changes to the empty view? Currently adapts to whether
        //  the selected group has tracks linked to it.  (TODO check that it handles the situation
        //  correctly if there are no tracks at all.)
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


    // Also initializes the array lists associated with the group spinner
    // Called only by getHeader - extracted to a separate method to improve readability
    private void initializeGroupSpinner(int rowHeight) {
        mGroupSpinner = new Spinner(context);
        mGroupSpinner.setPadding(0, 0, 0, 0);

        mGroupSpinner.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
                rowHeight, 0.8f));

        // init mSpinnerArraynames
        List<Group> allGroups = DataSource.getInstance().getGroups();
        ArrayList<String> mSpinnerArrayNames = new ArrayList<>();
        mSpinnerArrayNames.add(Group.ALL_GROUP.getName());
        for (Group group : allGroups) {
            mSpinnerArrayNames.add(group.getName());
        }

        initSpinnerArrayGroupIds();

        ArrayAdapter<String> spinnerArrayAdapter =
                new ArrayAdapter<>(context, android.R.layout.simple_spinner_dropdown_item, mSpinnerArrayNames);
        mGroupSpinner.setAdapter(spinnerArrayAdapter);
        mGroupSpinner.setOnItemSelectedListener(this);
        // Protect against errant values before calling setSelection
         if ((mSpinnerPosition < 0) || (mSpinnerPosition >= mGroupSpinner.getCount())) {
             Log.e(TAG, "mSpinnerPosition should not be "+mSpinnerPosition);
             mSpinnerPosition = 0;
        }
        mGroupSpinner.setSelection(mSpinnerPosition); // AVP:Monday TODO Test for the permanence of this selector state.
    }

    private void initSpinnerArrayGroupIds() {
        List<Group> allGroups = DataSource.getInstance().getGroups();
        mSpinnerArrayGroupIds.clear();
        mSpinnerArrayGroupIds.add(Group.ALL_GROUP.getId()); // The first entry in this array refers to the 'All Group', which does not occur in the database.
        for (Group group : allGroups) {
            mSpinnerArrayGroupIds.add(group.getId());
        }
    }


    public View getHeader() {
//        Log.d(TAG, "calling getHeader");
        int rowHeight = -1;

        // trackHeader will contain the track icons, while header will contain both the spinner and trackHeader
        LinearLayout header = new LinearLayout(this.context);
        header.setOrientation(LinearLayout.VERTICAL);
        header.setOnTouchListener(this); // Hack to ensure swipes are intercepted; improve & remove

        LinearLayout trackHeader = new LinearLayout(this.context);
        trackHeader.setOrientation(LinearLayout.HORIZONTAL);
        trackHeader.setOnTouchListener(this); // Hack to ensure swipes are intercepted; improve & remove

        LinearLayout headerrow = new LinearLayout(this.context);
        headerrow.setOrientation(LinearLayout.HORIZONTAL);
        headerrow.setOnTouchListener(this); // Hack to ensure swipes are intercepted; improve & remove

        TextView b2 = new TextView(context);
        b2.setOnTouchListener(this); // Hack to ensure swipes are intercepted; improve & remove
        b2.setText("");

        b2.setPadding(0, 0, 0, 0);
        b2.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
                rowHeight, 0.8f));

        initializeGroupSpinner(rowHeight);

        for (Track track : mTracksCurrentlyDisplayed) {
            TrackButton b = new TrackButton(context, track);
            b.setOnTouchListener(this); // Hack to ensure swipes are intercepted; improve & remove
            b.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
                    LayoutParams.MATCH_PARENT, (1.0f) / mTracksCurrentlyDisplayed.size()));
            headerrow.addView(b);
        }

        headerrow.setWeightSum(1.0f);
        headerrow.setPadding(5, 5, 10, 5);
        headerrow.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
                rowHeight, 0.2f));

        // Consider whether removing the view only to add it again is truly necessary, or if this should be redesigned.
        ViewGroup parent = (ViewGroup) mGroupSpinner.getParent();
        if (parent != null) {
            parent.removeView(mGroupSpinner);
        }

        trackHeader.addView(b2);
        trackHeader.addView(headerrow);
        trackHeader.setWeightSum(1.0f);
        trackHeader.setPadding(10, 0, 10, 0);
        trackHeader.setBackgroundResource(R.drawable.bottom_line);
        if (mGroupSpinner.getCount() > 1) {
            header.addView(mGroupSpinner);
        }
        header.addView(trackHeader);
        return header;
    }

    /**
     * Used to create and insert the week separator
     *
     * @param tickGrid the ViewGroup into which the week separator will be inserted
     */
    private void addStartWeekSeparator(ViewGroup tickGrid) {
        TextView splitter2 = new TextView(this.context);
        splitter2.setOnTouchListener(this); // Hack to ensure swipes are intercepted; improve & remove
        splitter2.setText("");
        splitter2.setHeight(5);
        tickGrid.addView(splitter2);
        TextView splitter = new TextView(this.context);
        splitter.setOnTouchListener(this); // Hack to ensure swipes are intercepted; improve & remove
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

//        Log.v(TAG, "Inflating row " + dateFormat.format(cal.getTime()));

        LinearLayout tickgrid = new LinearLayout(this.context);
//        LinearLayout tickgrid = new TestCustomEventControlLinearLayout(this.context);
//        getListView().getRootView().setOnTouchListener(mAdapter.getAdapter());
        tickgrid.setOnTouchListener(this);  // Hack to ensure swipes are intercepted; improve & remove

        tickgrid.setOrientation(LinearLayout.VERTICAL);

        String s = dateFormat.format(date);

        TextView t_weekday = new TextView(this.context);
        t_weekday.setOnTouchListener(this); // Hack to ensure swipes are intercepted; improve & remove
        TextView t_date = new TextView(this.context);
        t_date.setOnTouchListener(this); // Hack to ensure swipes are intercepted; improve & remove

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
        row.setOnTouchListener(this); // Hack to ensure swipes are intercepted; improve & remove
        row.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout l = new LinearLayout(this.context);
        l.setOnTouchListener(this); // Hack to ensure swipes are intercepted; improve & remove
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
		super.notifyDataSetChanged();

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

        Log.v(TAG, "Data range has been updated: " + dateFormat.format(activeDay.getTime()) + " - " + dateFormat.format(today.getTime()));
        ds.retrieveTicks(startday, endday);

        // Keep around for easier debug
//        Log.d(TAG, "Tracks currently displayed: " + TextUtils.join("\n ", mTracksCurrentlyDisplayed));
//        for (Track t : mTracksCurrentlyDisplayed) { Log.d(TAG, t.getName()); }
    }

    private List<Track> getTracksForCurrentGroup() {
        if (isAllGroupSelected()) {
            return DataSource.getInstance().getActiveTracks();
        } else {
            return DataSource.getInstance().getTracksForGroup(getCurrentGroupId());
        }
    }

    private int getCurrentGroupId() {
        return mSpinnerArrayGroupIds.get(mSpinnerPosition);
    }


    public void onItemSelected(AdapterView<?> parent, View view,
                               int pos, long id) {

        // Unless a new item was selected in the spinner, do nothing.
        if (pos == mSpinnerPosition) {
            Log.d(TAG, "Spinner selection matches previous selection, nothing to do.");
            return;
        }
        mSpinnerPosition = pos;

        mTracksCurrentlyDisplayed = getTracksForCurrentGroup();

        Tickmate tm = (Tickmate) context;

        tm.refresh();
//        Log.d(TAG, " item selected:  pos(" + mSpinnerPosition + "), groupID(" + currentGroupId + ")");
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

        // Consider: Confirm that doing nothing is truly the best choice here. Leaves previous mDisplayGroupName the same.
    }


    // Preserve the spinner state
    public void restoreState(Bundle state) {
        Log.d(TAG, "restoreState");
        if (state != null) {
            mSpinnerPosition = state.getInt("SpinnerPosition", 0);
        } else {
            mSpinnerPosition = 0;
        }
    }

    public void saveState(Bundle outState) {
        Log.d(TAG, "saveState()");
        outState.putInt("SpinnerPosition", mSpinnerPosition);
    }


    @Override
    public boolean onTouch(View v, MotionEvent event) {
//        Log.d(TAG, ">>> onTouch - in TA");

        return mGestureDetector.onTouchEvent(event);
    }

    public void onSwipeRight() {
//        Log.d(TAG, ">>> onSwipeRight - in TA");

        int position = mGroupSpinner.getSelectedItemPosition() - 1;
        if (position < 0) {
            position = mGroupSpinner.getCount() - 1;
        }
//        Toast.makeText(context, "Swiped right, was (" + mGroupSpinner.getSelectedItemPosition() + "), now (" + position + ")", Toast.LENGTH_SHORT).show();  // consider leaving for future debug
        mGroupSpinner.setSelection(position);
    }

    public void onSwipeLeft() {
//        Log.d(TAG, ">>> onSwipeLeft - in TA");

        int position = mGroupSpinner.getSelectedItemPosition() + 1;
        if (position == mGroupSpinner.getCount()) {
            position = 0;
        }
//        Toast.makeText(context, "Swiped left, was (" + mGroupSpinner.getSelectedItemPosition() + "), now (" + position + ")", Toast.LENGTH_SHORT).show();  // consider leaving for future debug
        mGroupSpinner.setSelection(position);
    }

    public Group getGroupCurrentlyDisplayed() {
        int spinnerPosition = mGroupSpinner.getSelectedItemPosition();
        if (isAllGroupSelected()) {
            return Group.ALL_GROUP;
        } else {
            return DataSource.getInstance().getGroup(mSpinnerArrayGroupIds.get(spinnerPosition));
        }
    }


    private final class GestureListener extends GestureDetector.SimpleOnGestureListener {

        private static final int SWIPE_THRESHOLD = 50;
        private static final int SWIPE_VELOCITY_THRESHOLD = 50;

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
//            Log.d(TAG, ">>> onFling");
            if ((e1 == null) || (e2 == null)) return false;

            float distanceX = e2.getX() - e1.getX();
            float distanceY = e2.getY() - e1.getY();
            if (Math.abs(distanceX) > Math.abs(distanceY) && Math.abs(distanceX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                if (distanceX > 0)
                    onSwipeRight();
                else
                    onSwipeLeft();
                return true;
            }
            return false;
        }
    }

}