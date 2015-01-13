package com.chteuchteu.freeboxstats;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.util.Log;

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
import com.chteuchteu.freeboxstats.ui.MainActivity;
import com.crashlytics.android.Crashlytics;

import org.json.JSONException;

public class FooBox extends Application {
	public static final String APP_ID = "com.chteuchteu.freeboxstats";
	public static final String APP_NAME = "FreeboxStats";
	public static final String DEVICE_NAME = "Android";
	
	public enum Premium { TRUE, FALSE, UNKNOWN }
	private Premium premium = Premium.TRUE;
	public static final boolean DEBUG_INAPPPURCHASE = false;
	private static final boolean FORCE_NOTPREMIUM = false;
	private static final boolean LOG = true;
	
	private static FooBox instance;
	private Context context;
	
	private Freebox freebox;
	private ErrorsLogger errorsLogger;
	
	private Session session;
	
	private Period currentPeriod;
	
	// MainActivity context
	private XYPlot plot1;
	private XYPlot plot2;
	private XYPlot plot3;
	private XYPlot plot4;
	private XYPlot stack_plot;

    private XYPlot switch_plot_1;
    private XYPlot switch_plot_2;
    private XYPlot switch_plot_3;
    private XYPlot switch_plot_4;

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
			
			if (this.premium == Premium.UNKNOWN && !BuildConfig.DEBUG) {
				// Init Billing Service
				BillingService.getInstance(this.context);
				// Once done :
				//  * open session
				//  * update the graph
			} else if (this.premium == Premium.TRUE || BuildConfig.DEBUG) {
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
	public static void log(String key, String msg) { if (BuildConfig.DEBUG && LOG) Log.i(key, msg); }
	
	public Freebox getFreebox() { return this.freebox; }
	public void setFreebox(Freebox val) { this.freebox = val; }
	
	public Session getSession() { return this.session; }
	
	public Context getContext() { return this.context; }
	
	public Period getPeriod() { return this.currentPeriod; }
	public void setPeriod(Period val) { this.currentPeriod = val; }
	
	public ErrorsLogger getErrorsLogger() { return this.errorsLogger; }
	
	public boolean isPremium() {
		if (FORCE_NOTPREMIUM)
			return false;
		return this.premium == Premium.TRUE || BuildConfig.DEBUG;
	}
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
			case 1: plot1 = plot; break;
			case 2: plot2 = plot; break;
			case 3: plot3 = plot; break;
			case 4: plot4 = plot; break;
			case 5: stack_plot = plot; break;
            case 6: switch_plot_1 = plot; break;
            case 7: switch_plot_2 = plot; break;
            case 8: switch_plot_3 = plot; break;
            case 9: switch_plot_4 = plot; break;
		}
	}
	
	public XYPlot getPlot(int plotIndex) {
		switch (plotIndex) {
			case 1: return plot1;
			case 2: return plot2;
			case 3: return plot3;
			case 4: return plot4;
			case 5: return stack_plot;
            case 6: return switch_plot_1;
            case 7: return switch_plot_2;
            case 8: return switch_plot_3;
            case 9: return switch_plot_4;
			default: return null;
		}
	}
}