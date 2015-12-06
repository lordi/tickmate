package de.smasi.tickmate.widgets;

import android.content.Context;
import android.os.Vibrator;
import android.widget.CompoundButton;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;

import java.util.Arrays;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.TimeZone;

import de.smasi.tickmatedata.models.Track;
import de.smasi.tickmatedata.wear.DataUtils;
import de.smasi.tickmatedata.wear.WearDataClient;
import de.smasi.tickmatedata.widgets.ButtonHelpers;

/**
 * Created by Adrian Geuss on 22.11.15.
 */
public class WearTickButton extends ToggleButton implements CompoundButton.OnCheckedChangeListener, MessageApi.MessageListener {

    Track track;
    private WearDataClient mWearDataClient;
    Calendar date;
    boolean pendingChanges = false;

    public WearTickButton(Context context, WearDataClient wearDataClient, Track track, Calendar date) {
        super(context);

        this.track = track;
        this.date = (Calendar)date.clone();
        this.date.set(Calendar.HOUR_OF_DAY, 0);
        this.date.set(Calendar.MINUTE, 0);
        this.date.set(Calendar.SECOND, 0);
        //this.setLayoutParams(new android.widget.LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, 20));
        this.setBackgroundResource(de.smasi.tickmatedata.R.drawable.toggle_button);
        int size = 32;
        this.setWidth(size);
        this.setMinWidth(size);
        this.setMaxWidth(size);
        this.setHeight(size);
        this.setMinHeight(size);
        this.setPadding(0, 0, 0, 0);
        this.setTextOn("");
        this.setTextOff("");
        //this.setAlpha((float) 0.8);

        setChecked(false);
        this.mWearDataClient = wearDataClient;
        Wearable.MessageApi.addListener(mWearDataClient.googleApiClient, this);
        setEnabled(false);
        setUpdating(true);
        mWearDataClient.isTicked(track, date, true);

        this.setOnCheckedChangeListener(this);
    }

    public Track getTrack () {
        return track;
    }

    public Calendar getDate () {
        return date;
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        try {
            if ( messageEvent.getPath().equals(WearDataClient.WEAR_MESSAGE_IS_TICKED) ||
                    messageEvent.getPath().equals(WearDataClient.WEAR_MESSAGE_SET_TICK) ||
                    messageEvent.getPath().equals(WearDataClient.WEAR_MESSAGE_REMOVE_TICK) ) {
                LinkedHashMap<String, Object> args = DataUtils.getObjectFromData(messageEvent.getData());
                Track track = (Track) args.get("track");
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis((Long) args.get("calendar"));
                calendar.setTimeZone(TimeZone.getTimeZone((String) args.get("calendarTimeZoneId")));

                if (track.getId() == this.track.getId() &&
                        calendar.get(Calendar.YEAR) == this.date.get(Calendar.YEAR) &&
                        calendar.get(Calendar.MONTH) == this.date.get(Calendar.MONTH) &&
                        calendar.get(Calendar.DAY_OF_MONTH) == this.date.get(Calendar.DAY_OF_MONTH)) {

                    Boolean isTicked = (Boolean) args.get("isTicked");
                    setUpdating(false);

                    if (messageEvent.getPath().equals(WearDataClient.WEAR_MESSAGE_IS_TICKED)) {
                        setOnCheckedChangeListener(null);
                        setChecked(isTicked);
                        setEnabled(true);
                        setOnCheckedChangeListener(this);
                    }

                    // Haptic feedback as confirmation
                    if (pendingChanges) {
                        Vibrator vibrator = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);
                        if (vibrator.hasVibrator()) {
                            long[] pattern = new long[10];
                            Arrays.fill(pattern, 10);
                            vibrator.vibrate(pattern, -1);
                        }
                    }
                    pendingChanges = false;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setUpdating(boolean isUpdating) {
//        setEnabled(!isUpdating);
        // TODO: Show loading indicator on button while loading ticks
    }

    @Override
    public void onCheckedChanged(CompoundButton arg0, boolean ticked) {
        if (!ButtonHelpers.isCheckChangePermitted(getContext(), date)) {
            arg0.setChecked(!arg0.isChecked());
            Toast.makeText(getContext(), de.smasi.tickmatedata.R.string.notify_user_ticking_disabled, Toast.LENGTH_LONG).show();
            return;
        }

        if (ticked) {
            mWearDataClient.setTick(this.track, this.date, true);
            pendingChanges = true;
        } else {
            mWearDataClient.removeTick(this.track, this.date, true);
            pendingChanges = true;
        }
    }
}
