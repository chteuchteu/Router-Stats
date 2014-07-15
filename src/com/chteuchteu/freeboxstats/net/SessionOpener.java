package com.chteuchteu.freeboxstats.net;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import com.chteuchteu.freeboxstats.MainActivity;
import com.chteuchteu.freeboxstats.hlpr.SettingsHelper;
import com.chteuchteu.freeboxstats.obj.Freebox;

public class SessionOpener extends AsyncTask<Void, Void, Void> {
	private boolean success;
	private Freebox freebox;
	private Context context;
	
	public SessionOpener(Freebox freebox, Context context) {
		this.freebox = freebox;
		this.context = context;
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
		} else
			Toast.makeText(context, "Impossible de se connecter à la Freebox...", Toast.LENGTH_SHORT).show();
		
		MainActivity.appLoadingStep++;
		if (MainActivity.appLoadingStep == 2)
			MainActivity.hideLoadingScreen();
	}
}