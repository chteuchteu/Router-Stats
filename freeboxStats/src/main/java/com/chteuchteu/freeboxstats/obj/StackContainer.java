package com.chteuchteu.freeboxstats.obj;

import com.chteuchteu.freeboxstats.hlpr.Enums.Field;
import com.chteuchteu.freeboxstats.hlpr.Enums.Period;

import org.json.JSONArray;

import java.util.ArrayList;

public class StackContainer {
	private Period period;
	private ArrayList<Field> fields;
	
	private StackGraphsContainer stackGraphsContainer;
	
	public StackContainer(Period period, ArrayList<Field> fields, JSONArray data) {
		this.period = period;
		this.fields = fields;
		
		computeData(data);
	}
	
	private void computeData(JSONArray data) {
		// Compute stack
		this.stackGraphsContainer = new StackGraphsContainer(fields, data, period);
	}

	public StackGraphsContainer getStackGraphsContainer() { return this.stackGraphsContainer; }
}
