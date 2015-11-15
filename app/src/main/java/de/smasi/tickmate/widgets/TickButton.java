package de.smasi.tickmate.widgets;

import android.content.Context;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.util.Calendar;

import de.smasi.tickmate.R;
import de.smasi.tickmatedata.database.DataSource;
import de.smasi.tickmatedata.models.Track;


public class TickButton extends ToggleButton implements OnCheckedChangeListener {

    Track track;
    DataSource ds;
    Calendar date;

    public TickButton(Context context, Track track, Calendar date) {
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

        ds = DataSource.getInstance();

        setChecked(ds.isTicked(track, date, false));
        this.setOnCheckedChangeListener(this);

    }

    public Track getTrack () {
        return track;
    }

    public Calendar getDate () {
        return date;
    }

    @Override
    public void onCheckedChanged(CompoundButton arg0, boolean ticked) {
        if (!ButtonHelpers.isCheckChangePermitted(getContext(), date)) {
            arg0.setChecked(!arg0.isChecked());
            Toast.makeText(getContext(), R.string.notify_user_ticking_disabled, Toast.LENGTH_LONG).show();
            return;
        }

        TickButton tb = (TickButton)arg0;

        DataSource ds = DataSource.getInstance();
        if (ticked) {
            ds.setTick(tb.getTrack(), tb.getDate(), true);
        }
        else {
            ds.removeTick(tb.getTrack(), tb.getDate());
        }
    }

}
