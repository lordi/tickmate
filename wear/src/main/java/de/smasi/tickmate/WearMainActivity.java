package de.smasi.tickmate;

import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.wearable.activity.WearableActivity;
import android.support.wearable.view.BoxInsetLayout;
import android.support.wearable.view.WearableListView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;

import java.util.List;

import de.smasi.tickmatedata.models.Track;
import de.smasi.tickmatedata.wear.DataClient;
import de.smasi.tickmatedata.wear.DataUtils;
import de.smasi.tickmatedata.wear.WearDataClient;

public class WearMainActivity extends WearableActivity implements MessageApi.MessageListener {

    private WearDataClient mWearDataClient;

    private BoxInsetLayout mContainerView;
    private ProgressBar mCircleProgressBar;
    private LinearLayout mSplash;
    private WearableListView mTrackListView;
    private TrackAdapter mTrackAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wear_main);
        setAmbientEnabled();

        mWearDataClient = new WearDataClient(this);
        Wearable.MessageApi.addListener(mWearDataClient.googleApiClient, this);

        mContainerView = (BoxInsetLayout) findViewById(R.id.container);
        mCircleProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mCircleProgressBar.setVisibility(View.INVISIBLE);
        mSplash = (LinearLayout) findViewById(R.id.splash);
        mTrackListView = (WearableListView) findViewById(R.id.track_listview);

        Button loadButton = (Button) findViewById(R.id.load_button);
        loadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCircleProgressBar.setVisibility(View.VISIBLE);
                mCircleProgressBar.setProgress(1);
                mWearDataClient.getTracks();
            }
        });

//        updateDisplay();
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
    public void onMessageReceived(MessageEvent messageEvent) {
        byte[] data = messageEvent.getData();
        if (messageEvent.getPath().equalsIgnoreCase(DataClient.WEAR_MESSAGE_GET_TRACKS)) {
            List<Track> tracks = DataUtils.getTrackListFromData(data);

            if (tracks != null) {
                mSplash.setVisibility(View.GONE);
                mTrackListView.setVisibility(View.VISIBLE);

                mTrackAdapter = new TrackAdapter(tracks);
                mTrackListView.setAdapter(mTrackAdapter);
            }

        } else if (messageEvent.getPath().equalsIgnoreCase(DataClient.WEAR_MESSAGE_GET_TICKS)) {
//            List<Tick> ticks = (List<Tick>) DataUtils.getTrackListFromData(data);
        }
        mCircleProgressBar.setVisibility(View.INVISIBLE);
    }

    private class TrackAdapter extends RecyclerView.Adapter<TrackAdapter.TrackViewHolder> {

        private List<Track> tracks;

        public class TrackViewHolder extends WearableListView.ViewHolder {
            private TextView mTitle;

            public TrackViewHolder(View view) {
                super(view);
                mTitle = (TextView) view.findViewById(R.id.title);
            }
        }

        public TrackAdapter(List<Track> tracks) {
            this.tracks = tracks;
        }

        @Override
        public int getItemCount() {
            return tracks.size();
        }

        @Override
        public TrackViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.row_track, parent, false);

            TrackViewHolder vh = new TrackViewHolder(v);
            return vh;
        }

        @Override
        public void onBindViewHolder(TrackViewHolder holder, int position) {
            Track track = tracks.get(position);

            holder.mTitle.setText(track.getName());
        }
    }
}

