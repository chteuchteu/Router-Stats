package com.chteuchteu.freeboxstats.hlpr;

import android.content.Context;

public class SettingsHelper {
	private static SettingsHelper instance;
	private Context context;
	
	/* Settings */
	private boolean autoRefresh;
	private boolean displayXdslTab;
	
	
	private SettingsHelper(Context context) {
		this.context = context;
		this.autoRefresh = Util.getPrefBoolean(context, "settings_autoRefresh", true);
		this.displayXdslTab = Util.getPrefBoolean(context, "settings_displayXdslTab", false);
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
	
	public boolean getDisplayXdslTab() { return this.displayXdslTab; }
	public void setDisplayXdslTab(boolean val) {
		this.displayXdslTab = val;
		Util.setPref(context, "settings_displayXdslTab", val);
	}
}
