package com.chteuchteu.freeboxstats.obj;

import java.util.ArrayList;

import com.chteuchteu.freeboxstats.ui.MainActivity;

public class ErrorsLogger {
	private ArrayList<String> errors;
	
	public ErrorsLogger() {
		this.errors = new ArrayList<String>();
	}
	
	public void logError(String error) {
		this.errors.add(error);
		MainActivity.displayDebugMenuItem();
	}
	public void logError(NetResponse netResponse) {
		if (netResponse != null)
			this.errors.add(netResponse.getError());
		else
			this.errors.add("Error 418");
		MainActivity.displayDebugMenuItem();
	}
	public boolean hasErrors() { return this.errors.size() > 0; }
	
	public ArrayList<String> getErrors() { return this.errors; }
	public String getErrorsString() {
		String out = "";
		for (String str : this.errors)
			out += str + "\r\n";
		return out;
	}
}