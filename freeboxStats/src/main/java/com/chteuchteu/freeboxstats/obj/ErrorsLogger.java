package com.chteuchteu.freeboxstats.obj;

import com.chteuchteu.freeboxstats.FooBox;
import com.chteuchteu.freeboxstats.ui.MainActivity;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class ErrorsLogger {
	private ArrayList<AppError> errors;
	private MainActivity activity;
	
	public ErrorsLogger() {
		this.errors = new ArrayList<>();
	}
	
	public void logError(String error) {
		this.errors.add(new AppError(error));
		if (this.activity != null)
			this.activity.displayDebugMenuItem();
	}
	public void logError(NetResponse netResponse) {
        this.errors.add(new AppError(netResponse == null ? "Void netResponse" : netResponse.getError()));

		if (this.activity != null)
			this.activity.displayDebugMenuItem();
	}
	
	public ArrayList<AppError> getErrors() { return this.errors; }
	public String getErrorsString() {
		String out = "";
		for (AppError err : this.errors)
			out += " - " + err + "\r\n";
		return out;
	}

	public void setActivity(MainActivity activity) { this.activity = activity; }

    public static void log(String msg) { FooBox.getInstance().getErrorsLogger().logError(msg); }
    public static void log(NetResponse nResponse) { FooBox.getInstance().getErrorsLogger().logError(nResponse); }

    public class AppError {
        private String error;
        private Date date;
        public AppError(String error) {
            this.error = error;
            this.date = new Date();
        }
        @Override
        public String toString() {
            DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss", Locale.FRENCH);
            return "[" + dateFormat.format(this.date) + "]" + this.error;
        }
    }
}
