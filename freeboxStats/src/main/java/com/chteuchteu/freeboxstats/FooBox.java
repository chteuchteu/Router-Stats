package com.chteuchteu.freeboxstats;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.util.Log;
import android.widget.ProgressBar;

import com.androidplot.xy.XYPlot;
import com.chteuchteu.freeboxstats.hlpr.Enums;
import com.chteuchteu.freeboxstats.hlpr.Enums.Period;
import com.chteuchteu.freeboxstats.hlpr.SettingsHelper;
import com.chteuchteu.freeboxstats.hlpr.Util;
import com.chteuchteu.freeboxstats.async.FreeboxDiscoverer;
import com.chteuchteu.freeboxstats.async.SessionOpener;
import com.chteuchteu.freeboxstats.obj.ErrorsLogger;
import com.chteuchteu.freeboxstats.obj.Freebox;
import com.chteuchteu.freeboxstats.obj.Session;
import com.chteuchteu.freeboxstats.obj.ValuesBag;
import com.chteuchteu.freeboxstats.ui.MainActivity;
import com.crashlytics.android.Crashlytics;

import io.fabric.sdk.android.Fabric;
import org.json.JSONException;

import java.util.HashMap;

public class FooBox extends Application {
	public static final String APP_ID = "com.chteuchteu.freeboxstats";
	public static final String APP_NAME = "FreeboxStats";
	public static final String DEVICE_NAME = "Android";
	
	private static FooBox instance;
	private MainActivity activity;
	private Context context;
	
	private Freebox freebox;
	private ErrorsLogger errorsLogger;
	
	private Session session;
	
	private Period currentPeriod;
	
	private Enums.Graph[] graphs;
	private HashMap<Enums.Graph, XYPlot> plots;
	private HashMap<Enums.Graph, ValuesBag> valuesBags;
	private HashMap<Enums.Graph, ProgressBar> progressBars;


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
		SettingsHelper settings = SettingsHelper.getInstance(getApplicationContext());

		// Instantiate graphs array according to settings
		graphs = Enums.Graph.values();

		if (!settings.getDisplayXdslTab())
			graphs = Util.removeElement(graphs, Enums.Graph.XDSL);

		// Init ValueBags
		valuesBags = new HashMap<>();
		for (Enums.Graph graph : graphs)
			valuesBags.put(graph, new ValuesBag(graph, currentPeriod));

		plots = new HashMap<>();
		progressBars = new HashMap<>();
	}
	
	public static synchronized FooBox getInstance() { return instance; }
	
	/**
	 * Checks if a Freebox has already been discovered on this network
	 * If not :
	 * 		-> ask for app_token
	 * Then, ask for auth_token.
	 */
	public FooBox init(MainActivity mainActivity) {
		this.context = mainActivity;
		this.activity = mainActivity;
		this.errorsLogger.setActivity(activity);

		activity.displayLoadingScreen();

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
			new SessionOpener(this.freebox, this.activity).execute();
			// (once done, we'll update the graph)
		} else {
			// Discover Freebox
			new FreeboxDiscoverer(activity).execute();
		}

		return this;
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
	public void setPeriod(Period val) {
		this.currentPeriod = val;

		// Update ValuesBags
		for (ValuesBag valuesBag : valuesBags.values())
			valuesBag.setPeriod(val);
	}
	
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

	public HashMap<Enums.Graph, XYPlot> getPlots() { return plots; }
	public HashMap<Enums.Graph, ValuesBag> getValuesBags() { return valuesBags; }
	public HashMap<Enums.Graph, ProgressBar> getProgressBars() { return this.progressBars; }
	public Enums.Graph[] getGraphs() { return graphs; }
	public MainActivity getActivity() { return this.activity; }
}
