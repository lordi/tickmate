package de.smasi.tickmate.fragments;

import android.app.Fragment;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.db.chart.Tools;
import com.db.chart.model.BarSet;
import com.db.chart.view.BarChartView;
import com.db.chart.view.ChartView;
import com.db.chart.view.animation.Animation;
import com.db.chart.view.animation.easing.CubicEase;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import de.smasi.tickmate.R;
import de.smasi.tickmatedata.models.Tick;
import de.smasi.tickmatedata.models.Track;
import de.smasi.tickmatedata.wear.DataUtils;
import de.smasi.tickmatedata.wear.WearDataClient;

/**
 * Created by Adrian Geuss on 24.11.15.
 */
public class FragmentStats extends Fragment implements MessageApi.MessageListener {

    private BarChartView mBarChartView;
    private Calendar startCalendar;
    private Calendar endCalendar;
    private long timespanMillis;
    private long spanSteps;

    private Track track;
    private WearDataClient mWearDataClient;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_stats, container, false);

        mBarChartView = (BarChartView) v.findViewById(R.id.barchart);

        Bundle args = getArguments();
        track = (Track) args.getSerializable("track");
        timespanMillis = args.getLong("timespanMillis");
        spanSteps = args.getLong("spanSteps");

        mWearDataClient = new WearDataClient(getActivity(),
                new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(Bundle bundle) {
                        loadChartData();
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
        Wearable.MessageApi.addListener(mWearDataClient.googleApiClient, this);

        return v;
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        try {
            if (messageEvent.getPath().equals(WearDataClient.WEAR_MESSAGE_RETRIEVE_TICKS)) {
                LinkedHashMap<String, Object> args = DataUtils.getObjectFromData(messageEvent.getData());
                Track track = (Track) args.get("track");
                Calendar startCalendar = Calendar.getInstance();
                startCalendar.setTimeInMillis((Long) args.get("startCalendar"));
                startCalendar.setTimeZone(TimeZone.getTimeZone((String) args.get("startCalendarTimeZoneId")));
                Calendar endCalendar = Calendar.getInstance();
                endCalendar.setTimeInMillis((Long) args.get("endCalendar"));
                endCalendar.setTimeZone(TimeZone.getTimeZone((String) args.get("endCalendarTimeZoneId")));

                if (track.getId() == this.track.getId() &&
                        (startCalendar.equals(this.startCalendar) || endCalendar.equals(this.endCalendar))) {
                    List<Tick> ticks = (List<Tick>) args.get("ticks");
                    if (ticks != null && getActivity() != null) {
                        createChart(ticks, endCalendar);
                    }
                }
            } else if (messageEvent.getPath().equals(WearDataClient.WEAR_MESSAGE_SET_TICK) ||
                    messageEvent.getPath().equals(WearDataClient.WEAR_MESSAGE_REMOVE_TICK) ||
                    messageEvent.getPath().equals(WearDataClient.WEAR_MESSAGE_REMOVE_LAST_TICK_OF_DAY)) {
                LinkedHashMap<String, Object> args = DataUtils.getObjectFromData(messageEvent.getData());
                Track track = (Track) args.get("track");
                if (track.getId() == track.getId()) {
                    loadChartData();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
    }

    private void loadChartData() {
        endCalendar = Calendar.getInstance();
        startCalendar = (Calendar) endCalendar.clone();
        startCalendar.setTimeInMillis(startCalendar.getTime().getTime() - (spanSteps * timespanMillis));

        mWearDataClient.retrieveTicks(track, startCalendar, endCalendar);
    }

    private void createChart(List<Tick> ticks, Calendar endCalendar) {
        // Bar chart customization
        int barColor = getActivity().getResources().getColor(R.color.button_blue);

        BarSet dataset = new BarSet();

        for (int i=0; i < spanSteps; i++) {
            Calendar calendar = (Calendar) endCalendar.clone();
            calendar.setTimeInMillis(calendar.getTime().getTime() - (i * timespanMillis));

            SimpleDateFormat weekDayFormat = new SimpleDateFormat("E", Locale.getDefault());
            String weekDay = weekDayFormat.format(calendar.getTime()).substring(0,1);

            long tickCount = 0;
            for (Tick tick: ticks) {
                if (calendar.get(Calendar.YEAR) == tick.getDate().get(Calendar.YEAR) &&
                        calendar.get(Calendar.DAY_OF_YEAR) == tick.getDate().get(Calendar.DAY_OF_YEAR)) {
                    tickCount++;
                }
            }

            dataset.addBar(weekDay, tickCount);
        }

        dataset.setColor(barColor);
        mBarChartView.setBarSpacing(32.0f);
        mBarChartView.setRoundCorners(10.0f);
        mBarChartView.reset();
        mBarChartView.addData(dataset);

        // Generic chart customization
//        mBarChartView.setXLabels()
        // Paint object used to draw Grid
        int gridColor = getActivity().getResources().getColor(R.color.semitransparent_grey);
        Paint gridPaint = new Paint();
        gridPaint.setColor(gridColor);
        gridPaint.setStyle(Paint.Style.STROKE);
        gridPaint.setAntiAlias(true);
        gridPaint.setStrokeWidth(Tools.fromDpToPx(1.0f));
        gridPaint.setPathEffect(new DashPathEffect(new float[]{2, 0}, 0));
        mBarChartView.setGrid(ChartView.GridType.NONE, gridPaint);
        mBarChartView.setAxisColor(gridColor);
        mBarChartView.setLabelsFormat(new DecimalFormat("#"));

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // Animation customization
                Animation anim = new Animation(500);
                anim.setEasing(new CubicEase());
                anim.setAlpha(3);
                anim.setOverlap(0.5f, new int[]{0, 1, 2, 3, 4, 5, 6,});
                mBarChartView.show(anim);
            }
        }, 100);
    }
}
