package com.chteuchteu.freeboxstats.hlpr;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;

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
	
	public enum Period {
		HOUR, DAY, WEEK, MONTH, TODAY;
		public int getIndex() { return Arrays.asList(Period.values()).indexOf(this); }
		public static Period get(int index) { return Period.values()[index]; }
		public String getLabel() {
			switch (getIndex()) {
				case 0: return "Heure";
				case 1: return "Jour";
				case 2: return "Semaine";
				case 3: return "Mois";
				default: return "";
			}
		}
	}
	
	public enum Db {
		NET, TEMP, DSL, SWITCH;
		public String getSerializedValue() { return this.name().toLowerCase(); }
	}
	public enum FieldType { DATA, TEMP, NOISE }
	public enum Field {
		BW_UP("Débit maximum"), BW_DOWN("Débit maximum"), RATE_UP("Débit up"), RATE_DOWN("Débit down"), VPN_RATE_UP, VPN_RATE_DOWN,
		CPUM("CpuM"), CPUB("CpuB"), SW("SW"), HDD("HDD"), FAN_SPEED("Ventilateur"),
		DSL_RATE_UP, DSL_RATE_DOWN, SNR_UP("Upload"), SNR_DOWN("Download"),
		RW_1, TX_1, RX_2, TX_2, RX_3, TX_3, RX_4, TX_4;
		
		private String displayName;
		Field() { this.displayName = getSerializedValue(); }
		Field(String displayName) { this.displayName = displayName; }
		public String getSerializedValue() { return this.name().toLowerCase(); }
		public Field get(String serializedValue) {
			for (Field field : Field.values()) {
				if (field.getSerializedValue().equals(serializedValue))
					return field;
			}
			return null;
		}
		public boolean equals(Field f1) { return this.getSerializedValue().equals(f1.getSerializedValue()); }
		public String getDisplayName() { return this.displayName; }
	}
	
	public enum Unit {
		dB(-2), C(-1), o(0), ko(1), Mo(2), Go(3), To(4);
		private int index;
		Unit(int index) { this.index = index; }
		public int getIndex() { return this.index; }
	}
	
	public enum GraphPrecision {
		Max("Maximale"), Medium("Moyenne"), Min("Minimum");
		private String label;
		private static final GraphPrecision defaultValue = GraphPrecision.Max;
		GraphPrecision(String label) { this.label = label; }
		public String getLabel() { return this.label; }
		public String getSerializedValue() { return this.name().toLowerCase(); }
		public static GraphPrecision get(String serializedValue) {
			for (GraphPrecision g : GraphPrecision.values()) {
				if (g.getSerializedValue().equals(serializedValue))
					return g;
			}
			return defaultValue;
		}
		public static GraphPrecision get(int index) { return GraphPrecision.values()[index]; }
		public int getIndex() { return Arrays.asList(GraphPrecision.values()).indexOf(this); }
		public static List<String> getStringArray() {
			ArrayList<String> list = new ArrayList<String>();
			for (GraphPrecision g : GraphPrecision.values())
				list.add(g.getLabel());
			return list;
		}
	}
	
	public enum ApplicationTheme {
		LIGHT, DARK;
		public static ApplicationTheme getApplicationTheme(Context context) {
			String appTheme = Util.getPrefString(context, "settings_applicationTheme", "DARK");
			if (appTheme.equals("LIGHT"))
				return ApplicationTheme.LIGHT;
			else
				return ApplicationTheme.DARK;
		}
		public static ApplicationTheme get(boolean lightTheme) {
			if (lightTheme)
				return ApplicationTheme.LIGHT;
			else
				return ApplicationTheme.DARK;
		}
	}
}