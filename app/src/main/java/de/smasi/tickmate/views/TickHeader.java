package de.smasi.tickmate.views;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.List;

import de.smasi.tickmate.R;
import de.smasi.tickmate.TickmateConstants;
import de.smasi.tickmate.database.DataSource;
import de.smasi.tickmate.models.Group;
import de.smasi.tickmate.models.Track;
import de.smasi.tickmate.widgets.TrackButton;

public class TickHeader extends LinearLayout implements
        AdapterView.OnItemSelectedListener,
        View.OnTouchListener {

    private static final String TAG = "TickListHeader";

    private Spinner mGroupSpinner;
    private LinearLayout mTrackHeader;

    private GestureDetector mGestureDetector;
    private TickHeaderListener mHeaderListener;

    // data
    private ArrayList<Integer> mGroupIds = new ArrayList<>();
    private int mSpinnerPosition = 0;

    public interface TickHeaderListener {
        void onGroupSelected(int groupId);
    }

    public TickHeader(Context context) {
        super(context);
        inflate();
    }

    public TickHeader(Context context, AttributeSet attrs) {
        super(context, attrs);
        inflate();
    }

    public TickHeader(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        inflate();
    }

    private void inflate() {
        setOrientation(LinearLayout.VERTICAL);

        LayoutInflater.from(getContext()).inflate(R.layout.tickmate_list_header, this, true);

        mGroupSpinner = (Spinner) findViewById(R.id.groups_spinner);
        mTrackHeader = (LinearLayout) findViewById(R.id.tracks_header);

        mGestureDetector = new GestureDetector(getContext(), new HeaderGestureListener());
    }

    public void initialize(TickHeaderListener headerListener) {
        refresh();

        mHeaderListener = headerListener;
        if (mHeaderListener != null) {
            mHeaderListener.onGroupSelected(getCurrentGroupId());
        }
    }

    public void refresh() {
        initializeGroupSpinner();
        initializeTrackHeader();
    }

    private void initializeGroupSpinner() {
        mGroupSpinner.setOnTouchListener(this);

        List<Group> allGroups = DataSource.getInstance().getGroups();
        mGroupSpinner.setVisibility((allGroups.size() > 0) ? View.VISIBLE : View.GONE);

        List<String> mSpinnerArrayNames = new ArrayList<>();
        mSpinnerArrayNames.add(getResources().getString(R.string.group_all_name));
        for (Group group : allGroups) {
            mSpinnerArrayNames.add(group.getName());
        }

        initializeGroupIds();

        ArrayAdapter<String> spinnerArrayAdapter =
                new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, mSpinnerArrayNames);
        mGroupSpinner.setAdapter(spinnerArrayAdapter);

        SharedPreferences groupPreferences = getContext().getSharedPreferences(TickmateConstants.GROUP_PREFS, Context.MODE_PRIVATE);
        mSpinnerPosition = groupPreferences.getInt(TickmateConstants.GROUP_SELECTED, 0);

        mGroupSpinner.setSelection(mSpinnerPosition);
        mGroupSpinner.setOnItemSelectedListener(this);
    }

    private void initializeGroupIds() {
        List<Group> allGroups = DataSource.getInstance().getGroups();
        mGroupIds.clear();

        // The first entry in this array refers to the 'All Group', which does not occur in the database.
        mGroupIds.add(Group.ALL_GROUP.getId());
        for (Group group : allGroups) {
            mGroupIds.add(group.getId());
        }
    }

    private void initializeTrackHeader() {
        mTrackHeader.removeAllViews();

        View shimView = new View(getContext());
        shimView.setOnTouchListener(this);
        mTrackHeader.addView(shimView, new LayoutParams(
                0,
                LayoutParams.MATCH_PARENT,
                0.2f));

        List<Track> currentTracks = getTracksForCurrentGroup();
        for (Track track : currentTracks) {
            TrackButton trackButton = new TrackButton(getContext(), track);
            trackButton.setOnTouchListener(this);
            mTrackHeader.addView(trackButton, new LayoutParams(
                    0,
                    LayoutParams.MATCH_PARENT,
                    (0.8f / (float) currentTracks.size())));
        }
    }

    private List<Track> getTracksForCurrentGroup() {
        if (isAllGroupSelected()) {
            return DataSource.getInstance().getActiveTracks();
        } else {
            return DataSource.getInstance().getTracksForGroup(getCurrentGroupId());
        }
    }

    private int getCurrentGroupId() {
        return mGroupIds.get(mSpinnerPosition);
    }

    private boolean isAllGroupSelected() {
        // we only have the default "all" group, so return that one
        if (mGroupSpinner.getVisibility() == View.GONE) {
            return (mSpinnerPosition == TickmateConstants.ALL_GROUPS_SPINNER_INDEX);
        }

        return (mGroupSpinner.getSelectedItemPosition() == TickmateConstants.ALL_GROUPS_SPINNER_INDEX);
    }

    private void onSwipeRight() {
        int position = mGroupSpinner.getSelectedItemPosition() - 1;
        if (position < 0) {
            position = mGroupSpinner.getCount() - 1;
        }

        mGroupSpinner.setSelection(position);
    }

    private void onSwipeLeft() {
        int position = mGroupSpinner.getSelectedItemPosition() + 1;
        if (position == mGroupSpinner.getCount()) {
            position = 0;
        }

        mGroupSpinner.setSelection(position);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        // Unless a new item was selected in the spinner, do nothing.
        if (pos == mSpinnerPosition) {
            Log.d(TAG, "Spinner selection matches previous selection, nothing to do.");
            return;
        }

        mSpinnerPosition = pos;

        SharedPreferences groupPreferences = getContext().getSharedPreferences(TickmateConstants.GROUP_PREFS, Context.MODE_PRIVATE);
        groupPreferences.edit().putInt(TickmateConstants.GROUP_SELECTED, mSpinnerPosition).apply();

        initializeTrackHeader();

        if (mHeaderListener != null) {
            mHeaderListener.onGroupSelected(getCurrentGroupId());
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // noop
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return mGestureDetector.onTouchEvent(event);
    }

    private final class HeaderGestureListener extends GestureDetector.SimpleOnGestureListener {
        private static final int SWIPE_THRESHOLD = 50;
        private static final int SWIPE_VELOCITY_THRESHOLD = 50;

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if ((e1 == null) || (e2 == null)) return false;

            float distanceX = e2.getX() - e1.getX();
            float distanceY = e2.getY() - e1.getY();
            if (Math.abs(distanceX) > Math.abs(distanceY)
                    && Math.abs(distanceX) > SWIPE_THRESHOLD
                    && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                if (distanceX > 0) {
                    onSwipeRight();
                } else {
                    onSwipeLeft();
                }

                return true;
            }

            return false;
        }
    }
}
