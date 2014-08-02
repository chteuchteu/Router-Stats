package com.chteuchteu.freeboxstats.obj;

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
		return "[" + this.from + " -> " + this.to + " = " + getDurationString() + "]";
	}
	
	public String getDurationString() {
		if (this.outageLengthSecs == 3600)
			return "Moins d'une heure";
		
		if (this.outageLengthSecs < 60)
			return "Moins d'une minute";
		else if (this.outageLengthSecs < 3600) {
			if (this.outageLengthSecs == 60)
				return "1 minutes";
			return (this.outageLengthSecs%3600)/60 + " minutes";
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
}