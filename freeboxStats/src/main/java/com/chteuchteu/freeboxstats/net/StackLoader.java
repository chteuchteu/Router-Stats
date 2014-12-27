package com.chteuchteu.freeboxstats.net;

import android.os.AsyncTask;

import com.chteuchteu.freeboxstats.hlpr.Enums.Field;
import com.chteuchteu.freeboxstats.hlpr.Enums.FieldType;
import com.chteuchteu.freeboxstats.hlpr.Enums.Period;
import com.chteuchteu.freeboxstats.obj.Freebox;
import com.chteuchteu.freeboxstats.obj.NetResponse;
import com.chteuchteu.freeboxstats.obj.StackContainer;
import com.chteuchteu.freeboxstats.ui.MainActivity;
import com.crashlytics.android.Crashlytics;

import org.json.JSONException;

import java.util.ArrayList;

public class StackLoader extends AsyncTask<Void, Void, Void> {
	private Freebox freebox;
	private boolean stackLoadingFailed;
	private StackContainer stackContainer;
	private Period period;
	
	public StackLoader(Freebox freebox, Period period) {
		this.freebox = freebox;
		this.stackLoadingFailed = false;
		this.period = period;
	}
	
	@Override
	protected Void doInBackground(Void... params) {
		this.stackContainer = loadData();
		
		return null;
	}
	
	@Override
	protected void onPostExecute(Void res) {
		MainActivity.updating = false;
		
		if (!stackLoadingFailed)
			MainActivity.loadGraph(5, this.stackContainer.getStackGraphsContainer(), period, FieldType.DATA,
					this.stackContainer.getStackGraphsContainer().getValuesUnit());
	}
	
	/**
	 * Load the graph and display the result on MainActivity
	 */
	private StackContainer loadData() {
		ArrayList<Field> fields = new ArrayList<>();
		
		fields.add(Field.RATE_DOWN);
		fields.add(Field.RATE_UP);
		
		NetResponse netResponse = NetHelper.loadGraph(freebox, period, fields, true);
		
		if (netResponse != null && netResponse.hasSucceeded()) {
			try {
				return new StackContainer(period, fields, netResponse.getJsonObject().getJSONArray("data"));
			} catch (JSONException ex) {
				ex.printStackTrace();
				Crashlytics.logException(ex);
				return null;
			}
		} else
			stackLoadingFailed = true;
		
		return null;
	}
}