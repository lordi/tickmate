package de.smasi.tickmate.widgets;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;

import java.util.Calendar;
import java.util.List;

import de.smasi.tickmatedata.models.Tick;
import de.smasi.tickmatedata.models.Track;
import de.smasi.tickmatedata.wear.DataUtils;
import de.smasi.tickmatedata.wear.WearDataClient;
import de.smasi.tickmatedata.widgets.ButtonHelpers;

/**
 * Created by Adrian Geuss on 22.11.15.
 */

public class WearMultiTickButton extends Button implements View.OnClickListener, View.OnLongClickListener, MessageApi.MessageListener {
    Track track;
    Calendar date;
    int count;
    private WearDataClient mWearDataClient;

    public WearMultiTickButton(Context context, WearDataClient wearDataClient, Track track, Calendar date) {
        super(context);
        this.setOnClickListener(this);
        this.setOnLongClickListener(this);
        this.track = track;
        this.date = date;
        int size = 32;
        this.setWidth(size);
        this.setMinWidth(size);
        this.setMaxWidth(size);
        this.setHeight(size);
        this.setMinHeight(size);
        this.setPadding(0, 0, 0, 0);
        this.mWearDataClient = wearDataClient;
        Wearable.MessageApi.addListener(mWearDataClient.googleApiClient, this);
        updateStatus();
    }

    Track getTrack () {
        return track;
    }

    Calendar getDate () {
        return date;
    }

    public void setTickCount(int count) {
        this.count = count;
        updateText();
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        try {
            Bundle args = DataUtils.getObjectFromData(messageEvent.getData());
            Track track = (Track) args.getSerializable("track");
            Calendar calendar = (Calendar) args.getSerializable("calendar");
            if (track.getId() == this.track.getId() && calendar.equals(this.date)) {
                if (messageEvent.getPath().equals(WearDataClient.WEAR_MESSAGE_GET_TICKS) ||
                        messageEvent.getPath().equals(WearDataClient.WEAR_MESSAGE_SET_TICK) ||
                        messageEvent.getPath().equals(WearDataClient.WEAR_MESSAGE_REMOVE_LAST_TICK_OF_DAY)) {
                    byte[] tickData = args.getByteArray("ticks");
                    List<Tick> ticks = DataUtils.getObjectFromData(tickData);
                    setTickCount(ticks.size());
                    setUpdating(false);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
    }

    private void setUpdating(boolean isUpdating) {
        setEnabled(!isUpdating);
        // TODO: Show loading indicator on button while loading ticks
    }

    private void updateStatus() {
        setUpdating(true);
        mWearDataClient.getTickCountForDay(track, date);
    }

    private void updateText() {
        if (count > 0) {
            this.setBackgroundResource(de.smasi.tickmatedata.R.drawable.counter_positive);
            this.setText(Integer.toString(count));
        } else {
            this.setBackgroundResource(de.smasi.tickmatedata.R.drawable.counter_neutral);
            this.setText("");
        }
    }

    @Override
    public void onClick(View v) {
        if (!ButtonHelpers.isCheckChangePermitted(getContext(), date)) {
            Toast.makeText(getContext(), de.smasi.tickmatedata.R.string.notify_user_ticking_disabled, Toast.LENGTH_LONG).show();
            return;
        }

        Calendar c = Calendar.getInstance();
        c.set(Calendar.MILLISECOND, 0);

        if (c.get(Calendar.DAY_OF_MONTH) == this.date.get(Calendar.DAY_OF_MONTH)) {
            mWearDataClient.setTick(this.track, c, false);
            setUpdating(true);
        } else {
            mWearDataClient.setTick(this.track, this.date, false);
            setUpdating(true);
        }
    }

    @Override
    public boolean onLongClick(View v) {
        if (!ButtonHelpers.isCheckChangePermitted(getContext(), date)) {
            Toast.makeText(getContext(), de.smasi.tickmatedata.R.string.notify_user_ticking_disabled, Toast.LENGTH_LONG).show();
            return false;
        }

        mWearDataClient.removeLastTickOfDay(this.track, this.date);
        setUpdating(true);

        return true;
    }
}
