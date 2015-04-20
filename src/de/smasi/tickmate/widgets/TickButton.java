package de.smasi.tickmate.widgets;

import java.util.Calendar;

import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CompoundButton;
import android.widget.ToggleButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import de.smasi.tickmate.R;
import de.smasi.tickmate.database.TracksDataSource;
import de.smasi.tickmate.models.Track;


public class TickButton extends ToggleButton implements OnCheckedChangeListener {

	AnimatorSet highlight;
	Track track;
	Calendar date;

	public TickButton(Context context, Track track, Calendar date, boolean checked) {
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
		
		setChecked(checked);
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
