package com.chteuchteu.freeboxstats.net;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.AsyncTask;

import com.chteuchteu.freeboxstats.MainActivity;
import com.chteuchteu.freeboxstats.FooBox;
import com.chteuchteu.freeboxstats.hlpr.Enums.AuthorizeStatus;
import com.chteuchteu.freeboxstats.obj.Freebox;
import com.chteuchteu.freeboxstats.obj.NetResponse;
import com.crashlytics.android.Crashlytics;

public class AskForAppToken extends AsyncTask<Void, Void, Void> {
	private boolean ok;
	private Freebox freebox;
	private Context context;
	
	public AskForAppToken(Freebox freebox, Context context) {
		this.freebox = freebox;
		this.context = context;
	}
	
	@Override
	protected Void doInBackground(Void... params) {
		ok = false;
		
		JSONObject obj = new JSONObject();
		
		try {
			obj.put("app_id", FooBox.APP_ID);
			obj.put("app_name", FooBox.APP_NAME);
			obj.put("app_version", FooBox.APP_VERSION);
			obj.put("device_name", FooBox.DEVICE_NAME);
		} catch (JSONException ex) {
			ex.printStackTrace();
			Crashlytics.logException(ex);
		}
		
		NetResponse response = NetHelper.authorize(freebox, obj.toString());
		
		
		if (response != null && response.hasSucceeded()) {
			JSONObject res = response.getJsonObject();
			
			int trackId = -1;
			
			try {
				trackId = res.getInt("track_id");
				String appTocken = res.getString("app_token");
				
				FooBox.getInstance().setTrackId(trackId);
				FooBox.getInstance().saveAppToken(appTocken);
				
				ok = true;
			} catch (JSONException ex) {
				ex.printStackTrace();
				Crashlytics.logException(ex);
			}
			
			// Watch for token status
			boolean check = true;
			while (check) {
				// Check the status returned by the Freebox
				AuthorizeStatus astatus = NetHelper.getAuthorizeStatus(freebox, trackId);
				if (astatus == AuthorizeStatus.TIMEOUT || astatus == AuthorizeStatus.GRANTED || astatus == AuthorizeStatus.DENIED) {
					MainActivity.pairingFinished(astatus);
					check = false;
				} else {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException ex) {
						ex.printStackTrace();
						Crashlytics.logException(ex);
					}
				}
			}
			
			// Once done, open session
			boolean success = NetHelper.openSession(freebox);
			if (success) {
				// Get Freebox IP
				String freeboxIP = NetHelper.getPublicIP(freebox);
				if (freeboxIP != null && !freeboxIP.equals(""))
					freebox.setIp(freeboxIP);
				FooBox.log("Found IP " + freeboxIP);
				
				// Save Freebox
				try {
					freebox.save(context);
				} catch (JSONException ex) {
					ex.printStackTrace();
					Crashlytics.logException(ex);
				}
			}
			else
				MainActivity.sessionOpenFailed();
		} else {
			FooBox.getInstance().getErrorsLogger().logError(response);
		}
		
		return null;
	}
	
	@Override
	protected void onPostExecute(Void res) {
		super.onPostExecute(res);
		
		if (ok) {
			MainActivity.finishedLoading();
			// Load graphs
			MainActivity.refreshGraph();
		}
	}
}