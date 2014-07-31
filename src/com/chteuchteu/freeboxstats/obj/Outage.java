package com.chteuchteu.freeboxstats.obj;

public class Outage {
	private Long from;
	private Long to;
	
	private Long outageLengthSecs;
	
	public Outage(Long from, Long to) {
		this.from = from;
		this.to = to;
		
		this.outageLengthSecs = (to - from)/60;
	}
	
	@Override
	public String toString() {
		return "[" + this.from + " -> " + this.to + " = " + this.outageLengthSecs + "]";
	}
}