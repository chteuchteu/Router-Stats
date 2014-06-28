package com.chteuchteu.freeboxstats.obj;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.chteuchteu.freeboxstats.hlpr.Enums.Field;

public class DataSet {
	private Field field;
	private ArrayList<Number> values;
	
	public DataSet(Field field) {
		this.field = field;
		this.values = new ArrayList<Number>();
	}
	
	public DataSet(Field field, ArrayList<Number> values) {
		this.field = field;
		this.values = values;
	}
	
	public DataSet(Field field, JSONArray jsonArray) {
		this.values = new ArrayList<Number>();
		try {
			for (int i=0; i<jsonArray.length(); i++) {
				JSONObject jsonObj = jsonArray.getJSONObject(i);
				if (jsonObj.has(field.getSerializedValue()))
					this.values.add(jsonObj.getInt(field.getSerializedValue()));
				else
					this.values.add(0);
			}
		} catch (JSONException e) { e.printStackTrace(); }
	}
	
	public Field getField() { return this.field; }
	public ArrayList<Number> getValues() { return this.values; }
}