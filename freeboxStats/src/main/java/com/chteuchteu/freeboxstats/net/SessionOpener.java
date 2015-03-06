package com.chteuchteu.freeboxstats.net;

import android.os.AsyncTask;

import com.chteuchteu.freeboxstats.obj.Freebox;
import com.chteuchteu.freeboxstats.ui.IMainActivity;
import com.chteuchteu.freeboxstats.ui.MainActivity;

public class SessionOpener extends AsyncTask<Void, Void, Void> {
	private boolean success;
	private Freebox freebox;
	private IMainActivity iActivity;
	
	public SessionOpener(Freebox freebox, IMainActivity activity) {
		this.freebox = freebox;
		this.success = false;
		this.iActivity = activity;
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
            iActivity.finishedLoading();
		else
            iActivity.sessionOpenFailed();
	}
}
