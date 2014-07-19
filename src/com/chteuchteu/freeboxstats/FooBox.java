package com.chteuchteu.freeboxstats;

import org.json.JSONException;

import android.content.Context;
import android.util.Log;

import com.chteuchteu.freeboxstats.hlpr.Enums.Period;
import com.chteuchteu.freeboxstats.hlpr.SettingsHelper;
import com.chteuchteu.freeboxstats.hlpr.Util;
import com.chteuchteu.freeboxstats.net.BillingService;
import com.chteuchteu.freeboxstats.net.FreeboxDiscoverer;
import com.chteuchteu.freeboxstats.net.SessionOpener;
import com.chteuchteu.freeboxstats.obj.Freebox;
import com.chteuchteu.freeboxstats.obj.Session;

public class FooBox {
	public static final String APP_ID = "com.chteuchteu.freeboxstats";
	public static final String APP_NAME = "FreeboxStats";
	public static final String APP_VERSION = "1.0";
	public static final String DEVICE_NAME = "Android";
	
	private boolean premium;
	public static final boolean DEBUG = true;
	public static final boolean FORCE_NOTPREMIUM = true;
	
	private static FooBox instance;
	private Context context;
	
	private Freebox freebox;
	private String authToken;
	private int trackId;
	
	private Session session;
	
	private Period currentPeriod;
	
	private FooBox(Context context) {
		loadInstance(context);
	}
	
	private void loadInstance(Context context) {
		if (context != null)
			this.context = context;
		this.session = new Session();
		this.currentPeriod = Period.HOUR;
		// Init settings
		SettingsHelper.getInstance(context);
	}
	
	public static synchronized FooBox getInstance(Context context) {
		if (instance == null)
			instance = new FooBox(context);
		return instance;
	}
	
	public static FooBox getInstance() {
		return getInstance(null);
	}
	
	/**
	 * Checks if a Freebox has already been discovered on this network
	 * If not :
	 * 		-> ask for app_token
	 * Then, ask for auth_token.
	 */
	public void init() {
		MainActivity.displayLoadingScreen();
		MainActivity.appLoadingStep = 0;
		
		String savedFreebox = Util.getPrefString(this.context, "freebox");
		if (!savedFreebox.equals("")) {
			FooBox.log("Loading freebox from preferences");
			// Load freebox
			try {
				this.freebox = Freebox.load(savedFreebox);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			
			// Open session
			new SessionOpener(this.freebox).execute();
			// (once done, we'll update the graph)
			// Init BillingService
			BillingService.getInstance(this.context);
		} else {
			// Discover Freebox
			new FreeboxDiscoverer().execute();
			MainActivity.appLoadingStep++;
		}
	}
	
	public void reset() {
		Context context = this.context;
		instance = null;
		loadInstance(context);
	}
	
	public void saveAppToken(String appToken) {
		this.freebox.setAppToken(appToken);
		if (freebox.isAlreadySaved(context)) {
			try {
				this.freebox.save(this.context);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static void log(String msg) { log("FreeboxStats", msg); }
	public static void log(String key, String msg) { if (DEBUG) Log.v(key, msg); }
	
	public Freebox getFreebox() { return this.freebox; }
	public void setFreebox(Freebox val) { this.freebox = val; }
	
	public String getAuthToken() { return this.authToken; }
	public void setAuthToken(String val) { this.authToken = val; }
	
	public int getTrackId() { return this.trackId; }
	public void setTrackId(int val) { this.trackId = val; }
	
	public Session getSession() { return this.session; }
	
	public Context getContext() { return this.context; }
	
	public Period getPeriod() { return this.currentPeriod; }
	public void setPeriod(Period val) { this.currentPeriod = val; }
	
	public boolean isPremium() {
		if (FORCE_NOTPREMIUM)
			return false;
		return this.premium || DEBUG;
	}
	public void setIsPremium(boolean val) { this.premium = val; }
}