package de.smasi.tickmate;

import android.app.Fragment;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.support.wearable.view.BoxInsetLayout;
import android.support.wearable.view.FragmentGridPagerAdapter;
import android.support.wearable.view.GridPagerAdapter;
import android.support.wearable.view.GridViewPager;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;

import java.util.List;

import de.smasi.tickmate.fragments.FragmentStats;
import de.smasi.tickmate.fragments.FragmentTicks;
import de.smasi.tickmate.utils.TrackUsage;
import de.smasi.tickmatedata.models.Track;
import de.smasi.tickmatedata.wear.DataUtils;
import de.smasi.tickmatedata.wear.WearDataClient;

public class WearMainActivity extends WearableActivity implements MessageApi.MessageListener {

    private WearDataClient mWearDataClient;

    private BoxInsetLayout mContainerView;
    private ProgressBar mCircleProgressBar;
    private LinearLayout mSplash;
    private TextView mSplashTitle;
    private TextView mErrorDesc;
    private LinearLayout mTrackLayout;
    private GridViewPager mTrackViewPager;
//    private DotsPageIndicator mTrackPageIndicator;
    private TrackPagerAdapter mTrackPagerAdapter;
    private int selectedRow = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wear_main);
        setAmbientEnabled();

        mWearDataClient = new WearDataClient(this,
                new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(Bundle bundle) {
                        clearConnectionError();
                        loadTracks();
                    }

                    @Override
                    public void onConnectionSuspended(int i) {
                        showConnectionError("Lost connection to handset.");
                    }
                },
                new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(ConnectionResult connectionResult) {
                        showConnectionError("Unable to connect to handset.");
                    }
                });
        Wearable.MessageApi.addListener(mWearDataClient.googleApiClient, this);

        mContainerView = (BoxInsetLayout) findViewById(R.id.container);
        mCircleProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mCircleProgressBar.setVisibility(View.INVISIBLE);
        mSplash = (LinearLayout) findViewById(R.id.splash);
        mSplashTitle = (TextView) findViewById(R.id.splash_title);
        mErrorDesc = (TextView) findViewById(R.id.error_desc);
        mTrackLayout = (LinearLayout) findViewById(R.id.track_container);
        mTrackViewPager = (GridViewPager) findViewById(R.id.pager);
//        mTrackPageIndicator = (DotsPageIndicator) findViewById(R.id.page_indicator);
//        mTrackPageIndicator.setPager(mTrackViewPager);

        mTrackViewPager.setOnPageChangeListener(new GridViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, int i1, float v, float v1, int i2, int i3) {

            }

            @Override
            public void onPageSelected(int row, int col) {
                selectedRow = row;
//                if (mTrackPagerAdapter.tracks != null) {
//                    Track track = mTrackPagerAdapter.tracks.get(row);
//                    TrackUsage.usedTrack(WearMainActivity.this, track);
//                }
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });
    }

//    @Override
//    public void onEnterAmbient(Bundle ambientDetails) {
//        super.onEnterAmbient(ambientDetails);
//        updateDisplay();
//    }
//
//    @Override
//    public void onUpdateAmbient() {
//        super.onUpdateAmbient();
//        updateDisplay();
//    }
//
//    @Override
//    public void onExitAmbient() {
//        updateDisplay();
//        super.onExitAmbient();
//    }
//
//    private void updateDisplay() {
//        mContainerView.setBackgroundColor(getResources().getColor(android.R.color.black));
//    }


    @Override
    protected void onPause() {
        if (mTrackPagerAdapter != null && mTrackPagerAdapter.tracks != null) {
            Track track = mTrackPagerAdapter.tracks.get(selectedRow);
            TrackUsage.usedTrack(this, track);
        }
        super.onPause();
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        if (messageEvent.getPath().equalsIgnoreCase(WearDataClient.WEAR_MESSAGE_GET_TRACKS)) {
            byte[] data = messageEvent.getData();
            List<Track> tracks = DataUtils.getObjectFromData(data);

            if (tracks != null) {
                // Sort tracks by usage
                TrackUsage.sortTracksByUsage(this, tracks);

                mTrackPagerAdapter = new TrackPagerAdapter(this, getFragmentManager(), tracks);
                mTrackViewPager.setAdapter(mTrackPagerAdapter);

                // Fade in
                AlphaAnimation alphaAnimation = new AlphaAnimation(1.0f, 0.0f);
                alphaAnimation.setDuration(500);
                alphaAnimation.setFillAfter(true);
                alphaAnimation.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        mSplash.setVisibility(View.GONE);
                        mTrackLayout.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                mSplash.startAnimation(alphaAnimation);
            }
            mCircleProgressBar.setVisibility(View.INVISIBLE);
        }
    }

    private void loadTracks() {
        mCircleProgressBar.setVisibility(View.VISIBLE);
        mCircleProgressBar.setProgress(1);
        mWearDataClient.getTracks();
    }

    private void showConnectionError(String desc) {
        mErrorDesc.setText(desc);
        mErrorDesc.setVisibility(View.VISIBLE);
        mTrackLayout.setVisibility(View.GONE);
    }

    private void clearConnectionError() {
        mErrorDesc.setText("");
        mErrorDesc.setVisibility(View.GONE);
    }

    public class TrackPagerAdapter extends FragmentGridPagerAdapter {

        private final Context mContext;
        public List<Track> tracks;

        public TrackPagerAdapter(Context ctx, android.app.FragmentManager fm, List<Track> tracks) {
            super(fm);
            mContext = ctx;
            this.tracks = tracks;
        }

        @Override
        public Fragment getFragment(int row, int col) {
            Track track = tracks.get(row);
            Fragment fragment = null;
            if (col == 0) {
                fragment = new FragmentTicks();
                Bundle args = new Bundle();
                args.putSerializable("track", track);
                fragment.setArguments(args);
            } else if (col == 1) {
                fragment = new FragmentStats();
                Bundle args = new Bundle();
                args.putSerializable("track", track);
                args.putLong("timespanMillis", 24 * 60 * 60 * 1000);
                args.putLong("spanSteps", 7);
                fragment.setArguments(args);
            }

            return fragment;
        }

        @Override
        public int getRowCount() {
            return tracks.size();
        }

        @Override
        public int getColumnCount(int i) {
            return 2;
        }

        @Override
        public Drawable getBackgroundForRow(int row) {
//            return mContext.getDrawable(R.color.blue);
            return GridPagerAdapter.BACKGROUND_NONE;
        }
    }
}

