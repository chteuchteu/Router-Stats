package com.chteuchteu.freeboxstats.hlpr;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.chteuchteu.freeboxstats.FooBox;
import com.chteuchteu.freeboxstats.obj.Outage;
import com.crashlytics.android.Crashlytics;

public class OutagesHelper {
	/**
	 * Get all the values where BW_DOWN = 0. Then check the value before, and this value to get the time diff.
	 * Simple as fuck.
	 */
	
	
	public static ArrayList<Outage> getOutages(JSONArray values) {
		ArrayList<Outage> outages = new ArrayList<Outage>();
		
		try {
			for (int i=1; i<values.length(); i++) {
				JSONObject obj = values.getJSONObject(i);
				
				if (!obj.has("bw_down") || obj.getInt("bw_down") == 0) { // Here's an outage!
					long timestamp1 = values.getJSONObject(i-1).getLong("time");
					long timestamp2 = obj.getLong("time");
					outages.add(new Outage(timestamp1, timestamp2));
				}
			}
		} catch (JSONException ex) {
			ex.printStackTrace();
			Crashlytics.logException(ex);
		}
		
		return outages;
	}
	
	public static void logOutages(ArrayList<Outage> outages) {
		for (Outage outage : outages)
			FooBox.log(outage.toString());
	}
}