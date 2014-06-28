package com.chteuchteu.freeboxstats.obj;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.chteuchteu.freeboxstats.hlpr.Enums.Field;
import com.chteuchteu.freeboxstats.hlpr.GraphHelper;

public class GraphsContainer {
	private ArrayList<String> serie;
	private ArrayList<DataSet> dataSets;
	
	public GraphsContainer() {
		this.serie = new ArrayList<String>();
		this.dataSets = new ArrayList<DataSet>();
	}
	
	public GraphsContainer(ArrayList<String> serie, ArrayList<DataSet> dataSets) {
		this.serie = serie;
		this.dataSets = dataSets;
	}
	
	public GraphsContainer(ArrayList<Field> fields, JSONArray data) {
		// Construct things from raw data from the Freebox
		this.serie = new ArrayList<String>();
		this.dataSets = new ArrayList<DataSet>();
		
		for (Field f : fields)
			this.dataSets.add(new DataSet(f));
		
		for (int i=0; i<data.length(); i++) {
			try {
				JSONObject obj = (JSONObject) data.get(i);
				for (Field f : fields) {
					try {
						getDataSet(f).getValues().add(obj.getInt(f.getSerializedValue()));
					} catch (Exception ex) { ex.printStackTrace(); }
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		
		this.serie.addAll(GraphHelper.getDatesLabelsFromData(data));
	}
	
	public void setSerie(ArrayList<String> val) { this.serie = val; }
	public ArrayList<String> getSerie() { return this.serie; }
	
	public void addDataSet(DataSet val) { this.dataSets.add(val); }
	public void addDataSet(Field field, JSONArray jsonArray) { this.dataSets.add(new DataSet(field, jsonArray)); }
	public DataSet getDataSet(Field field) {
		for (DataSet ds : this.dataSets) {
			Log.v("", ds.getField().getSerializedValue() + " vs " + field.getSerializedValue());
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
}