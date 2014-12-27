package com.chteuchteu.freeboxstats.obj;

import com.chteuchteu.freeboxstats.hlpr.Enums.Field;
import com.chteuchteu.freeboxstats.hlpr.Enums.Period;
import com.chteuchteu.freeboxstats.hlpr.Enums.Unit;
import com.chteuchteu.freeboxstats.hlpr.GraphHelper;
import com.chteuchteu.freeboxstats.hlpr.SettingsHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class StackGraphsContainer extends GraphsContainer {
	
	public StackGraphsContainer(ArrayList<Field> fields, JSONArray data, Period period) {
		super(fields, data, period);
		
		// Construct StackGraphsContainer from raw data from the Freebox
		this.period = period;
		this.serie = new ArrayList<>();
		this.dataSets = new ArrayList<>();
		this.valuesUnit = defaultUnit;
		
		for (Field f : fields)
			this.dataSets.add(new DataSet(f, valuesUnit));
		
		computeData(data, fields);
	}
	
	private void computeData(JSONArray data, ArrayList<Field> fields) {
		int timestampDiff = 0;
		try {
			timestampDiff = GraphHelper.getTimestampDiff(data);
		} catch (JSONException ex) {
			ex.printStackTrace();
		}
		
		// Adjust precision
		switch (SettingsHelper.getInstance().getGraphPrecision()) {
			case Max:
				// Do nothing
				break;
			case Medium:
				timestampDiff = timestampDiff*3;
				break;
			case Min:
				timestampDiff = timestampDiff*4;
				break;
			default:
				break;
		}
		
		int lastAddedTimestamp = -1;
		ValuesBuffer valuesBuffer = new ValuesBuffer(fields);
		for (int i=0; i<data.length(); i++) {
			try {
				JSONObject obj = (JSONObject) data.get(i);
				
				if (lastAddedTimestamp == -1)
					lastAddedTimestamp = obj.getInt("time");
				
				if (obj.getInt("time") - lastAddedTimestamp >= timestampDiff
						|| obj.getInt("time") - lastAddedTimestamp == 0
						|| timestampDiff == 0) {
					int time = obj.getInt("time") - lastAddedTimestamp;
					// Add the Numbers to the values lists
					for (Field f : fields) {
						try {
							int val = 0;
							if (obj.has(f.getSerializedValue()))
								val = obj.getInt(f.getSerializedValue());
							
							if (!valuesBuffer.isEmpty()) { // Include all the buffered values
								valuesBuffer.addValue(f, val);
								val = valuesBuffer.getAverage(f);
								valuesBuffer.clear(f);
							}
							
							getDataSet(f).stackValue(val*time);
						} catch (JSONException ex) { ex.printStackTrace(); }
					}
					lastAddedTimestamp = obj.getInt("time");
					
					this.serie.add(GraphHelper.getDateLabelFromTimestamp(obj.getLong("time"), this.period));
				} else {
					for (Field f : fields) {
						try {
							int val = 0;
							if (obj.has(f.getSerializedValue()))
								val = obj.getInt(f.getSerializedValue());
							valuesBuffer.addValue(f, val);
						} catch (JSONException ex) { ex.printStackTrace(); }
					}
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		
		long highestValue = GraphHelper.getHighestStackValue(this.dataSets);
		Unit bestUnit = GraphHelper.getBestUnitByMaxVal(highestValue);
		
		if (bestUnit != defaultUnit) {
			convertAllValues(defaultUnit, bestUnit);
			this.valuesUnit = bestUnit;
		}
	}
	
	/**
	 * Reverse order so the down graph has an higher z-index than the down graph
	 */
	@Override
	public ArrayList<DataSet> getDataSets() {
		ArrayList<DataSet> newDataSets = new ArrayList<>();
		
		for (int i = this.dataSets.size()-1; i>=0; i--)
			newDataSets.add(this.dataSets.get(i));
		
		return newDataSets;
	}
}