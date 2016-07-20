package com.chteuchteu.freeboxstats.hlpr;

import android.annotation.SuppressLint;

import com.chteuchteu.freeboxstats.hlpr.Enums.Db;
import com.chteuchteu.freeboxstats.hlpr.Enums.Field;
import com.chteuchteu.freeboxstats.hlpr.Enums.Period;
import com.chteuchteu.freeboxstats.hlpr.Enums.Unit;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;

public class GraphHelper {
	private static final int UnitTreshold = 600;

	@SuppressLint("SimpleDateFormat")
	public static String getDateLabelFromTimestamp(long jsonTimestamp, Period period) {
		try {
			Date date = new Date((jsonTimestamp * 1000));
			SimpleDateFormat dateFormat;
			if (period == Period.HOUR || period == Period.DAY)
				dateFormat = new SimpleDateFormat("kk:mm");
			else if (period == Period.WEEK)
				dateFormat = new SimpleDateFormat("dd/MM:kk");
			else if (period == Period.MONTH) {
				//dateFormat = new SimpleDateFormat("dd/MM");
				return String.valueOf(jsonTimestamp);
			} else// (period == null)
				dateFormat = new SimpleDateFormat("dd/MM");
			
			return dateFormat.format(date);
		} catch (Exception ex) {
			ex.printStackTrace();
			return "";
		}
	}
	
	/**
	 * Returns net / temp / dsl / switch from field
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
			case RX_1:
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
	public static Unit getBestUnitByMaxVal(double maxVal, Unit maxValUnit) {
		Number valueKo = Util.convertUnit(maxValUnit, Unit.ko, maxVal);
		if (valueKo.doubleValue() <= UnitTreshold)
			return Unit.ko;
		
		Number valueMo = Util.convertUnit(maxValUnit, Unit.Mo, maxVal);
		if (valueMo.doubleValue() <= UnitTreshold)
			return Unit.Mo;
		
		Number valueGo = Util.convertUnit(maxValUnit, Unit.Go, maxVal);
		if (valueGo.doubleValue() <= UnitTreshold)
			return Unit.Go;
		
		return Unit.To;
	}

	public static int getTimestampDiff(JSONArray data) throws JSONException {
		// For every period > HOUR, the time between 2 values
		// becomes smaller at 3/4 from the beginning.
		// We'll try to respect those.
		int timestamp0 = ((JSONObject) data.get(0)).getInt("time");
		int timestamp1 = ((JSONObject) data.get(1)).getInt("time");
		int timestampDiff = timestamp1 - timestamp0;

		if (data.length() < 4)
			return timestampDiff;
		
		// Try with the next value (sometimes, the result returned is wrong)
		int timestamp2 = ((JSONObject) data.get(2)).getInt("time");
		int timestamp3 = ((JSONObject) data.get(3)).getInt("time");
		int timestampDiff2 = timestamp3 - timestamp2;
		
		if (timestampDiff == timestampDiff2)
			return timestampDiff;
		else
			return timestampDiff2;
	}
}
