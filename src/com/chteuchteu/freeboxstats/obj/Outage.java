package com.chteuchteu.freeboxstats.obj;

public class Outage {
	private int from;
	private int to;
	
	private int outageLengthSecs;
	private int outageLength;
	private TimeUnit outageLengthUnit;
	
	public enum TimeUnit { SECONDS, MINUTES, HOURS, DAYS }
	
	public Outage(int from, int to) {
		this.from = from;
		this.to = to;
		
		int outageLengthTimestamp = to - from;
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
}