package com.chteuchteu.freeboxstats.net;

import java.util.ArrayList;

import android.os.AsyncTask;
import android.util.Log;

import com.chteuchteu.freeboxstats.hlpr.Enums.Field;
import com.chteuchteu.freeboxstats.hlpr.Enums.Period;
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
		
		ArrayList<Field> fields = new ArrayList<Field>();
		fields.add(Field.RATE_DOWN);
		fields.add(Field.BW_DOWN);
		
		if (success)
			new GraphLoader(freebox, Period.HOUR, fields).execute();
		else
			Log.v("", "Failed to open session");
	}
}