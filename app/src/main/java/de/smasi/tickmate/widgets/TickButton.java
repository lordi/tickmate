package de.smasi.tickmate.widgets;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.util.Calendar;

import de.smasi.tickmate.R;
import de.smasi.tickmate.database.TracksDataSource;
import de.smasi.tickmate.models.Track;


public class TickButton extends ToggleButton implements OnCheckedChangeListener {

    Track track;
    TracksDataSource ds;
    Calendar date;

    public TickButton(Context context, Track track, Calendar date, TracksDataSource ds) {
        super(context);

        this.track = track;
        this.date = (Calendar)date.clone();
        //this.setLayoutParams(new android.widget.LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, 20));
        this.setBackgroundResource(R.drawable.toggle_button);
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

        this.ds = ds;

        setChecked(ds.isTicked(track, date, false));
        this.setOnCheckedChangeListener(this);

    }

    public Track getTrack () {
        return track;
    }

    public Calendar getDate () {
        return date;
    }

    // Evaluates whether this TickButton should be check-able.
    // This depends on the preferences (have ticks been disable outside of today?)
    //  the current date, and the date of this TickButton
    private boolean isCheckChangePermitted() {

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        String limitActivePref = sharedPrefs.getString("active-date-key", "ALLOW_ALL");

        Calendar today = Calendar.getInstance();
		today.set(Calendar.HOUR, 0);
		today.set(Calendar.MINUTE, 0);
		today.set(Calendar.SECOND, 0);
		today.set(Calendar.MILLISECOND, 0);

        switch (limitActivePref) {
            case "ALLOW_CURRENT":
                return (date.compareTo(today) == 0);
            case "ALLOW_CURRENT_AND_NEXT_DAY":
                Calendar yesterday = (Calendar) today.clone();
                yesterday.add(Calendar.DATE, -1);
                return (date.compareTo(yesterday) >= 0);
            case "ALLOW_ALL":
            default:
                return true;
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton arg0, boolean ticked) {
        if (! isCheckChangePermitted()) {
            arg0.setChecked(!arg0.isChecked());
            Toast.makeText(getContext(), R.string.notify_user_ticking_disabled, Toast.LENGTH_LONG).show();
            return;
        }

        TickButton tb = (TickButton)arg0;

        TracksDataSource ds = new TracksDataSource(this.getContext());
        if (ticked) {
            ds.setTick(tb.getTrack(), tb.getDate(), true);
        }
        else {
            ds.removeTick(tb.getTrack(), tb.getDate());
        }
    }

}
