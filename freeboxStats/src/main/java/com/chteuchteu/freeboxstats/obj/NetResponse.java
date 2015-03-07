package com.chteuchteu.freeboxstats.obj;

import com.crashlytics.android.Crashlytics;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class NetResponse {
	private boolean success;
	private Object result;
	private JSONObject completeResponse;
	
	public NetResponse(JSONObject response) {
		parseResponse(response);
	}
	
	private void parseResponse(JSONObject response) {
		try {
			this.success = response.getBoolean("success");
			this.completeResponse = response;
			if (success)
				this.result = response.get("result");
			
		} catch (JSONException ex) {
			ex.printStackTrace();
			Crashlytics.logException(ex);
		}
	}
	
	public String getError() {
		if (this.success)
			return "";
		try {
			return this.completeResponse.getString("error_code") + " => " + this.completeResponse.getString("msg");
		} catch (JSONException ex) {
			ex.printStackTrace();
			return "";
		}
	}
	
	public boolean hasSucceeded() { return this.success; }
	public JSONObject getJsonObject() { return (JSONObject) this.result; }
	public JSONArray getJsonArray() { return (JSONArray) this.result; }
	public JSONObject getCompleteResponse() { return this.completeResponse; }
}
