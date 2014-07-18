package com.chteuchteu.freeboxstats.hlpr;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.util.Log;

import com.chteuchteu.freeboxstats.hlpr.Enums.Db;
import com.chteuchteu.freeboxstats.hlpr.Enums.Field;
import com.chteuchteu.freeboxstats.hlpr.Enums.Period;
import com.chteuchteu.freeboxstats.hlpr.Enums.Unit;

public class GraphHelper {
	@SuppressLint("SimpleDateFormat")
	public static ArrayList<String> getDatesLabelsFromData(JSONArray dataArray) {
		ArrayList<String> list = new ArrayList<String>();
		for (int i=0; i<dataArray.length(); i++) {
			try {
				JSONObject jsonObj = dataArray.getJSONObject(i);
				long timestamp = jsonObj.getLong("time")*1000;
				Date date = new Date(timestamp);
				SimpleDateFormat dateFormat = new SimpleDateFormat("kk:mm");
				list.add(dateFormat.format(date));
			} catch (Exception ex) {
				ex.printStackTrace();
				list.add("");
			}
		}
		return list;
	}
	
	public static String getDateLabelFromTimestamp(long jsonTimestamp, Period period) {
		try {
			long timestamp = jsonTimestamp*1000;
			Date date = new Date(timestamp);
			SimpleDateFormat dateFormat;
			if (period == Period.HOUR || period == Period.DAY)
				dateFormat = new SimpleDateFormat("kk:mm");
			else if (period == Period.WEEK)
				dateFormat = new SimpleDateFormat("dd/MM:kk");
			else if (period == Period.MONTH) {
				dateFormat = new SimpleDateFormat("dd/MM");
				return String.valueOf(jsonTimestamp);
			} else// (period == null)
				dateFormat = new SimpleDateFormat("dd/MM");
			
			return dateFormat.format(date);
		} catch (Exception ex) {
			ex.printStackTrace();
			return "";
		}
	}
	
	public static Date getDateFromString(String s) {
		try {
			return new SimpleDateFormat("dd/MM").parse(s);
		} catch (ParseException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Returns net / temp / dsl / switch from field
	 * @param field
	 * @return
	 */
	public static Db getDbFromField(Field field) {
		switch (field) {
			case BW_UP:
			case BW_DOWN:
			case RATE_UP:
			case RATE_DOWN:
			case VPN_RATE_UP:
			case VPN_RATE_DOWN:
				return Db.NET;
			case CPUM:
			case CPUB:
			case SW:
			case HDD:
			case FAN_SPEED:
				return Db.TEMP;
			case DSL_RATE_UP:
			case DSL_RATE_DOWN:
			case SNR_UP:
			case SNR_DOWN:
				return Db.DSL;
			case RW_1:
			case TX_1:
			case RX_2:
			case TX_2:
			case RX_3:
			case TX_3:
			case RX_4:
			case TX_4:
				return Db.SWITCH;
		}
		return null;
	}
	
	/**
	 * Returns the best unit depending on the highest value
	 */
	public static Unit getBestUnitByMaxVal(int maxVal) {
		Number valueKo = Util.convertUnit(Unit.o, Unit.ko, maxVal);
		Log.v("", "valueKo = " + valueKo);
		if (valueKo.doubleValue() <= 600)
			return Unit.ko;
		
		Number valueMo = Util.convertUnit(Unit.o, Unit.Mo, maxVal);
		Log.v("", "valueMo = " + valueMo);
		if (valueMo.doubleValue() <= 600)
			return Unit.Mo;
		
		Number valueGo = Util.convertUnit(Unit.o, Unit.Go, maxVal);
		Log.v("", "valueGo = " + valueGo);
		if (valueGo.doubleValue() <= 600)
			return Unit.Go;
		
		return Unit.To;
	}
	
	public static int getHighestValue(JSONArray dataArray, ArrayList<Field> fields) {
		int highestValue = 0;
		for (int i=0; i<dataArray.length(); i++) {
			try {
				JSONObject jsonObj = dataArray.getJSONObject(i);
				for (Field field : fields) {
					if (jsonObj.has(field.getSerializedValue())) {
						int val = jsonObj.getInt(field.getSerializedValue());
						if (val > highestValue)
							highestValue = val;
					}
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		return highestValue;
	}
}