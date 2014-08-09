package com.chteuchteu.freeboxstats.hlpr;

import android.content.Context;

import com.chteuchteu.freeboxstats.hlpr.Enums.GraphPrecision;

public class SettingsHelper {
	private static SettingsHelper instance;
	private Context context;
	
	/* Settings */
	private boolean autoRefresh;
	private boolean displayXdslTab;
	private boolean enableZoom;
	private GraphPrecision graphPrecision;
	
	
	private SettingsHelper(Context context) {
		this.context = context;
		this.autoRefresh = Util.getPrefBoolean(context, "settings_autoRefresh", true);
		this.graphPrecision = GraphPrecision.get(Util.getPrefString(context, "settings_graphPrecision"));
		this.displayXdslTab = Util.getPrefBoolean(context, "settings_displayXdslTab", true);
		this.enableZoom = Util.getPrefBoolean(context, "settings_enableZoom", false);
	}
	
	public static synchronized SettingsHelper getInstance(Context context) {
		if (instance == null)
			instance = new SettingsHelper(context);
		return instance;
	}
	
	public static synchronized SettingsHelper getInstance() { return instance; }
	
	public boolean getAutoRefresh() { return this.autoRefresh; }
	public void setAutoRefresh(boolean val) {
		this.autoRefresh = val;
		Util.setPref(context, "settings_autoRefresh", val);
	}
	
	public GraphPrecision getGraphPrecision() { return this.graphPrecision; }
	public void setGraphPrecision(GraphPrecision val) {
		this.graphPrecision = val;
		Util.setPref(context, "settings_graphPrecision", val.getSerializedValue());
	}
	
	public boolean getDisplayXdslTab() { return this.displayXdslTab; }
	public void setDisplayXdslTab(boolean val) {
		this.displayXdslTab = val;
		Util.setPref(context, "settings_displayXdslTab", val);
	}
	
	public boolean getEnableZoom() { return this.enableZoom; }
	public void setEnableZoom(boolean val) {
		this.enableZoom = val;
		Util.setPref(context, "settings_enableZoom", val);
	}
}