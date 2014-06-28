package com.chteuchteu.freeboxstats.hlpr;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONObject;

import android.annotation.SuppressLint;

import com.chteuchteu.freeboxstats.hlpr.Enums.Db;
import com.chteuchteu.freeboxstats.hlpr.Enums.Field;

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
			case TEMP1:
			case TEMP2:
			case TEMP3:
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
}