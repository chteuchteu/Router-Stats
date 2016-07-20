package com.chteuchteu.freeboxstats.obj;

import com.androidplot.xy.SimpleXYSeries;
import com.chteuchteu.freeboxstats.hlpr.Enums;
import com.chteuchteu.freeboxstats.hlpr.Enums.Field;
import com.chteuchteu.freeboxstats.hlpr.Enums.Unit;
import com.chteuchteu.freeboxstats.hlpr.Util;

import java.util.ArrayList;

public class DataSet {
	private Field field;
	private Unit valuesUnit;
	private ArrayList<Number> values;
	private SimpleXYSeries xySerieRef;
	
	public DataSet(Field field, Unit valuesUnit) {
		this.field = field;
		this.values = new ArrayList<>();
		this.valuesUnit = valuesUnit;
	}
	
	public Field getField() { return this.field; }
	/**
	 * Get this DataSet values.
	 * This shouldn't be used to add values, use addValue(Unit, int) instead
	 */
	public ArrayList<Number> getValues() { return this.values; }

	public void addValue(Enums.FieldType fieldType, int value, Unit valueUnit) {
		switch (fieldType) {
			case TEMP:
				this.values.add(value/10);
				break;
			case NOISE:
				this.values.add((double)value/10);
				break;
			case DATA:
				if (valueUnit == null)
					throw new RuntimeException("You must provide Unit for FieldType.DATA");

				this.values.add(Util.convertUnit(valueUnit, this.valuesUnit, value));
				break;
		}
	}

	/**
	 * Updates values unit & convert all values
     */
	public void setValuesUnit(Unit toUnit) {
		ArrayList<Number> newValues = new ArrayList<>();
		for (Number number : this.values)
			newValues.add(Util.convertUnit(this.valuesUnit, toUnit, number.doubleValue()));
		this.values = newValues;

		this.valuesUnit = toUnit;
	}


	public SimpleXYSeries getXySerieRef() {
		return xySerieRef;
	}
	public void setXySerieRef(SimpleXYSeries xySerieRef) {
		this.xySerieRef = xySerieRef;
	}
}
