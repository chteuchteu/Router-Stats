package com.chteuchteu.freeboxstats;

import org.json.JSONException;

import android.content.Context;

import com.chteuchteu.freeboxstats.hlpr.Util;
import com.chteuchteu.freeboxstats.net.FreeboxDiscoverer;
import com.chteuchteu.freeboxstats.obj.Freebox;
import com.chteuchteu.freeboxstats.obj.Session;

public class SingleBox {
	public static final String APP_ID = "com.chteuchteu.freeboxstats";
	public static final String APP_NAME = "FreeboxStats";
	public static final String APP_VERSION = "1.0";
	public static final String DEVICE_NAME = "Android";
	
	private static SingleBox instance;
	private Context context;
	
	private Freebox freebox;
	private String authToken;
	private int trackId;
	
	private Session session;
	
	private SingleBox(Context context) {
		loadInstance(context);
	}
	
	private void loadInstance(Context context) {
		if (context != null)
			this.context = context;
		this.session = new Session();
	}
	
	public static synchronized SingleBox getInstance(Context context) {
		if (instance == null)
			instance = new SingleBox(context);
		return instance;
	}
	
	public static SingleBox getInstance() {
		return getInstance(null);
	}
	
	/**
	 * Checks if a Freebox has already been discovered on this network
	 * If not :
	 * 		-> ask for app_token
	 * Then, ask for auth_token.
	 */
	public void init() {
		String savedFreebox = Util.getPref(this.context, "freebox");
		if (!savedFreebox.equals("")) {
			// Load freebox
			try {
				this.freebox = Freebox.load(savedFreebox);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			
			// Get Auth token
			
		} else {
			// Discover Freebox
			new FreeboxDiscoverer().execute();
		}
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
	
	public Freebox getFreebox() { return this.freebox; }
	public void setFreebox(Freebox val) { this.freebox = val; }
	
	public String getAuthToken() { return this.authToken; }
	public void setAuthToken(String val) { this.authToken = val; }
	
	public int getTrackId() { return this.trackId; }
	public void setTrackId(int val) { this.trackId = val; }
	
	public Session getSession() { return this.session; }
	
	public Context getContext() { return this.context; }
}