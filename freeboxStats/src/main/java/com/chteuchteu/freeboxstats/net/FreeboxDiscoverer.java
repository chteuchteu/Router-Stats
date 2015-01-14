package com.chteuchteu.freeboxstats.net;

import android.os.AsyncTask;

import com.chteuchteu.freeboxstats.FooBox;
import com.chteuchteu.freeboxstats.obj.Freebox;
import com.chteuchteu.freeboxstats.ui.MainActivity;

public class FreeboxDiscoverer extends AsyncTask<Void, Void, Void> {
	private Freebox freebox;
	private MainActivity activity;

	public FreeboxDiscoverer(MainActivity activity) {
		this.activity = activity;
	}
	
	@Override
	protected Void doInBackground(Void... params) {
		freebox = NetHelper.checkFreebox();
		
		// Init Billing Service
		BillingService.getInstance(activity);
		
		return null;
	}
	
	@Override
	protected void onPostExecute(Void res) {
		super.onPostExecute(res);
		
		if (freebox != null) {
			if (freebox.isApiVersionOk()) {
				FooBox.log("Found freebox", freebox.toString());
				FooBox.getInstance().setFreebox(freebox);
				
				activity.hideLoadingScreen();
				activity.displayLaunchPairingScreen();
			} else {
				// The API version is < 3.0 (actually, the needed version is 3.0.2
				// 	but we only get 3.0)
				activity.displayFreeboxUpdateNeededScreenBeforePairing();
			}
		} else {
			FooBox.log("Not found", "Error while trying to find Freebox");
			activity.displayFreeboxSearchFailedScreen();
		}
	}
}