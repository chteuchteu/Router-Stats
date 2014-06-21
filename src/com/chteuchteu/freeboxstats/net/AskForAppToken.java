package com.chteuchteu.freeboxstats.net;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.AsyncTask;

import com.chteuchteu.freeboxstats.MainActivity;
import com.chteuchteu.freeboxstats.SingleBox;
import com.chteuchteu.freeboxstats.hlpr.Enums.AuthorizeStatus;
import com.chteuchteu.freeboxstats.obj.Freebox;
import com.chteuchteu.freeboxstats.obj.NetResponse;

public class AskForAppToken extends AsyncTask<Void, Void, Void> {
	private boolean ok;
	private Freebox freebox;
	
	public AskForAppToken(Freebox freebox) {
		this.freebox = freebox;
	}
	
	@Override
	protected Void doInBackground(Void... params) {
		ok = false;
		
		JSONObject obj = new JSONObject();
		
		try {
			obj.put("app_id", SingleBox.APP_ID);
			obj.put("app_name", SingleBox.APP_NAME);
			obj.put("app_version", SingleBox.APP_VERSION);
			obj.put("device_name", SingleBox.DEVICE_NAME);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		NetResponse response = NetHelper.authorize(freebox, obj.toString());
		
		if (response != null) {
			if (response.hasSucceeded()) {
				JSONObject res = response.getJsonObject();
				
				int trackId = -1;
				
				try {
					trackId = res.getInt("track_id");
					String appTocken = res.getString("app_token");
					
					SingleBox.getInstance().setTrackId(trackId);
					SingleBox.getInstance().saveAppToken(appTocken);
					
					ok = true;
				} catch (JSONException e) {
					e.printStackTrace();
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
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
				
				// Once done, open session
				boolean success = NetHelper.openSession(freebox);
				if (success)
					MainActivity.hideLaunchPairingButton();
				else
					MainActivity.sessionOpenFailed();
			}
		}
		
		return null;
	}
	
	@Override
	protected void onPostExecute(Void res) {
		super.onPostExecute(res);
		
		if (ok) {
			// Save Freebox and all
			try {
				SingleBox.getInstance().getFreebox().save(SingleBox.getInstance().getContext());
			} catch (JSONException e) {
				e.printStackTrace();
			}
			
			// Ask for auth token
			
		}
	}
}