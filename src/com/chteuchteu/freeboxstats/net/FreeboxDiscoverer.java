package com.chteuchteu.freeboxstats.net;

import android.os.AsyncTask;

import com.chteuchteu.freeboxstats.MainActivity;
import com.chteuchteu.freeboxstats.FooBox;
import com.chteuchteu.freeboxstats.obj.Freebox;

public class FreeboxDiscoverer extends AsyncTask<Void, Void, Void> {
	public FreeboxDiscoverer() { }
	
	@Override
	protected Void doInBackground(Void... params) {
		Freebox freebox = NetHelper.checkFreebox();
		
		if (freebox != null) {
			FooBox.log("Found freebox", freebox.toString());
			FooBox.getInstance().setFreebox(freebox);
			
			MainActivity.displayLaunchPairingScreen();
		}
		else
			FooBox.log("Not found", "Error while trying to find Freebox");
		
		return null;
	}
	
	@Override
	protected void onPostExecute(Void res) {
		super.onPostExecute(res);
	}
}