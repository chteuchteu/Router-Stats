package com.chteuchteu.freeboxstats.obj;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.chteuchteu.freeboxstats.hlpr.Enums.Field;
import com.chteuchteu.freeboxstats.hlpr.Enums.Unit;
import com.chteuchteu.freeboxstats.hlpr.GraphHelper;

public class GraphsContainer {
	private ArrayList<String> serie;
	private ArrayList<DataSet> dataSets;
	private Unit valuesUnit;
	public static final Unit defaultUnit = Unit.Mo;
	
	public GraphsContainer() {
		this.serie = new ArrayList<String>();
		this.dataSets = new ArrayList<DataSet>();
		this.valuesUnit = defaultUnit;
	}
	
	public GraphsContainer(ArrayList<String> serie, ArrayList<DataSet> dataSets) {
		this.serie = serie;
		this.dataSets = dataSets;
		this.valuesUnit = defaultUnit;
	}
	
	public GraphsContainer(ArrayList<Field> fields, JSONArray data) {
		// Construct things from raw data from the Freebox
		this.serie = new ArrayList<String>();
		this.dataSets = new ArrayList<DataSet>();
		this.valuesUnit = defaultUnit;
		
		for (Field f : fields)
			this.dataSets.add(new DataSet(f, valuesUnit));
		
		for (int i=0; i<data.length(); i++) {
			try {
				JSONObject obj = (JSONObject) data.get(i);
				for (Field f : fields) {
					try {
						getDataSet(f).addValue(Unit.o, obj.getInt(f.getSerializedValue()));
					} catch (JSONException ex) { ex.printStackTrace(); }
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		
		// Get the best values unit
		int highestValue = GraphHelper.getHighestValue(data, fields);
		Unit bestUnit = GraphHelper.getBestUnitByMaxVal(highestValue);
		if (bestUnit != defaultUnit) {
			convertAllValues(defaultUnit, bestUnit);
			this.valuesUnit = bestUnit;
		}
		
		this.serie.addAll(GraphHelper.getDatesLabelsFromData(data));
	}
	
	private void convertAllValues(Unit from, Unit to) {
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