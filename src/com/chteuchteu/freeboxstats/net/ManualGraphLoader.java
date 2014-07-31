package com.chteuchteu.freeboxstats.net;

import java.util.ArrayList;

import org.json.JSONException;

import android.os.AsyncTask;

import com.chteuchteu.freeboxstats.FooBox;
import com.chteuchteu.freeboxstats.MainActivity;
import com.chteuchteu.freeboxstats.hlpr.Enums.Field;
import com.chteuchteu.freeboxstats.hlpr.Enums.FieldType;
import com.chteuchteu.freeboxstats.hlpr.Enums.Period;
import com.chteuchteu.freeboxstats.obj.Freebox;
import com.chteuchteu.freeboxstats.obj.GraphsContainer;
import com.chteuchteu.freeboxstats.obj.NetResponse;
import com.crashlytics.android.Crashlytics;

public class ManualGraphLoader extends AsyncTask<Void, Void, Void> {
	private Freebox freebox;
	private Period period;
	
	private GraphsContainer graph1;
	private GraphsContainer graph2;
	private GraphsContainer graph3;
	
	private boolean graphLoadingFailed;
	private boolean needAuth;
	private boolean needUpdate;
	
	public ManualGraphLoader(Freebox freebox, Period period) {
		this.freebox = freebox;
		this.period = period;
		this.graph1 = null;
		this.graph2 = null;
		this.graph3 = null;
		this.graphLoadingFailed = false;
		this.needAuth = false;
		this.needUpdate = false;
	}
	
	@Override
	protected Void doInBackground(Void... params) {
		graph1 = loadGraph(1);
		if (!graphLoadingFailed) {
			graph2 = loadGraph(2);
			graph3 = loadGraph(3);
		}
		
		return null;
	}
	
	@Override
	protected void onPostExecute(Void res) {
		MainActivity.updating = false;
		
		if (!graphLoadingFailed) {
			if (graph1 != null)
				MainActivity.loadGraph(1, graph1, period, FieldType.DATA, graph1.getValuesUnit());
			if (graph2 != null)
				MainActivity.loadGraph(2, graph2, period, FieldType.DATA, graph2.getValuesUnit());
			if (graph3 != null)
				MainActivity.loadGraph(3, graph3, period, FieldType.TEMP, graph3.getValuesUnit());
		}
		
		if (graphLoadingFailed) {
			MainActivity.toggleSpinningMenuItem(false);
			MainActivity.graphLoadingFailed();
		}
		
		if (needAuth)
			MainActivity.displayNeedAuthScreen();
		if (needUpdate)
			MainActivity.displayFreeboxUpdateNeededScreen();
	}
	
	/**
	 * Load the graph and display the result on MainActivity
	 * PlotIndex from 1 to 3
	 * @param plotIndex
	 */
	private GraphsContainer loadGraph(int plotIndex) {
		ArrayList<Field> fields = new ArrayList<Field>();
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
		}
		
		NetResponse netResponse = NetHelper.loadGraph(freebox, period, fields);
		
		if (netResponse != null && netResponse.hasSucceeded()) {
			try {
				return new GraphsContainer(fields, netResponse.getJsonObject().getJSONArray("data"), fieldType, period);
			} catch (JSONException ex) {
				ex.printStackTrace();
				Crashlytics.logException(ex);
				return null;
			}
		} else {
			boolean cancel = true;
			
			if (netResponse != null) {
				FooBox.getInstance().getErrorsLogger().logError(netResponse);
				try {
					String response = netResponse.getCompleteResponse().getString("error_code");
					// If the session has expired / hasn't beed opened, open it
					
					if (response.equals("auth_required")) {
						cancel = false;
						new SessionOpener(freebox, FooBox.getInstance().getContext()).execute();
					} else if (response.equals("insufficient_rights")) {
						cancel = true;
						needAuth = true;
					} else if (response.equals("invalid_request")
							|| response.equals("invalid_api_version")) {
						cancel = true;
						needUpdate = true;
					}
				} catch (JSONException ex) {
					Crashlytics.logException(ex);
					ex.printStackTrace();
					return null;
				}
			} else {
				FooBox.getInstance().getErrorsLogger().logError("GraphLoader - error 401");
			}
			
			graphLoadingFailed = cancel;
			
			return null;
		}
	}
}