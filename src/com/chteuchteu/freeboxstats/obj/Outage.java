package com.chteuchteu.freeboxstats.obj;

public class Outage {
	private int from;
	private int to;
	
	public Outage() {
		this.from = -1;
		this.to = -1;
	}
	
	public Outage(int from, int to) {
		this.from = from;
		this.to = to;
	}
	
	public int getDuration() {
		// TODO convert in seconds
		return this.to - this.from;
	}
}