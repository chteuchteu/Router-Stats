package com.chteuchteu.freeboxstats.obj;

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
		return "[" + this.from + " -> " + this.to + " = " + this.outageLengthSecs + "]";
	}
	
	public String getDurationString() {
		if (this.outageLengthSecs == 3600)
			return "Moins d'une heure";
		return String.format("%d:%02d:%02d", this.outageLengthSecs/3600, (this.outageLengthSecs%3600)/60, (this.outageLengthSecs%60));
	}
}