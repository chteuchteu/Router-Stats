package com.chteuchteu.freeboxstats.hlpr;

import java.util.ArrayList;

import com.chteuchteu.freeboxstats.obj.NetResponse;
import com.chteuchteu.freeboxstats.obj.Outage;

public class OutagesHelper {
	public static ArrayList<Outage> getOutages(NetResponse values) {
		ArrayList<Outage> outages = new ArrayList<Outage>();
		
		// Get normal difference between two values
		// => foreach : x = n-(n-1) => average(x)
		
		// Foreach value in values : check if (n-(n-1)) is > x
		// If true : get diff and construct Outage object
		
		return outages;
	}
}