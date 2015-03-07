package com.chteuchteu.freeboxstats.hlpr;

import android.annotation.SuppressLint;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
		HOUR, DAY, WEEK, MONTH;
		public int getIndex() { return Arrays.asList(Period.values()).indexOf(this); }
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
		RX_1("Down SW1"), TX_1("Up SW1"), RX_2("Down SW2"), TX_2("Up SW2"),
		RX_3("Down SW3"), TX_3("Up SW3"), RX_4("Down SW3"), TX_4("Up SW4");
		
		private String displayName;
		Field() { this.displayName = getSerializedValue(); }
		Field(String displayName) { this.displayName = displayName; }
		public String getSerializedValue() { return this.name().toLowerCase(); }
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
		private static final GraphPrecision defaultValue = GraphPrecision.Medium;
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
			ArrayList<String> list = new ArrayList<>();
			for (GraphPrecision g : GraphPrecision.values())
				list.add(g.getLabel());
			return list;
		}
	}

	public enum SpecialBool {
		TRUE, FALSE, UNKNOWN;
		public static SpecialBool get(String serializedValue) {
			for (SpecialBool specialBool : SpecialBool.values()) {
				if (specialBool.getSerializedValue().equals(serializedValue))
					return specialBool;
			}
			return UNKNOWN;
		}
		public String getSerializedValue() { return this.name().toLowerCase(); }
	}

	public enum SwitchStatusLink { UP, DOWN, UNKNOWN }
}
