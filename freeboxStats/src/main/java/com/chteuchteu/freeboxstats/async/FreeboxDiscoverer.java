package com.chteuchteu.freeboxstats.async;

import android.os.AsyncTask;

import com.chteuchteu.freeboxstats.FooBox;
import com.chteuchteu.freeboxstats.net.NetHelper;
import com.chteuchteu.freeboxstats.obj.Freebox;
import com.chteuchteu.freeboxstats.ui.IMainActivity;

public class FreeboxDiscoverer extends AsyncTask<Void, Void, Void> {
	private Freebox freebox;
	private IMainActivity iActivity;

	public FreeboxDiscoverer(IMainActivity activity) {
		this.iActivity = activity;
	}
	
	@Override
	protected Void doInBackground(Void... params) {
		freebox = NetHelper.checkFreebox();
		
		return null;
	}
	
	@Override
	protected void onPostExecute(Void res) {
		super.onPostExecute(res);
		
		if (freebox != null) {
			if (freebox.isApiVersionOk()) {
				FooBox.log("Found freebox", freebox.toString());
				FooBox.getInstance().setFreebox(freebox);
				
				iActivity.hideLoadingScreen();
				iActivity.displayLaunchPairingScreen();
			} else {
				// The API version is < 3.0 (actually, the needed version is 3.0.2
				// 	but we only get 3.0)
                iActivity.displayFreeboxUpdateNeededScreenBeforePairing();
			}
		} else {
			FooBox.log("Not found", "Error while trying to find Freebox");
            iActivity.displayFreeboxSearchFailedScreen();
		}
	}
}
