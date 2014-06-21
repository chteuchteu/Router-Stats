package com.chteuchteu.freeboxstats.net;

import org.json.JSONException;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.chteuchteu.freeboxstats.MainActivity;
import com.chteuchteu.freeboxstats.SingleBox;
import com.chteuchteu.freeboxstats.obj.Freebox;

public class FreeboxDiscoverer extends AsyncTask<Void, Void, Void> {
	private Context context;
	
	public FreeboxDiscoverer(Context context) {
		this.context = context;
	}
	
	@Override
	protected Void doInBackground(Void... params) {
		Freebox freebox = NetHelper.checkFreebox();
		
		if (freebox != null) {
			Log.v("Found freebox", freebox.toString());
			SingleBox.getInstance().setFreebox(freebox);
			try {
				freebox.save(context);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			MainActivity.displayLaunchPairingButton();
		}
		else
			Log.v("Not found", "Error while trying to find Freebox");
		
		return null;
	}
	
	@Override
	protected void onPostExecute(Void res) {
		super.onPostExecute(res);
	}
}