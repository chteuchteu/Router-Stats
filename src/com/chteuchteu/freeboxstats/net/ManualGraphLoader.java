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

public class ManualGraphLoader extends AsyncTask<Void, Void, Void> {
	private Freebox freebox;
	private Period period;
	
	public ManualGraphLoader(Freebox freebox, Period period) {
		this.freebox = freebox;
		this.period = period;
	}
	
	@Override
	protected Void doInBackground(Void... params) {
		
		loadGraph(1);
		loadGraph(2);
		loadGraph(3);
		
		return null;
	}
	
	/**
	 * Load the graph and display the result on MainActivity
	 * PlotIndex from 1 to 3
	 * @param plotIndex
	 */
	private void loadGraph(int plotIndex) {
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
				GraphsContainer graphsContainer = new GraphsContainer(fields, netResponse.getJsonObject().getJSONArray("data"), fieldType, period);
				
				MainActivity.loadGraph(plotIndex, graphsContainer, period, fieldType, graphsContainer.getValuesUnit());
			} catch (JSONException e) { e.printStackTrace(); }
		} else {
			boolean cancel = true;
			
			if (netResponse != null) {
				try {
					String response = netResponse.getCompleteResponse().getString("error_code");
					// If the session has expired / hasn't beed opened, open it
					if (response.equals("auth_required")) {
						cancel = false;
						new SessionOpener(freebox).execute();
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