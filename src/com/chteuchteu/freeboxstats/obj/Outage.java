package com.chteuchteu.freeboxstats.obj;

public class Outage {
	private Long from;
	private Long to;
	
	private Long outageLengthSecs;
	private Long outageLength;
	private TimeUnit outageLengthUnit;
	
	public enum TimeUnit { SECONDS, MINUTES, HOURS, DAYS }
	
	public Outage(Long from, Long to) {
		this.from = from;
		this.to = to;
		
		Long outageLengthTimestamp = to - from;
		// TODO convert in seconds
		
		this.outageLengthSecs = outageLengthTimestamp;
		
	}
	
	public static TimeUnit getBestUnit(int outageLengthSecs) {
		long outageLength = outageLengthSecs;
		
		if (outageLength <= 60)
			return TimeUnit.SECONDS;
		
		outageLength = outageLength / 60;
		if (outageLength <= 60)
			return TimeUnit.MINUTES;
		
		outageLength = outageLength / 60;
		if (outageLength <= 60)
			return TimeUnit.HOURS;
		
		return TimeUnit.DAYS;
	}
	
	public static long convert() {
		// TODO convert
		return 0;
	}
	
	@Override
	public String toString() {
		return "[" + this.from + " -> " + this.to + " = " + this.outageLength + "]";
	}
}