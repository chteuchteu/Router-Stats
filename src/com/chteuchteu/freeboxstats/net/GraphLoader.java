package com.chteuchteu.freeboxstats.net;

import java.util.ArrayList;

import org.json.JSONException;

import android.os.AsyncTask;

import com.chteuchteu.freeboxstats.MainActivity;
import com.chteuchteu.freeboxstats.hlpr.Enums.Field;
import com.chteuchteu.freeboxstats.hlpr.Enums.Period;
import com.chteuchteu.freeboxstats.obj.Freebox;
import com.chteuchteu.freeboxstats.obj.GraphsContainer;
import com.chteuchteu.freeboxstats.obj.NetResponse;

public class GraphLoader extends AsyncTask<Void, Void, Void> {
	private Freebox freebox;
	private Period period;
	private ArrayList<Field> fields;
	private GraphsContainer graphsContainer;
	
	public GraphLoader(Freebox freebox, Period period, ArrayList<Field> fields) {
		this.freebox = freebox;
		this.period = period;
		this.fields = fields;
	}
	
	@Override
	protected Void doInBackground(Void... params) {
		NetResponse netResponse = NetHelper.loadGraph(freebox, period, fields);
		if (netResponse != null && netResponse.hasSucceeded()) {
			try {
				graphsContainer = new GraphsContainer(fields, netResponse.getJsonObject().getJSONArray("data"));
			} catch (JSONException e) { e.printStackTrace(); }
		}
		
		return null;
	}
	
	@Override
	protected void onPostExecute(Void res) {
		super.onPostExecute(res);
		
		if (graphsContainer != null)
			MainActivity.loadGraph(graphsContainer);
	}
}