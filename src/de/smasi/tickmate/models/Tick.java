package de.smasi.tickmate.models;

import java.util.Calendar;

public class Tick {
	public int track_id;
	public Calendar date; // unix timestamp
	
	public Tick(int track_id, Calendar date) {
		this.track_id = track_id;
		this.date = date;
	}

	@Override
	public boolean equals(Object o) {
		Tick other = (Tick)o;
		return other.track_id == track_id &&
				other.date.get(Calendar.YEAR) == date.get(Calendar.YEAR) &&
				other.date.get(Calendar.MONTH) == date.get(Calendar.MONTH) &&
				other.date.get(Calendar.DAY_OF_MONTH) == date.get(Calendar.DAY_OF_MONTH);
	}
}
