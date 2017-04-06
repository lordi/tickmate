package de.smasi.tickmate.widgets;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.util.Calendar;

import de.smasi.tickmate.R;
import de.smasi.tickmate.TickColor;
import de.smasi.tickmate.database.DataSource;
import de.smasi.tickmate.models.Track;


public class TickButton extends ToggleButton implements OnCheckedChangeListener {

    Track track;
    DataSource ds;
    Calendar date;
    private Drawable mTickedDrawable;
    private Drawable mUnTickedDrawable;

    public TickButton(Context context, Track track, Calendar date) {
        super(context);

        this.track = track;
        this.date = (Calendar)date.clone();
        //this.setLayoutParams(new android.widget.LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, 20));


//        this.setBackgroundResource(R.drawable.toggle_button);
//        this.setBackgroundDrawable(mUnTickedDrawable);
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

        mUnTickedDrawable = TickColor.getUnTickedButtonDrawable(this.getContext());
        mTickedDrawable = TickColor.getTickedButtonDrawable(this.getContext(), track.getTickColor().getColorValue());
        if (isChecked()) {
            setBackgroundDrawable(mTickedDrawable);
        } else {
            setBackgroundDrawable(mUnTickedDrawable);
        }
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
            //  Since the track color maybe have changed since mTickedDrawable was created, refresh it
            //  TODO Anything we should do to ensure that all appropriate fields in all objects get updated when the user changes the tick color setting for a track?
            mTickedDrawable = TickColor.getTickedButtonDrawable(this.getContext(), track.getTickColor().getColorValue());
            setBackgroundDrawable(mTickedDrawable);

            Calendar c = Calendar.getInstance();
            if (c.get(Calendar.DAY_OF_YEAR) == tb.getDate().get(Calendar.DAY_OF_YEAR) &&
                    c.get(Calendar.YEAR) == tb.getDate().get(Calendar.YEAR) &&
                    c.get(Calendar.ERA) == tb.getDate().get(Calendar.ERA)) {
                ds.setTick(tb.getTrack(), c, true);
            } else {
                ds.setTick(tb.getTrack(), tb.getDate(), false);
            }
        }
        else {
            setBackgroundDrawable(mUnTickedDrawable);
            ds.removeTick(tb.getTrack(), tb.getDate());
        }
    }

}
