package com.chteuchteu.freeboxstats.net;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;

import android.content.Context;
import android.os.AsyncTask;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.chteuchteu.freeboxstats.OutagesAdapter;
import com.chteuchteu.freeboxstats.R;
import com.chteuchteu.freeboxstats.hlpr.Enums.Field;
import com.chteuchteu.freeboxstats.hlpr.Enums.Period;
import com.chteuchteu.freeboxstats.hlpr.OutagesHelper;
import com.chteuchteu.freeboxstats.obj.Freebox;
import com.chteuchteu.freeboxstats.obj.NetResponse;
import com.chteuchteu.freeboxstats.obj.Outage;

public class OutagesFetcher extends AsyncTask<Void, Void, Void> {
	private Context context;
	private Freebox freebox;
	private Period period;
	private ArrayList<Outage> outages;
	private ListView listView;
	private ProgressBar progressBar;
	
	public OutagesFetcher(Context context, Freebox freebox, Period period, ListView listView, ProgressBar progressBar) {
		this.context = context;
		this.freebox = freebox;
		this.period = period;
		this.listView = listView;
		this.progressBar = progressBar;
	}
	
	@Override
	protected Void doInBackground(Void... params) {
		
		this.outages = loadData();
		
		return null;
	}
	
	@Override
	protected void onPostExecute(Void res) {
		this.progressBar.setVisibility(View.GONE);
		OutagesAdapter outagesAdapter = new OutagesAdapter(context, R.layout.outage_item, OutagesHelper.reverseOrder(outages));
		this.listView.setAdapter(outagesAdapter);
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