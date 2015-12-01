package de.smasi.tickmatedata.models;

import java.io.Serializable;
import java.util.Calendar;

public class Tick implements Serializable {
	public int track_id;
	public int tick_id;
	private long dateMillis;
	public boolean hasTimeInfo;
	
	public Tick(int track_id, Calendar date) {
		this.track_id = track_id;
		setDate(date);
	}
	
	public Tick(int track_id, Calendar date, boolean hasTimeInfo) {
		this.track_id = track_id;
		setDate(date);
		this.hasTimeInfo = hasTimeInfo;
	}

	@Override
	public boolean equals(Object o) {
		Tick other = (Tick)o;
        Calendar thisDate = getDate();
        Calendar otherDate = other.getDate();

		if (this.hasTimeInfo) {
			return other.track_id == track_id &&
					otherDate.get(Calendar.YEAR) == thisDate.get(Calendar.YEAR) &&
					otherDate.get(Calendar.MONTH) == thisDate.get(Calendar.MONTH) &&
					otherDate.get(Calendar.DAY_OF_MONTH) == thisDate.get(Calendar.DAY_OF_MONTH) &&
					otherDate.get(Calendar.HOUR_OF_DAY) == thisDate.get(Calendar.HOUR_OF_DAY) &&
					otherDate.get(Calendar.MINUTE) == thisDate.get(Calendar.MINUTE) &&
					otherDate.get(Calendar.SECOND) == thisDate.get(Calendar.SECOND);
		}
		
		return other.track_id == track_id &&
                otherDate.get(Calendar.YEAR) == thisDate.get(Calendar.YEAR) &&
                otherDate.get(Calendar.MONTH) == thisDate.get(Calendar.MONTH) &&
                otherDate.get(Calendar.DAY_OF_MONTH) == thisDate.get(Calendar.DAY_OF_MONTH);
	}

	public Calendar getDate() {
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(dateMillis);
		return calendar;
	}

	public void setDate(Calendar date) {
		dateMillis = date.getTimeInMillis();
	}
}
