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

	private static Unit getUnit(Enums.FieldType fieldType) {
		switch (fieldType) {
			case DATA:
				return Unit.o;
			case TEMP:
				return Unit.C;
			case NOISE:
				return Unit.dB;
			default:
				return null;
		}
	}

	public void addValue(Enums.FieldType fieldType, int value) {
		Unit unit = getUnit(fieldType);

		if (unit == Unit.C)
			this.values.add(value/10);
		else if (unit == Unit.dB)
			this.values.add((double)value/10);
		else
			this.values.add(Util.convertUnit(unit, valuesUnit, value));
	}

	public void setValuesUnit(Unit unit, boolean convertAll) {
		if (convertAll) {
			ArrayList<Number> newValues = new ArrayList<>();
			for (Number number : this.values)
				newValues.add(Util.convertUnit(valuesUnit, unit, number.doubleValue()));
			this.values = newValues;
		}
		this.valuesUnit = unit;
	}


	public SimpleXYSeries getXySerieRef() {
		return xySerieRef;
	}
	public void setXySerieRef(SimpleXYSeries xySerieRef) {
		this.xySerieRef = xySerieRef;
	}
}
