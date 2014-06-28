package com.chteuchteu.freeboxstats.net;

import android.os.AsyncTask;
import android.util.Log;

import com.chteuchteu.freeboxstats.MainActivity;
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
		
		if (success)
			MainActivity.updateGraph();
		else
			Log.v("", "Failed to open session");
	}
}