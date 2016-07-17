package com.chteuchteu.freeboxstats.async;

import android.os.AsyncTask;

import com.chteuchteu.freeboxstats.net.NetHelper;
import com.chteuchteu.freeboxstats.obj.Freebox;
import com.chteuchteu.freeboxstats.ui.MainActivity;

public class SessionOpener extends AsyncTask<Void, Void, Void> {
	private boolean success;
	private Freebox freebox;
	private MainActivity activity;
	
	public SessionOpener(Freebox freebox, MainActivity activity) {
		this.freebox = freebox;
		this.success = false;
		this.activity = activity;
	}
	
	@Override
	protected Void doInBackground(Void... params) {
		success = NetHelper.openSession(this.freebox);
		
		return null;
	}
	
	@Override
	protected void onPostExecute(Void res) {
		super.onPostExecute(res);
		
		if (success)
			activity.finishedLoading();
		else
			activity.sessionOpenFailed();
	}
}
