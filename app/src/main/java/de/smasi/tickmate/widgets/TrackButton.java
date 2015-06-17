package de.smasi.tickmate.widgets;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;

import de.smasi.tickmate.models.Track;
import de.smasi.tickmate.views.ShowTrackActivity;


public class TrackButton extends ImageButton implements OnClickListener{
	Track track;
	
	public TrackButton(Context context, Track track) {
		super(context);
		this.track = track;
		this.setOnClickListener(this);
		this.setImageResource(track.getIconId(context));
	}

	@Override
	public void onClick(View v) {
		Intent intent = new Intent(getContext(), ShowTrackActivity.class);
		intent.putExtra("track_id", track.getId());
		getContext().startActivity(intent);
	}
}
