package com.chteuchteu.freeboxstats.net;

import android.content.Context;
import android.os.AsyncTask;

import com.chteuchteu.freeboxstats.FooBox;
import com.chteuchteu.freeboxstats.MainActivity;
import com.chteuchteu.freeboxstats.obj.Freebox;

public class FreeboxDiscoverer extends AsyncTask<Void, Void, Void> {
	private Freebox freebox;
	private Context context;
	
	
	public FreeboxDiscoverer(Context context) {
		this.context = context;
	}
	
	@Override
	protected Void doInBackground(Void... params) {
		freebox = NetHelper.checkFreebox();
		
		// Init Billing Service
		BillingService.getInstance(context);
		
		return null;
	}
	
	@Override
	protected void onPostExecute(Void res) {
		super.onPostExecute(res);
		
		if (freebox != null) {
			if (freebox.isApiVersionOk()) {
				FooBox.log("Found freebox", freebox.toString());
				FooBox.getInstance().setFreebox(freebox);
				
				MainActivity.hideLoadingScreen();
				MainActivity.displayLaunchPairingScreen();
			} else {
				// The API version is < 3.0 (actually, the needed version is 3.0.2
				// 	but we only get 3.0)
				MainActivity.displayFreeboxUpdateNeededScreenBeforePairing();
			}
		} else {
			FooBox.log("Not found", "Error while trying to find Freebox");
			MainActivity.displayFreeboxSearchFailedScreen();
		}
	}
}