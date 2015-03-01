package com.chteuchteu.freeboxstats.obj;

import android.content.Context;

import com.chteuchteu.freeboxstats.R;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Outage {
	private Long from;
	private Long to;
	
	private Long outageLengthSecs;
	
	public Outage(Long from, Long to) {
		this.from = from;
		this.to = to;
		
		this.outageLengthSecs = (to - from);
	}
	
	@Override
	public String toString() {
		return "[" + this.from + " -> " + this.to;
	}
	
	public String getDurationString(Context context) {
		if (this.outageLengthSecs == 3600)
			return context.getString(R.string.outage_less_than_one_hour);
		
		if (this.outageLengthSecs < 60)
			return context.getString(R.string.outage_less_than_one_minute);
		else if (this.outageLengthSecs < 3600) {
			if (this.outageLengthSecs == 60)
				return context.getString(R.string.outage_one_minute);

			return context.getString(R.string.outage_minutes).replace("XX",
					String.valueOf(this.outageLengthSecs%3600/60));
		}
		
		return String.format("%d:%02d:%02d", this.outageLengthSecs/3600, (this.outageLengthSecs%3600)/60, (this.outageLengthSecs%60));
	}
	
	public String getFromDateString() {
		Date date = new Date(this.from * 1000);
		SimpleDateFormat formater = new SimpleDateFormat("dd/MM");
		return formater.format(date);
	}
	
	public String getFromHourString() {
		Date date = new Date(this.from * 1000);
		SimpleDateFormat formater = new SimpleDateFormat("hh:mm");
		return formater.format(date);
	}
	
	public String getToDateString() {
		Date date = new Date(this.to * 1000);
		SimpleDateFormat formater = new SimpleDateFormat("dd/MM");
		return formater.format(date);
	}
	
	public String getToHourString() {
		Date date = new Date(this.to * 1000);
		SimpleDateFormat formater = new SimpleDateFormat("hh:mm");
		return formater.format(date);
	}
	
	public boolean isSingleDay() {
		return getFromDateString().equals(getToDateString());
	}
}
