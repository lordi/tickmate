package de.smasi.tickmate.widgets;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.Button;
import android.widget.Toast;

import java.util.Calendar;

import de.smasi.tickmate.R;
import de.smasi.tickmate.TickColor;
import de.smasi.tickmate.database.DataSource;
import de.smasi.tickmate.models.Track;

public class MultiTickButton extends Button implements OnClickListener, OnLongClickListener {
	Track track;
	Calendar date;
	int count;
    private Drawable mTickedDrawable;
    private Drawable mUnTickedDrawable;

	public MultiTickButton(Context context, Track track, Calendar date) {
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
        mUnTickedDrawable = TickColor.getUnTickedButtonDrawable(this.getContext());
        mTickedDrawable = TickColor.getTickedButtonDrawable(this.getContext(), track.getTickColor().getColorValue());
        setTickCount(DataSource.getInstance().getTickCountForDay(track, date));
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
	
	private void updateStatus() {
		count = DataSource.getInstance().getTicksForDay(this.getTrack(), this.getDate()).size();
		updateText();
	}
	
	private void updateText() {		
		if (count > 0) {
//			this.setBackgroundResource(R.drawable.counter_positive);
            this.setBackgroundDrawable(mTickedDrawable);
			this.setText(Integer.toString(count));
		} else {
//			this.setBackgroundResource(R.drawable.counter_neutral);
            this.setBackgroundDrawable(mUnTickedDrawable);
			this.setText("");
		}
	}
	
	@Override
	public void onClick(View v) {
		if (!ButtonHelpers.isCheckChangePermitted(getContext(), date)) {
			Toast.makeText(getContext(), R.string.notify_user_ticking_disabled, Toast.LENGTH_LONG).show();
			return;
		}
		Calendar c = Calendar.getInstance();
		c.set(Calendar.MILLISECOND, 0);

		if (c.get(Calendar.DAY_OF_YEAR) == this.date.get(Calendar.DAY_OF_YEAR) &&
				c.get(Calendar.YEAR) == this.date.get(Calendar.YEAR) &&
				c.get(Calendar.ERA) == this.date.get(Calendar.ERA)) {
			DataSource.getInstance().setTick(this.getTrack(), c, true);
		} else {
			DataSource.getInstance().setTick(this.getTrack(), this.date, false);
		}
		
		updateStatus();
	}
	
	@Override
	public boolean onLongClick(View v) {
		if (!ButtonHelpers.isCheckChangePermitted(getContext(), date)) {
			Toast.makeText(getContext(), R.string.notify_user_ticking_disabled, Toast.LENGTH_LONG).show();
			return false;
		}

		boolean success = DataSource.getInstance().removeLastTickOfDay(this.getTrack(), this.getDate());
		
		if (success) {
			updateStatus();
			Toast.makeText(this.getContext(), R.string.tick_deleted, Toast.LENGTH_SHORT).show();
		}
		
		return true;
	}
}
