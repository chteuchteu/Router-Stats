package com.chteuchteu.freeboxstats.obj;

import com.chteuchteu.freeboxstats.ui.MainActivity;

import java.util.ArrayList;

public class ErrorsLogger {
	private ArrayList<String> errors;
	private MainActivity activity;
	
	public ErrorsLogger() {
		this.errors = new ArrayList<>();
	}
	
	public void logError(String error) {
		this.errors.add(error);
		if (this.activity != null)
			this.activity.displayDebugMenuItem();
	}
	public void logError(NetResponse netResponse) {
		if (netResponse != null)
			this.errors.add(netResponse.getError());
		else
			this.errors.add("Error 418");

		if (this.activity != null)
			this.activity.displayDebugMenuItem();
	}
	
	public ArrayList<String> getErrors() { return this.errors; }
	public String getErrorsString() {
		String out = "";
		for (String str : this.errors)
			out += str + "\r\n";
		return out;
	}

	public void setActivity(MainActivity activity) { this.activity = activity; }
}
