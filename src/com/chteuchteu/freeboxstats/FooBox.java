package com.chteuchteu.freeboxstats;

import org.json.JSONException;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.util.Log;
import android.view.View;

import com.androidplot.xy.XYPlot;
import com.chteuchteu.freeboxstats.hlpr.Enums.Period;
import com.chteuchteu.freeboxstats.hlpr.SettingsHelper;
import com.chteuchteu.freeboxstats.hlpr.Util;
import com.chteuchteu.freeboxstats.net.BillingService;
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
	private Premium premium = Premium.TRUE;
	public static final boolean DEBUG = false;
	public static final boolean DEBUG_INAPPPURCHASE = false;
	public static final boolean FORCE_NOTPREMIUM = false;
	
	private static FooBox instance;
	private Context context;
	
	private Freebox freebox;
	private String authToken;
	private int trackId;
	private ErrorsLogger errorsLogger;
	
	private Session session;
	
	private Period currentPeriod;
	
	// MainActivity context
	public XYPlot plot1;
	public XYPlot plot2;
	public XYPlot plot3;
	public XYPlot plot4;
	public View fragment1RootView;
	public View fragment2RootView;
	public View fragment3RootView;
	public View fragment4RootView;
	
	@Override
	public void onCreate() {
		super.onCreate();
		instance = this;
		loadInstance();
	}
	
	private void loadInstance() {
		this.session = new Session();
		this.currentPeriod = Period.HOUR;
		this.errorsLogger = new ErrorsLogger();
		// Init settings
		SettingsHelper.getInstance(getApplicationContext());
	}
	
	public static synchronized FooBox getInstance() { return instance; }
	
	/**
	 * Checks if a Freebox has already been discovered on this network
	 * If not :
	 * 		-> ask for app_token
	 * Then, ask for auth_token.
	 */
	public void init(Context context) {
		if (this.inited)
			return;
		
		this.context = context;
		
		MainActivity.displayLoadingScreen();
		
		this.premium = Premium.UNKNOWN;
		if (Util.hasPref(context, "premium") && Util.getPrefBoolean(context, "premium", false))
			this.premium = Premium.TRUE;
		
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
			
			if (this.premium == Premium.UNKNOWN && !FooBox.DEBUG) {
				// Init Billing Service
				BillingService.getInstance(this.context);
				// Once done :
				//  * open session
				//  * update the graph
			} else if (this.premium == Premium.TRUE || FooBox.DEBUG) {
				// Open session
				new SessionOpener(this.freebox, this.context).execute();
				// (once done, we'll update the graph)
			}
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
	
	public void setPlot(XYPlot plot, int plotIndex) {
		switch (plotIndex) {
			case 1:
				plot1 = plot;
				break;
			case 2:
				plot2 = plot;
				break;
			case 3:
				plot3 = plot;
				break;
			case 4:
				plot4 = plot;
				break;
		}
	}
	
	public XYPlot getPlot(int plotIndex) {
		switch (plotIndex) {
			case 1: return plot1;
			case 2: return plot2;
			case 3: return plot3;
			case 4: return plot4;
			default: return null;
		}
	}
	
	public void setFragmentRootView(View view, int plotIndex) {
		switch (plotIndex) {
			case 1:
				fragment1RootView = view;
				break;
			case 2:
				fragment2RootView = view;
				break;
			case 3:
				fragment3RootView = view;
				break;
			case 4:
				fragment4RootView = view;
				break;
		}
	}
	
	public View getFragmentRootView(int plotIndex) {
		switch (plotIndex) {
			case 1: return fragment1RootView;
			case 2: return fragment2RootView;
			case 3: return fragment3RootView;
			case 4: return fragment4RootView;
			default: return null;
		}
	}
}