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
import com.chteuchteu.freeboxstats.async.FreeboxDiscoverer;
import com.chteuchteu.freeboxstats.async.SessionOpener;
import com.chteuchteu.freeboxstats.obj.ErrorsLogger;
import com.chteuchteu.freeboxstats.obj.Freebox;
import com.chteuchteu.freeboxstats.obj.Session;
import com.chteuchteu.freeboxstats.ui.IMainActivity;
import com.chteuchteu.freeboxstats.ui.MainActivity;
import com.crashlytics.android.Crashlytics;

import io.fabric.sdk.android.Fabric;
import org.json.JSONException;

public class FooBox extends Application {
	public static final String APP_ID = "com.chteuchteu.freeboxstats";
	public static final String APP_NAME = "FreeboxStats";
	public static final String DEVICE_NAME = "Android";
	
	private static FooBox instance;
	private IMainActivity iActivity;
	private Context context;
	
	private Freebox freebox;
	private ErrorsLogger errorsLogger;
	
	private Session session;
	
	private Period currentPeriod;
	
	// MainActivity context
	public enum PlotType { RATEDOWN, RATEUP, TEMP, XDSL, SW1, SW2, SW3, SW4 }
	private XYPlot plot_rateDown;
	private XYPlot plot_rateUp;
	private XYPlot plot_temp;
	private XYPlot plot_xdsl;
    private XYPlot plot_switch_1;
    private XYPlot plot_switch_2;
    private XYPlot plot_switch_3;
    private XYPlot plot_switch_4;

	@Override
	public void onCreate() {
		super.onCreate();
		Fabric.with(this, new Crashlytics());
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
	public void init(MainActivity mainActivity) {
		this.context = mainActivity;
		this.iActivity = mainActivity;
		this.errorsLogger.setActivity(iActivity);

        iActivity.displayLoadingScreen();

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
			new SessionOpener(this.freebox, this.iActivity).execute();
			// (once done, we'll update the graph)
		} else {
			// Discover Freebox
			new FreeboxDiscoverer(iActivity).execute();
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
	public static void log(String key, String msg) { if (BuildConfig.DEBUG) Log.i(key, msg); }
	
	public Freebox getFreebox() { return this.freebox; }
	public void setFreebox(Freebox val) { this.freebox = val; }
	
	public Session getSession() { return this.session; }
	
	public Context getContext() { return this.context; }
	
	public Period getPeriod() { return this.currentPeriod; }
	public void setPeriod(Period val) { this.currentPeriod = val; }
	
	public ErrorsLogger getErrorsLogger() { return this.errorsLogger; }
	
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
	
	public void setPlot(XYPlot plot, PlotType plotType) {
		switch (plotType) {
			case RATEDOWN: plot_rateDown = plot; break;
			case RATEUP: plot_rateUp = plot; break;
			case TEMP: plot_temp = plot; break;
			case XDSL: plot_xdsl = plot; break;
			case SW1: plot_switch_1 = plot; break;
			case SW2: plot_switch_2 = plot; break;
			case SW3: plot_switch_3 = plot; break;
			case SW4: plot_switch_4 = plot; break;
		}
	}
	
	public XYPlot getPlot(PlotType plotType) {
		switch (plotType) {
			case RATEDOWN: return plot_rateDown;
			case RATEUP: return plot_rateUp;
			case TEMP: return plot_temp;
			case XDSL: return plot_xdsl;
			case SW1: return plot_switch_1;
			case SW2: return plot_switch_2;
			case SW3: return plot_switch_3;
			case SW4: return plot_switch_4;
			default: return null;
		}
	}

	public IMainActivity getActivity() { return this.iActivity; }
}
