package com.chteuchteu.freeboxstats.obj;

import com.chteuchteu.freeboxstats.hlpr.Enums.Field;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * When skipping values (because the timestampdiff is not respected),
 * we generate the values average (or sum) for each Field.
 */
public class ValuesBuffer {
    private HashMap<Field, ValuesBufferSet> bufferSets;

    public ValuesBuffer(Field[] fields) {
        this.bufferSets = new HashMap<>();
        for (Field field : fields)
            this.bufferSets.put(field, new ValuesBufferSet());
    }

    public void addValue(Field field, int val) {
        this.bufferSets.get(field).addValue(val);
    }

    public void clear(Field field) {
        this.bufferSets.get(field).clear();
    }

    public boolean isEmpty() {
        for (ValuesBufferSet vbs : this.bufferSets.values())
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
        ValuesBufferSet valuesBufferSet = this.bufferSets.get(field);
        float sum = 0;
        int nb = valuesBufferSet.getValues().size();
        for (int n : valuesBufferSet.getValues())
            sum += n;
        return (int) (sum/nb);
    }

    private class ValuesBufferSet {
        private ArrayList<Integer> values;
        public ValuesBufferSet() {
            this.values = new ArrayList<>();
        }
        public void addValue(int val) { this.values.add(val); }
        public ArrayList<Integer> getValues() { return this.values; }
        public void clear() { this.values.clear(); }
    }
}
