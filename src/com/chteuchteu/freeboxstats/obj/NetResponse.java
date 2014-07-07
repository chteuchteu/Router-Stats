package com.chteuchteu.freeboxstats.obj;

import org.json.JSONException;
import org.json.JSONObject;

public class NetResponse {
	private boolean success;
	private JSONObject result;
	private JSONObject completeResponse;
	
	public NetResponse(JSONObject response) {
		parseResponse(response);
	}
	
	private void parseResponse(JSONObject response) {
		try {
			this.success = response.getBoolean("success");
			this.completeResponse = response;
			if (success)
				this.result = response.getJSONObject("result");
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	public boolean hasSucceeded() { return this.success; }
	public JSONObject getJsonObject() { return this.result; }
	public JSONObject getCompleteResponse() { return this.completeResponse; }
}