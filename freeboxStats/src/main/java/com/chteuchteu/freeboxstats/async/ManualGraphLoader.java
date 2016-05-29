package com.chteuchteu.freeboxstats.async;

import android.os.AsyncTask;

import com.chteuchteu.freeboxstats.FooBox;
import com.chteuchteu.freeboxstats.hlpr.Enums.Field;
import com.chteuchteu.freeboxstats.hlpr.Enums.FieldType;
import com.chteuchteu.freeboxstats.hlpr.Enums.Period;
import com.chteuchteu.freeboxstats.hlpr.SettingsHelper;
import com.chteuchteu.freeboxstats.net.NetHelper;
import com.chteuchteu.freeboxstats.obj.ErrorsLogger;
import com.chteuchteu.freeboxstats.obj.Freebox;
import com.chteuchteu.freeboxstats.obj.GraphsContainer;
import com.chteuchteu.freeboxstats.obj.NetResponse;
import com.chteuchteu.freeboxstats.ui.IMainActivity;
import com.crashlytics.android.Crashlytics;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

public class ManualGraphLoader extends AsyncTask<Void, Void, Void> {
	private Freebox freebox;
	private Period period;
	private IMainActivity activity;
	
	private GraphsContainer graph1;
	private GraphsContainer graph2;
	private GraphsContainer graph3;
	private GraphsContainer graph4;
	
	private boolean graphLoadingFailed;
	private boolean needUpdate;
	
	public ManualGraphLoader(Freebox freebox, Period period, IMainActivity activity) {
		this.freebox = freebox;
		this.period = period;
		this.activity = activity;
		this.graph1 = null;
		this.graph2 = null;
		this.graph3 = null;
		this.graph4 = null;
		this.graphLoadingFailed = false;
		this.needUpdate = false;
	}
	
	@Override
	protected Void doInBackground(Void... params) {
		graph1 = loadGraph(1);
		if (!graphLoadingFailed) {
			graph2 = loadGraph(2);
			graph3 = loadGraph(3);
			if (SettingsHelper.getInstance().getDisplayXdslTab())
				graph4 = loadGraph(4);
		}
		
		return null;
	}
	
	@Override
	protected void onPostExecute(Void res) {
        activity.setUpdating(false);
		
		if (!graphLoadingFailed) {
			if (graph1 != null)
				activity.loadGraph(FooBox.PlotType.RATEDOWN, graph1, period, graph1.getValuesUnit());
			if (graph2 != null)
				activity.loadGraph(FooBox.PlotType.RATEUP, graph2, period, graph2.getValuesUnit());
			if (graph3 != null)
				activity.loadGraph(FooBox.PlotType.TEMP, graph3, period, graph3.getValuesUnit());
			if (graph4 != null)
				activity.loadGraph(FooBox.PlotType.XDSL, graph4, period, graph4.getValuesUnit());
		}
		
		if (graphLoadingFailed) {
			activity.toggleProgressBar(false);
			activity.graphLoadingFailed();
		}

		if (needUpdate)
			activity.displayFreeboxUpdateNeededScreen();
	}
	
	/**
	 * Load the graph and display the result on MainActivity
	 * @param plotIndex int
	 */
	private GraphsContainer loadGraph(int plotIndex) {
		ArrayList<Field> fields = new ArrayList<>();
		FieldType fieldType = null;
		
		switch (plotIndex) {
			case 1:
				fields.add(Field.RATE_DOWN);
				fields.add(Field.BW_DOWN);
				fieldType = FieldType.DATA;
				break;
			case 2:
				fields.add(Field.RATE_UP);
				fields.add(Field.BW_UP);
				fieldType = FieldType.DATA;
				break;
			case 3:
				fields.add(Field.CPUM);
				fields.add(Field.CPUB);
				fields.add(Field.SW);
				fields.add(Field.HDD);
				fieldType = FieldType.TEMP;
				break;
			case 4:
				fields.add(Field.SNR_DOWN);
				fields.add(Field.SNR_UP);
				fieldType = FieldType.NOISE;
				break;
		}
		
		NetResponse netResponse = NetHelper.loadGraph(freebox, period, fields);
		
		if (netResponse != null && netResponse.hasSucceeded()) {
			try {
				if (netResponse.getJsonObject().get("data") instanceof JSONArray)
					return new GraphsContainer(fields, netResponse.getJsonObject().getJSONArray("data"), fieldType, period);
				else
					return null;
			} catch (JSONException ex) {
				ex.printStackTrace();
				Crashlytics.logException(ex);
				return null;
			}
		} else {
			boolean cancel = true;
			
			if (netResponse != null) {
				ErrorsLogger.log(netResponse);
				try {
					String response = netResponse.getCompleteResponse().getString("error_code");
					// If the session has expired / hasn't beed opened, open it

					switch (response) {
						case "auth_required":
							cancel = false;
							new SessionOpener(freebox, activity).execute();
							break;
						case "insufficient_rights":
							cancel = true;
							break;
						case "invalid_request":
						case "invalid_api_version":
							cancel = true;
							needUpdate = true;
							break;
					}
				} catch (JSONException ex) {
					Crashlytics.logException(ex);
					ex.printStackTrace();
					return null;
				}
			} else
				ErrorsLogger.log("GraphLoader - error 401");
			
			graphLoadingFailed = cancel;
			
			return null;
		}
	}
}
