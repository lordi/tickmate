package de.smasi.tickmate.widgets;

import java.util.Calendar;

import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ToggleButton;
import de.smasi.tickmate.R;
import de.smasi.tickmate.models.Track;


public class TickButton extends ToggleButton implements OnClickListener {

	AnimatorSet highlight;
	Track track;
	Calendar date;

	public TickButton(Context context, Track track, Calendar date) {
		super(context);		
		
		highlight = (AnimatorSet) AnimatorInflater.loadAnimator(context,
			    R.animator.tick_highlight);
		highlight.setTarget(this);
		//highlight.start();
			
		this.track = track;
		this.date = date;
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
		this.setAlpha((float) 0.8);
		this.setOnClickListener(this);
	}
	
	public Track getTrack () {
		return track;
	}
	
	public Calendar getDate () {
		return date;		
	}
	
	@Override
	public void onClick(View v) {
		highlight.start();
	}

}
