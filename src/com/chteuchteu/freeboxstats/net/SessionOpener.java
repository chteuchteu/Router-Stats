package com.chteuchteu.freeboxstats.net;

import android.os.AsyncTask;

import com.chteuchteu.freeboxstats.MainActivity;
import com.chteuchteu.freeboxstats.hlpr.SettingsHelper;
import com.chteuchteu.freeboxstats.obj.Freebox;

public class SessionOpener extends AsyncTask<Void, Void, Void> {
	private boolean success;
	private Freebox freebox;
	
	public SessionOpener(Freebox freebox) {
		this.freebox = freebox;
	}
	
	@Override
	protected Void doInBackground(Void... params) {
		success = NetHelper.openSession(freebox);
		
		return null;
	}
	
	@Override
	protected void onPostExecute(Void res) {
		super.onPostExecute(res);
		
		if (success) {
			MainActivity.displayGraphs();
			MainActivity.refreshGraph();
			if (SettingsHelper.getInstance().getAutoRefresh())
				MainActivity.startRefreshThread();
			MainActivity.appLoadingStep++;
			if (MainActivity.appLoadingStep == 2)
				MainActivity.hideLoadingScreen();
		} else {
			MainActivity.appLoadingStep = -2;
			MainActivity.sessionOpenFailed();
		}
	}
}