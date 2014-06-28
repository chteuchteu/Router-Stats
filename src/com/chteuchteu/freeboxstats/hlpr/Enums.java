package com.chteuchteu.freeboxstats.hlpr;

import android.annotation.SuppressLint;

@SuppressLint("DefaultLocale")
public class Enums {
	public enum AuthorizeStatus {
		UNKNOWN, PENDING, TIMEOUT, GRANTED, DENIED;
		public String getSerializedValue() { return this.name().toLowerCase(); }
		public static AuthorizeStatus get(String serializedValue) {
			for (AuthorizeStatus as : AuthorizeStatus.values()) {
				if (as.getSerializedValue().equals(serializedValue))
					return as;
			}
			return null;
		}
	}
	
	public enum Period { HOUR, DAY, WEEK, MONTH }
	
	public enum Db { NET, TEMP, DSL, SWITCH }
	public enum Field {
		BW_UP, BW_DOWN, RATE_UP, RATE_DOWN, VPN_RATE_UP, VPN_RATE_DOWN,
		CPUM, CPUB, SW, HDD, FAN_SPEED, TEMP1, TEMP2, TEMP3,
		DSL_RATE_UP, DSL_RATE_DOWN, SNR_UP, SNR_DOWN,
		RW_1, TX_1, RX_2, TX_2, RX_3, TX_3, RX_4, TX_4;
		
		public String getSerializedValue() { return this.name().toLowerCase(); }
		public Field get(String serializedValue) {
			for (Field field : Field.values()) {
				if (field.getSerializedValue().equals(serializedValue))
					return field;
			}
			return null;
		}
		public boolean equals(Field f1) { return this.getSerializedValue().equals(f1.getSerializedValue()); }
	}
}