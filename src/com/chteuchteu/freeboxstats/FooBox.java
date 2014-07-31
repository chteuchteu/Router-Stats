package com.chteuchteu.freeboxstats;

import org.json.JSONException;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.util.Log;

import com.chteuchteu.freeboxstats.hlpr.Enums.Period;
import com.chteuchteu.freeboxstats.hlpr.SettingsHelper;
import com.chteuchteu.freeboxstats.hlpr.Util;
import com.chteuchteu.freeboxstats.net.FreeboxDiscoverer;
import com.chteuchteu.freeboxstats.net.SessionOpener;
import com.chteuchteu.freeboxstats.obj.ErrorsLogger;
import com.chteuchteu.freeboxstats.obj.Freebox;
import com.chteuchteu.freeboxstats.obj.Session;
import com.crashlytics.android.Crashlytics;

public class FooBox extends Application {
	public static final String APP_ID = "com.chteuchteu.freeboxstats";
	public static final String APP_NAME = "FreeboxStats";
	public static final String DEVICE_NAME = "Android";
	
	private boolean inited;
	
	public enum Premium { TRUE, FALSE, UNKNOWN }
	private Premium premium;
	public static final boolean DEBUG = true;
	public static final boolean FORCE_NOTPREMIUM = false;
	
	private static FooBox instance;
	private Context context;
	
	private Freebox freebox;
	private String authToken;
	private int trackId;
	private ErrorsLogger errorsLogger;
	
	private Session session;
	
	private Period currentPeriod;
	
	@Override
	public void onCreate() {
		super.onCreate();
		this.context = getApplicationContext();
		instance = this;
		loadInstance();
	}
	
	private void loadInstance() {
		this.session = new Session();
		this.currentPeriod = Period.HOUR;
		this.errorsLogger = new ErrorsLogger();
		// Init settings
		SettingsHelper.getInstance(context);
	}
	
	public static synchronized FooBox getInstance() { return instance; }
	
	/**
	 * Checks if a Freebox has already been discovered on this network
	 * If not :
	 * 		-> ask for app_token
	 * Then, ask for auth_token.
	 */
	public void init() {
		if (this.inited)
			return;
		
		MainActivity.displayLoadingScreen();
		
		this.premium = Premium.UNKNOWN;
		
		String savedFreebox = Util.getPrefString(this.context, "freebox");
		
		if (!savedFreebox.equals("")) {
			FooBox.log("Loading freebox from preferences");
			// Load freebox
			try {
				this.freebox = Freebox.load(savedFreebox);
			} catch (JSONException ex) {
				ex.printStackTrace();
				Crashlytics.logException(ex);
			}
			
			// Open session
			new SessionOpener(this.freebox, this.context).execute();
			// (once done, we'll update the graph)
		} else {
			// Discover Freebox
			new FreeboxDiscoverer(context).execute();
		}
	}
	
	public void saveAppToken(String appToken) {
		this.freebox.setAppToken(appToken);
		if (freebox.isAlreadySaved(context)) {
			try {
				this.freebox.save(this.context);
			} catch (JSONException ex) {
				ex.printStackTrace();
				Crashlytics.logException(ex);
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
	
	public ErrorsLogger getErrorsLogger() { return this.errorsLogger; }
	
	public boolean isPremium() {
		if (FORCE_NOTPREMIUM)
			return false;
		return this.premium == Premium.TRUE || DEBUG;
	}
	public void setIsPremium(Premium val) { this.premium = val; }
	public void setIsPremium(boolean val) {
		if (val)
			this.premium = Premium.TRUE;
		else
			this.premium = Premium.FALSE;
	}
	
	public String getAppVersion() {
		String versionName = "";
		
		try {
			PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
			versionName = pInfo.versionName;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		
		return versionName;
	}
}