package com.chteuchteu.freeboxstats.async;

import android.os.AsyncTask;
import android.view.View;

import com.chteuchteu.freeboxstats.FooBox;
import com.chteuchteu.freeboxstats.hlpr.Enums;
import com.chteuchteu.freeboxstats.hlpr.Enums.Field;
import com.chteuchteu.freeboxstats.hlpr.Enums.Period;
import com.chteuchteu.freeboxstats.net.NetHelper;
import com.chteuchteu.freeboxstats.obj.ErrorsLogger;
import com.chteuchteu.freeboxstats.obj.Freebox;
import com.chteuchteu.freeboxstats.obj.NetResponse;
import com.chteuchteu.freeboxstats.obj.ValuesBag;
import com.chteuchteu.freeboxstats.ui.MainActivity;

import org.json.JSONArray;
import org.json.JSONException;

public class GraphLoader extends AsyncTask<Void, Void, Void> {
	private FooBox fooBox;
	private Freebox freebox;
	private Period period;
	private MainActivity activity;
	private Enums.Graph graph;

	private boolean needsFreeboxUpdate;
	private boolean success;
	private boolean displayToastOnError;

	public GraphLoader(FooBox fooBox, MainActivity activity, Enums.Graph graph) {
		this.fooBox = fooBox;
		this.freebox = fooBox.getFreebox();
		this.period = fooBox.getPeriod();
		this.activity = activity;
		this.graph = graph;
		this.needsFreeboxUpdate = false;
		this.success = false;
		this.displayToastOnError = true;
	}
	
	@Override
	protected Void doInBackground(Void... params) {
		success = loadGraph();
		
		return null;
	}
	
	@Override
	protected void onPostExecute(Void res) {
        activity.setUpdating(false);

		fooBox.getProgressBars().get(graph).setVisibility(View.GONE);

		if (success)
			activity.loadGraph(graph);
		else if (displayToastOnError)
			activity.graphLoadingFailed();

		if (needsFreeboxUpdate)
			activity.displayFreeboxUpdateNeededScreen();
	}

	/**
	 * Load the graph and display the result on MainActivity
	 */
	private boolean loadGraph() {
		ValuesBag valuesBag = FooBox.getInstance().getValuesBags().get(graph);
		Field[] fields = valuesBag.getFields();

		NetResponse netResponse = NetHelper.loadGraph(freebox, period, fields, valuesBag.getLastTimestamp());

		if (netResponse != null && netResponse.hasSucceeded()) {
			try {
				if (netResponse.getJsonObject().get("data") instanceof JSONArray) {
					valuesBag.fill(netResponse.getJsonObject().getJSONArray("data"));
					return true;
				}
			} catch (JSONException ex) {
				ex.printStackTrace();
				return false;
			}
		} else {
			if (netResponse != null) {
				ErrorsLogger.log(netResponse);
				try {
					String response = netResponse.getCompleteResponse().getString("error_code");

					// If the session has expired / hasn't been opened, open it
					switch (response) {
						case "auth_required":
							displayToastOnError = false;
							new SessionOpener(freebox, activity).execute();
							break;
						case "insufficient_rights":
							// We don't have sufficient rights, that's a failure
							break;
						case "invalid_request":
						case "invalid_api_version":
							needsFreeboxUpdate = true;
							break;
					}
				} catch (JSONException ex) {
					ex.printStackTrace();
					return false;
				}
			} else
				ErrorsLogger.log("GraphLoader - error 401");
		}

		return false;
	}
}
