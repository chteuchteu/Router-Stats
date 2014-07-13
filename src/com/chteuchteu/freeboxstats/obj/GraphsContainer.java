package com.chteuchteu.freeboxstats.obj;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.chteuchteu.freeboxstats.hlpr.Enums.Field;
import com.chteuchteu.freeboxstats.hlpr.Enums.FieldType;
import com.chteuchteu.freeboxstats.hlpr.Enums.Period;
import com.chteuchteu.freeboxstats.hlpr.Enums.Unit;
import com.chteuchteu.freeboxstats.hlpr.GraphHelper;

public class GraphsContainer {
	private ArrayList<String> serie;
	private ArrayList<DataSet> dataSets;
	private Period period;
	public static final Period defaultPeriod = Period.HOUR;
	private Unit valuesUnit;
	public static final Unit defaultUnit = Unit.Mo;
	private FieldType fieldType;
	public static final FieldType defaultFieldType = FieldType.DATA;
	
	public GraphsContainer() {
		this.serie = new ArrayList<String>();
		this.dataSets = new ArrayList<DataSet>();
		this.valuesUnit = defaultUnit;
		this.fieldType = defaultFieldType;
		this.period = defaultPeriod;
	}
	
	public GraphsContainer(ArrayList<String> serie, ArrayList<DataSet> dataSets) {
		this.serie = serie;
		this.dataSets = dataSets;
		this.valuesUnit = defaultUnit;
		this.fieldType = defaultFieldType;
		this.period = defaultPeriod;
	}
	
	public GraphsContainer(ArrayList<Field> fields, JSONArray data, FieldType fieldType, Period period) {
		// Construct GraphsContainer from raw data from the Freebox
		this.period = period;
		this.serie = new ArrayList<String>();
		this.dataSets = new ArrayList<DataSet>();
		if (fieldType == FieldType.DATA)
			this.valuesUnit = defaultUnit;
		else
			this.valuesUnit = Unit.C;
		this.fieldType = fieldType;
		
		for (Field f : fields)
			this.dataSets.add(new DataSet(f, valuesUnit));
		
		int timestampDiff = 0;
		try {
			timestampDiff = getTimestampDiff(data);
		} catch (JSONException ex) {
			ex.printStackTrace();
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
							
							if (fieldType == FieldType.DATA)
								getDataSet(f).addValue(Unit.o, val);
							else if (fieldType == FieldType.TEMP)
								getDataSet(f).addValue(Unit.C, val);
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
		
		// Get the best values unit
		if (fieldType == FieldType.DATA) {
			int highestValue = GraphHelper.getHighestValue(data, fields);
			Unit bestUnit = GraphHelper.getBestUnitByMaxVal(highestValue);
			if (bestUnit != defaultUnit) {
				convertAllValues(defaultUnit, bestUnit);
				this.valuesUnit = bestUnit;
			}
		}
	}
	
	/**
	 * When skipping values (because the timestampdiff is not respected),
	 * we generate the values average for each Field.
	 */
	private class ValuesBuffer {
		private ArrayList<ValuesBufferSet> bufferSets;
		
		public ValuesBuffer(ArrayList<Field> fields) {
			this.bufferSets = new ArrayList<ValuesBufferSet>();
			for (Field f : fields)
				this.bufferSets.add(new ValuesBufferSet(f));
		}
		
		public void addValue(Field field, int val) {
			getVBSByField(field).addValue(val);
		}
		
		public void clear(Field field) {
			getVBSByField(field).clear();
		}
		
		public boolean isEmpty() {
			for (ValuesBufferSet vbs : this.bufferSets)
				if (!vbs.getValues().isEmpty())
					return false;
			return true;
		}
		
		public int getAverage(Field field) {
			ValuesBufferSet vbs = getVBSByField(field);
			float sum = 0;
			int nb = vbs.getValues().size();
			for (int n : vbs.getValues())
				sum += n;
			return (int) (sum/nb);
		}
		
		private ValuesBufferSet getVBSByField(Field field) {
			for (ValuesBufferSet vbs : this.bufferSets) {
				if (vbs.getField() == field)
					return vbs;
			}
			return null;
		}
		
		private class ValuesBufferSet {
			private Field field;
			private ArrayList<Integer> values;
			public ValuesBufferSet(Field field) {
				this.field = field;
				this.values = new ArrayList<Integer>();
			}
			public void addValue(int val) { this.values.add(val); }
			public ArrayList<Integer> getValues() { return this.values; }
			public Field getField() { return this.field; }
			public void clear() { this.values.clear(); }
		}
	}
	
	private int getTimestampDiff(JSONArray data) throws JSONException {
		// For every period > HOUR, the time between 2 values
		// becomes smaller at 3/4 from the beginning.
		// We'll try to respect those.
		int timestamp0 = ((JSONObject) data.get(0)).getInt("time");
		int timestamp1 = ((JSONObject) data.get(1)).getInt("time");
		int timestampDiff = timestamp1 - timestamp0;
		
		// Try with the next value (sometimes, the result returned is wrong)
		int timestamp2 = ((JSONObject) data.get(2)).getInt("time");
		int timestamp3 = ((JSONObject) data.get(3)).getInt("time");
		int timestampDiff2 = timestamp3 - timestamp2;
		
		if (timestampDiff == timestampDiff2)
			return timestampDiff;
		else
			return timestampDiff2;
	}
	
	private void convertAllValues(Unit from, Unit to) {
		if (this.fieldType == FieldType.TEMP)
			return;
		
		for (DataSet ds : this.dataSets)
			ds.setValuesUnit(to, true);
	}
	
	public void setSerie(ArrayList<String> val) { this.serie = val; }
	public ArrayList<String> getSerie() { return this.serie; }
	
	public void addDataSet(DataSet val) { this.dataSets.add(val); }
	public void addDataSet(Field field, JSONArray jsonArray, Unit valuesUnit) { this.dataSets.add(new DataSet(field, jsonArray, valuesUnit)); }
	public DataSet getDataSet(Field field) {
		for (DataSet ds : this.dataSets) {
			if (ds.getField().equals(field))
				return ds;
		}
		return null;
	}
	public ArrayList<DataSet> getDataSets() { return this.dataSets; }
	public ArrayList<Field> getFields() {
		ArrayList<Field> fields = new ArrayList<Field>();
		for (DataSet ds : this.dataSets)
			fields.add(ds.getField());
		return fields;
	}
	public Unit getValuesUnit() { return this.valuesUnit; }
}