package com.chteuchteu.freeboxstats.obj;

import com.chteuchteu.freeboxstats.hlpr.Enums.Field;

import java.util.ArrayList;

/**
 * When skipping values (because the timestampdiff is not respected),
 * we generate the values average (or sum) for each Field.
 */
public class ValuesBuffer {
	private ArrayList<ValuesBufferSet> bufferSets;
	
	public ValuesBuffer(ArrayList<Field> fields) {
		this.bufferSets = new ArrayList<>();
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

	/**
	 * Generate buffer average (standard graph)
	 * @param field Field
	 * @return (int) sum
	 */
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
			this.values = new ArrayList<>();
		}

		public void addValue(int val) { this.values.add(val); }
		public ArrayList<Integer> getValues() { return this.values; }
		public Field getField() { return this.field; }
		public void clear() { this.values.clear(); }
	}
}
