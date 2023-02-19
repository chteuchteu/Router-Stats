package com.chteuchteu.freeboxstats.hlpr;

import com.chteuchteu.freeboxstats.obj.Outage;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class OutagesHelper {
	public static ArrayList<Outage> getOutages(JSONArray values) {
		ArrayList<Outage> outages = new ArrayList<>();
		
		try {
			for (int i=1; i<values.length(); i++) {
				JSONObject obj = values.getJSONObject(i);
				
				if (!obj.has("bw_down") || obj.getInt("bw_down") == 0) { // Here's an outage!
					long timestamp1 = values.getJSONObject(i-1).getInt("time");
					long timestamp2 = obj.getInt("time");
					outages.add(new Outage(timestamp1, timestamp2));
				}
			}
		} catch (JSONException ex) {
			ex.printStackTrace();
		}
		
		return outages;
	}
	
	public static ArrayList<Outage> reverseOrder(ArrayList<Outage> outages) {
		ArrayList<Outage> newList = new ArrayList<>();
		
		for (int i=outages.size()-1; i>= 0; i--)
			newList.add(outages.get(i));
		
		return newList;
	}
}
