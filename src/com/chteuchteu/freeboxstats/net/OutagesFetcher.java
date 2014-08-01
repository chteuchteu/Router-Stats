package com.chteuchteu.freeboxstats.net;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;

import android.os.AsyncTask;

import com.chteuchteu.freeboxstats.MainActivity;
import com.chteuchteu.freeboxstats.hlpr.Enums.Field;
import com.chteuchteu.freeboxstats.hlpr.Enums.Period;
import com.chteuchteu.freeboxstats.hlpr.OutagesHelper;
import com.chteuchteu.freeboxstats.obj.Freebox;
import com.chteuchteu.freeboxstats.obj.NetResponse;
import com.chteuchteu.freeboxstats.obj.Outage;

public class OutagesFetcher extends AsyncTask<Void, Void, Void> {
	private Freebox freebox;
	private Period period;
	private ArrayList<Outage> outages;
	
	public OutagesFetcher(Freebox freebox, Period period) {
		this.freebox = freebox;
		this.period = period;
	}
	
	@Override
	protected Void doInBackground(Void... params) {
		
		this.outages = loadData();
		
		return null;
	}
	
	@Override
	protected void onPostExecute(Void res) {
		MainActivity.displayOutagesDialog(outages);
	}
	
	/**
	 * Load the graph and display the result on MainActivity
	 * PlotIndex from 1 to 3
	 * @param plotIndex
	 */
	private ArrayList<Outage> loadData() {
		ArrayList<Field> fields = new ArrayList<Field>();
		fields.add(Field.BW_DOWN);
		
		NetResponse netResponse = NetHelper.loadGraph(freebox, period, fields);
		
		if (netResponse != null && netResponse.hasSucceeded()) {
			try {
				JSONArray data = netResponse.getJsonObject().getJSONArray("data");
				return OutagesHelper.getOutages(data);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		
		return null;
	}
}