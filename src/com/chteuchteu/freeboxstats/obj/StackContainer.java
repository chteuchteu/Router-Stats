package com.chteuchteu.freeboxstats.obj;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.chteuchteu.freeboxstats.hlpr.Enums.Period;
import com.chteuchteu.freeboxstats.hlpr.Enums.Unit;
import com.chteuchteu.freeboxstats.hlpr.Util;

public class StackContainer {
	private Period period;
	
	private long upStack;
	private Unit upUnit;
	private long downStack;
	private Unit downUnit;
	
	public StackContainer(Period period, JSONArray data) {
		this.period = period;
		
		computeData(data);
	}
	
	private void computeData(JSONArray data) {
		long upStackSum = 0;
		long downStackSum = 0;
		
		for (int i=0; i<data.length(); i++) {
			try {
				if (i == data.length()-1)
					continue;
				JSONObject obj = (JSONObject) data.get(i);
				
				// Get time diff between now and now+1
				long nowTimestamp = obj.getLong("time");
				long nextTimestamp = ((JSONObject) data.get(i+1)).getLong("time");
				int timestampDiff = (int) (nextTimestamp - nowTimestamp);
				
				int upRate = obj.getInt("rate_up");
				int downRate = obj.getInt("rate_down");
				
				int upData = (int) (upRate * timestampDiff);
				int downData = (int) (downRate * timestampDiff);
				
				upStackSum += upData;
				downStackSum += downData;
				Log.v("", "down = "  + downStackSum);
			} catch (JSONException ex) {
				ex.printStackTrace();
			}
		}
		
		this.upStack = upStackSum;
		this.downStack = downStackSum;
		this.upUnit = Unit.Mo;
		this.downUnit = Unit.Mo;
	}
	
	public Period getPeriod() { return this.period; }
	public String getFormattedUpStack() {
		return String.format("%.5g%n",
				Util.convertUnit(Unit.o, this.upUnit, this.upStack));
	}
	public Unit getUpUnit() { return this.upUnit; }
	public String getFormattedDownStack() {
		return String.format("%.5g%n",
				Util.convertUnit(Unit.o, this.downUnit, this.downStack));
	}
	public Unit getDownUnit() { return this.downUnit; }
}