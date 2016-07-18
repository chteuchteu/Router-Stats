package com.chteuchteu.freeboxstats.obj;

import com.chteuchteu.freeboxstats.hlpr.Enums;
import com.chteuchteu.freeboxstats.hlpr.Enums.Field;
import com.chteuchteu.freeboxstats.hlpr.Enums.FieldType;
import com.chteuchteu.freeboxstats.hlpr.Enums.Period;
import com.chteuchteu.freeboxstats.hlpr.Enums.Unit;
import com.chteuchteu.freeboxstats.hlpr.GraphHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class ValuesBag {
	private Field[] fields;
	private FieldType fieldType;

	private ArrayList<String> serie;
	private HashMap<Field, DataSet> dataSets;

	private long lastTimestamp;

	private Period period;
	private Unit valuesUnit;
	public static final Unit defaultUnit = Unit.Mo;

	public ValuesBag(Enums.Graph graph, Period period) {
		this.period = period;
		this.fields = deduceFields(graph);
		this.fieldType = deduceFieldType(graph);
		this.valuesUnit = deduceUnit(fieldType);
		this.serie = new ArrayList<>();
		this.dataSets = new HashMap<>();
		this.lastTimestamp = -1;

		for (Field field : fields)
			this.dataSets.put(field, new DataSet(field, valuesUnit));
	}

	public void clear() {
		for (DataSet dataSet : dataSets.values())
			dataSet.getValues().clear();

		lastTimestamp = -1;
		serie.clear();
	}

	private static Field[] deduceFields(Enums.Graph graph) {
		switch (graph) {
			case RateDown:
				return new Field[] { Field.RATE_DOWN, Field.BW_DOWN };
			case RateUp:
				return new Field[] { Field.RATE_UP, Field.BW_UP };
			case Temp:
				return new Field[] {
						Field.CPUM,
						Field.CPUB,
						Field.SW,
						Field.HDD
				};
			case XDSL:
				return new Field[] { Field.SNR_DOWN, Field.SNR_UP };
			case Switch1:
				return new Field[] { Field.RX_1, Field.TX_1 };
			case Switch2:
				return new Field[] { Field.RX_2, Field.TX_2 };
			case Switch3:
				return new Field[] { Field.RX_3, Field.TX_3 };
			case Switch4:
				return new Field[] { Field.RX_4, Field.TX_4 };
			default:
				return null;
		}
	}

	private static FieldType deduceFieldType(Enums.Graph graph) {
		switch (graph) {
			case RateDown:
			case RateUp:
			case Switch1:
			case Switch2:
			case Switch3:
			case Switch4:
				return FieldType.DATA;
			case Temp:
				return FieldType.TEMP;
			case XDSL:
				return FieldType.NOISE;
			default:
				return null;
		}
	}

	private static Unit deduceUnit(FieldType fieldType) {
		switch (fieldType) {
			case DATA:
				return defaultUnit;
			case TEMP:
				return Unit.C;
			case NOISE:
				return Unit.dB;
			default:
				return null;
		}
	}

	public void fill(JSONArray data) {
		// Clear previous datasets
		clear();

		int timestampDiff = 0;
		try {
			timestampDiff = GraphHelper.getTimestampDiff(data);
		} catch (JSONException ex) {
			ex.printStackTrace();
		}

		int lastAddedTimestamp = -1;
		ValuesBuffer valuesBuffer = new ValuesBuffer(fields);

		for (int i=0; i<data.length(); i++) {
			try {
				JSONObject obj = (JSONObject) data.get(i);
				int time = obj.getInt("time");

				// Skip already processed values
				if (lastTimestamp != -1 && time <= lastTimestamp)
					continue;
				// Keep "time" for upcoming request
				if (i == data.length()-1)
					this.lastTimestamp = time;

				if (lastAddedTimestamp == -1)
					lastAddedTimestamp = time;


				if (obj.getInt("time") - lastAddedTimestamp >= timestampDiff
						|| obj.getInt("time") - lastAddedTimestamp == 0
						|| timestampDiff == 0) {
					// Add the Numbers to the values lists
					for (Field field : fields) {
						try {
							int val = 0;
							if (obj.has(field.getSerializedValue()))
								val = obj.getInt(field.getSerializedValue());

							if (!valuesBuffer.isEmpty()) { // Include all the buffered values
								valuesBuffer.addValue(field, val);
								val = valuesBuffer.getAverage(field);
								valuesBuffer.clear(field);
							}

							DataSet dataSet = dataSets.get(field);
							if (fieldType == FieldType.DATA)
								dataSet.addValue(Unit.o, val);
							else if (fieldType == FieldType.TEMP)
								dataSet.addValue(Unit.C, val);
							else if (fieldType == FieldType.NOISE)
								dataSet.addValue(Unit.dB, val);
						} catch (JSONException ex) { ex.printStackTrace(); }
					}
					lastAddedTimestamp = obj.getInt("time");

					this.serie.add(GraphHelper.getDateLabelFromTimestamp(obj.getLong("time"), this.period));
				} else {
					// Dump buffers
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

		// Empty the valuesBuffer if needed
		if (!valuesBuffer.isEmpty()) {
			for (Field field : fields) {
				int val = valuesBuffer.getAverage(field);
				valuesBuffer.clear(field);

				DataSet dataSet = dataSets.get(field);
				if (fieldType == FieldType.DATA)
					dataSet.addValue(Unit.o, val);
				else if (fieldType == FieldType.TEMP)
					dataSet.addValue(Unit.C, val);
				else if (fieldType == FieldType.NOISE)
					dataSet.addValue(Unit.dB, val);
			}
			this.serie.add("");
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
	
	protected void convertAllValues(Unit from, Unit to) {
		if (this.fieldType == FieldType.TEMP)
			return;
		
		for (DataSet dataSet : this.dataSets.values())
			dataSet.setValuesUnit(to, true);
	}

	public void setPeriod(Period period) { this.period = period; }

	public ArrayList<String> getSerie() { return this.serie; }
	public DataSet[] getDataSets() {
		return this.dataSets.values().toArray(new DataSet[this.dataSets.values().size()]);
	}
	public Unit getValuesUnit() { return this.valuesUnit; }
	public Field[] getFields() { return this.fields; }
	public FieldType getFieldType() { return this.fieldType; }
	public long getLastTimestamp() { return this.lastTimestamp; }
}
