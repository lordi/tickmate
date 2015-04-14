package de.smasi.tickmate.models;

import java.util.Calendar;

public class DayRange {

	public Calendar startday;
	public Calendar endday;

	public DayRange(Calendar startday, Calendar endday) {
		this.startday = startday;
		this.endday = endday;
	}

}
