package com.chteuchteu.freeboxstats.net;

import java.util.ArrayList;

import org.json.JSONException;

import android.os.AsyncTask;

import com.chteuchteu.freeboxstats.MainActivity;
import com.chteuchteu.freeboxstats.hlpr.Enums.Field;
import com.chteuchteu.freeboxstats.hlpr.Enums.FieldType;
import com.chteuchteu.freeboxstats.hlpr.Enums.Period;
import com.chteuchteu.freeboxstats.obj.Freebox;
import com.chteuchteu.freeboxstats.obj.GraphsContainer;
import com.chteuchteu.freeboxstats.obj.NetResponse;

@Deprecated
public class GraphLoader extends AsyncTask<Void, Void, Void> {
	private Freebox freebox;
	private Period period;
	private ArrayList<Field> fields;
	private GraphsContainer graphsContainer;
	private int plotIndex;
	private FieldType fieldType;
	private NetResponse netResponse;
	
	public GraphLoader(Freebox freebox, Period period, ArrayList<Field> fields, int plotIndex, FieldType fieldType) {
		this.freebox = freebox;
		this.period = period;
		this.fields = fields;
		this.plotIndex = plotIndex;
		this.fieldType = fieldType;
	}
	
	@Override
	protected Void doInBackground(Void... params) {
		netResponse = NetHelper.loadGraph(freebox, period, fields);
		if (netResponse != null && netResponse.hasSucceeded()) {
			try {
				graphsContainer = new GraphsContainer(fields, netResponse.getJsonObject().getJSONArray("data"), fieldType, period);
			} catch (JSONException e) { e.printStackTrace(); }
		}
		
		return null;
	}
	
	@Override
	protected void onPostExecute(Void res) {
		super.onPostExecute(res);
		
		if (graphsContainer != null)
			MainActivity.loadGraph(plotIndex, graphsContainer, period, fieldType, graphsContainer.getValuesUnit());
		else {
			boolean cancel = true;
			
			if (netResponse != null) {
				try {
					String response = netResponse.getCompleteResponse().getString("error_code");
					if (response.equals("auth_required")) {
						cancel = false;
						new SessionOpener(freebox, MainActivity.context).execute();
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
				
			}
			
			if (cancel) {
				MainActivity.toggleSpinningMenuItem(false);
				MainActivity.graphLoadingFailed();
			}
		}
	}
}