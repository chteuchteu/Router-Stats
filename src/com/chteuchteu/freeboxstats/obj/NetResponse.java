package com.chteuchteu.freeboxstats.obj;

import org.json.JSONException;
import org.json.JSONObject;

public class NetResponse {
	private boolean success;
	private JSONObject result;
	
	public NetResponse(JSONObject response) {
		parseResponse(response);
	}
	
	private void parseResponse(JSONObject response) {
		if (response.equals(""))
			return;
		
		try {
			this.success = response.getBoolean("success");
			
			if (this.success)
				this.result = response.getJSONObject("result");
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	public boolean hasSucceeded() { return this.success; }
	public JSONObject getJsonObject() { return this.result; }
}