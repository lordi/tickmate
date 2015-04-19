package de.smasi.tickmate.models;

import java.util.Calendar;

public class DayRange {

	public Calendar startday;
	public Calendar endday;

	public DayRange(Calendar startday, Calendar endday) {
		this.startday = startday;
		this.endday = endday;
	}
	
	public Calendar getStartDay() {
		return startday;
	}

	public void setStartDay(Calendar startday) {
		this.startday = startday;
	}

	public Calendar getEndDay() {
		return endday;
	}

	public void setEndDay(Calendar endday) {
		this.endday = endday;
	}
}
