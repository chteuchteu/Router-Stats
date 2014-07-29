package com.chteuchteu.freeboxstats.net;

import android.content.Context;
import android.os.AsyncTask;

import com.chteuchteu.freeboxstats.MainActivity;
import com.chteuchteu.freeboxstats.obj.Freebox;

public class SessionOpener extends AsyncTask<Void, Void, Void> {
	private boolean success;
	private Freebox freebox;
	private Context context;
	
	public SessionOpener(Freebox freebox, Context context) {
		this.freebox = freebox;
		this.success = false;
		this.context = context;
	}
	
	@Override
	protected Void doInBackground(Void... params) {
		success = NetHelper.openSession(freebox);
		
		// Init BillingService
		BillingService.getInstance(this.context);
		
		return null;
	}
	
	@Override
	protected void onPostExecute(Void res) {
		super.onPostExecute(res);
		
		if (success) {
			MainActivity.appLoadingPrereq1 = true;
			MainActivity.finishedLoading();
		} else
			MainActivity.sessionOpenFailed();
	}
}