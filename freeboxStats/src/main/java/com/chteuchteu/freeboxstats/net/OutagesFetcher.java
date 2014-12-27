package com.chteuchteu.freeboxstats.net;

import android.app.AlertDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.chteuchteu.freeboxstats.OutagesAdapter;
import com.chteuchteu.freeboxstats.R;
import com.chteuchteu.freeboxstats.hlpr.Enums.Field;
import com.chteuchteu.freeboxstats.hlpr.Enums.Period;
import com.chteuchteu.freeboxstats.hlpr.OutagesHelper;
import com.chteuchteu.freeboxstats.hlpr.Util;
import com.chteuchteu.freeboxstats.obj.Freebox;
import com.chteuchteu.freeboxstats.obj.NetResponse;
import com.chteuchteu.freeboxstats.obj.Outage;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

public class OutagesFetcher extends AsyncTask<Void, Void, Void> {
	private Context context;
	private Freebox freebox;
	private ArrayList<Outage> outages;
	private AlertDialog dialog;
	private View dialogLayout;
	
	public OutagesFetcher(Context context, Freebox freebox, AlertDialog dialog, View dialogLayout) {
		this.context = context;
		this.freebox = freebox;
		this.dialog = dialog;
		this.dialogLayout = dialogLayout;
	}
	
	@Override
	protected Void doInBackground(Void... params) {
		
		this.outages = loadData();
		
		return null;
	}
	
	@Override
	protected void onPostExecute(Void res) {
		if (this.outages != null) {
			ProgressBar progressBar = (ProgressBar) dialogLayout.findViewById(R.id.outages_loading);
			progressBar.setVisibility(View.GONE);
			
			if (this.outages.isEmpty()) {
				String text = context.getText(R.string.outages_nooutage).toString();
				text = text.replaceAll("FROM", Util.Times.getDate_oneMonthAgo());
				text = text.replaceAll("TO", Util.Times.getDate_today());
				this.dialogLayout.findViewById(R.id.outages_text).setVisibility(View.GONE);
				this.dialogLayout.findViewById(R.id.outages_nooutage_title).setVisibility(View.VISIBLE);
				TextView tv_text = (TextView) this.dialogLayout.findViewById(R.id.outages_nooutage_text);
				tv_text.setText(text);
				tv_text.setVisibility(View.VISIBLE);
			} else {
				ListView listView = (ListView) dialogLayout.findViewById(R.id.outages_lv);
				OutagesAdapter outagesAdapter = new OutagesAdapter(context, R.layout.outage_item, OutagesHelper.reverseOrder(outages));
				listView.setAdapter(outagesAdapter);
			}
		} else {
			dialog.dismiss();
			Toast.makeText(this.context, R.string.outages_error, Toast.LENGTH_SHORT).show();
		}
	}
	
	/**
	 * Load the graph and display the result on MainActivity
	 * PlotIndex from 1 to 3
	 */
	private ArrayList<Outage> loadData() {
		ArrayList<Field> fields = new ArrayList<>();
		fields.add(Field.BW_DOWN);
		
		NetResponse netResponse = NetHelper.loadGraph(freebox, Period.MONTH, fields, false);
		
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