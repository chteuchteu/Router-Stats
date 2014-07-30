package com.chteuchteu.freeboxstats.hlpr;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;

import com.chteuchteu.freeboxstats.obj.Outage;
import com.crashlytics.android.Crashlytics;

public class OutagesHelper {
	public static ArrayList<Outage> getOutages(JSONArray values) {
		ArrayList<Outage> outages = new ArrayList<Outage>();
		
		try {
			ArrayList<Integer> timestampsList = GraphHelper.getTimestampsFromData(values);
			
			// Get normal difference between two values
			// => foreach : x = n-(n-1) => average(x)
			int averageTimestampDiff = getAverageTimestampDiff(timestampsList);
			
			// Foreach value in values : check if (n-(n-1)) is > x
			// If true : get diff and construct Outage object
			for (int i=1; i<timestampsList.size(); i++) {
				int from = timestampsList.get(i-1);
				int to = timestampsList.get(i);
				int diff = to - from;
				// We're looking on significant outages => averageTimestampDiff*2
				if (diff > (averageTimestampDiff*2)) {
					Outage outage = new Outage(from, to);
					outages.add(outage);
				}
			}
		} catch (JSONException ex) {
			ex.printStackTrace();
			Crashlytics.logException(ex);
		} catch (Exception ex) {
			ex.printStackTrace();
			Crashlytics.logException(ex);
		}
		
		return outages;
	}

	private static int getAverageTimestampDiff(ArrayList<Integer> values) throws JSONException {
		ArrayList<Integer> diffs = new ArrayList<Integer>();

		for (int i=1; i<values.size(); i++) {
			int diff = values.get(i) - values.get(i-1);
			diffs.add(diff);
		}
		
		return (int) Util.calculateAverage(diffs);
	}
}