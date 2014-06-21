package com.chteuchteu.freeboxstats.net;

import java.util.ArrayList;

import org.json.JSONException;

import android.os.AsyncTask;
import android.util.Log;

import com.chteuchteu.freeboxstats.MainActivity;
import com.chteuchteu.freeboxstats.obj.Freebox;
import com.chteuchteu.freeboxstats.obj.NetResponse;

public class GraphLoader extends AsyncTask<Void, Void, Void> {
	private Freebox freebox;
	private ArrayList<String> fields;
	private NetResponse netResponse;
	
	public GraphLoader(Freebox freebox) {
		this.freebox = freebox;
	}
	
	@Override
	protected Void doInBackground(Void... params) {
		fields = new ArrayList<String>();
		//fields.add("rate_up");
		fields.add("rate_down");
		
		netResponse = NetHelper.loadGraph(freebox, fields);
		
		return null;
	}
	
	@Override
	protected void onPostExecute(Void res) {
		super.onPostExecute(res);
		
		if (netResponse != null && netResponse.hasSucceeded()) {
			Log.v("", "Applying graph");
			try {
				MainActivity.loadGraph(netResponse.getJsonObject().getJSONArray("data"), fields);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}
}