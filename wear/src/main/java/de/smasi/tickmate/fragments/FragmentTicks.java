package de.smasi.tickmate.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;

import java.util.Calendar;

import de.smasi.tickmate.R;
import de.smasi.tickmate.widgets.WearMultiTickButton;
import de.smasi.tickmate.widgets.WearTickButton;
import de.smasi.tickmatedata.models.Track;
import de.smasi.tickmatedata.wear.WearDataClient;

/**
 * Created by Adrian Geuss on 21.11.15.
 */
public class FragmentTicks extends Fragment {

    private LinearLayout mContainer;
    private TextView mTrackTitle;
    private ImageView mTrackIcon;

    private WearDataClient mWearDataClient;
    private Calendar mTodayCal;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_ticks, container, false);

        mContainer = (LinearLayout) v.findViewById(R.id.container);
        mTrackTitle = (TextView) v.findViewById(R.id.track_title);
        mTrackIcon = (ImageView) v.findViewById(R.id.track_icon);

        mWearDataClient = new WearDataClient(getActivity(),
                new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(Bundle bundle) {
                        loadTickButtons();
                    }

                    @Override
                    public void onConnectionSuspended(int i) {
//                        showConnectionError("Lost connection to handset.");
                    }
                },
                new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(ConnectionResult connectionResult) {
//                        showConnectionError("Unable to connect to handset.");
                    }
                });

        Bundle args = getArguments();
        Track track = (Track) args.getSerializable("track");
        mTrackTitle.setText(track.getName());
        mTrackIcon.setImageResource(track.getIconId(getActivity(), true));

        return v;
    }

    private Track getTrack() {
        return (Track) getArguments().getSerializable("track");
    }

    private void loadTickButtons() {
        // Reset calendar
        mTodayCal = Calendar.getInstance();
        mTodayCal.set(Calendar.HOUR, 0);
        mTodayCal.set(Calendar.MINUTE, 0);
        mTodayCal.set(Calendar.SECOND, 0);
        mTodayCal.set(Calendar.MILLISECOND, 0);

        Track track = getTrack();
        if (track.multipleEntriesEnabled()) {
            WearMultiTickButton counter = new WearMultiTickButton(getActivity(), mWearDataClient, track, (Calendar) mTodayCal.clone());
            mContainer.addView(counter);
        } else {
            WearTickButton checker = new WearTickButton(getActivity(), mWearDataClient, track, mTodayCal);
            mContainer.addView(checker);
        }
    }

}
