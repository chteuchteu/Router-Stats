package com.chteuchteu.freeboxstats.obj;

import org.json.JSONException;
import org.json.JSONObject;

import com.chteuchteu.freeboxstats.hlpr.Util;

import android.content.Context;

public class Freebox {
	public static final String ApiUri = "http://mafreebox.freebox.fr";
	
	private String uid;
	private String deviceName;
	private String apiVersion;
	private String apiBaseUrl;
	private String deviceType;
	private String appToken;
	
	public Freebox() {
		this.uid = "";
		this.deviceName = "";
		this.apiVersion = "";
		this.apiBaseUrl = "";
		this.deviceType = "";
		this.appToken = "";
	}
	
	public Freebox(String uid, String deviceName, String apiVersion, String apiBaseUrl, String deviceType) {
		this.uid = uid;
		this.deviceName = deviceName;
		this.apiVersion = apiVersion;
		this.apiBaseUrl = apiBaseUrl;
		this.deviceType = deviceType;
		this.appToken = "";
	}
	
	@Override
	public String toString() {
		return "Freebox[uid=" + this.uid + ", deviceName=" + this.deviceName + ", apiVersion=" + this.apiVersion
				+ ", apiBaseUrl=" + this.apiBaseUrl + ", deviceType=" + this.deviceType + "]";
	}
	
	public String getApiCallUrl() {
		return Freebox.ApiUri + this.apiBaseUrl + "v1/";
	}
	
	public void save(Context c) throws JSONException {
		JSONObject obj = new JSONObject();
		obj.put("uid", this.uid);
		obj.put("deviceName", this.deviceName);
		obj.put("apiVersion", this.apiVersion);
		obj.put("apiBaseUrl", this.apiBaseUrl);
		obj.put("deviceType", this.deviceType);
		obj.put("appToken", this.appToken);
		
		Util.setPref(c, "freebox", obj.toString());
	}
	
	public boolean isAlreadySaved(Context context) {
		return !Util.getPref(context, "freebox").equals("");
	}
	
	public static Freebox load(String json) throws JSONException {
		Freebox f = new Freebox();
		
		JSONObject obj = new JSONObject(json);
		if (obj.has("uid"))
			f.setUid(obj.getString("uid"));
		if (obj.has("deviceName"))
			f.setDeviceName(obj.getString("deviceName"));
		if (obj.has("apiVersion"))
			f.setApiVersion(obj.getString("apiVersion"));
		if (obj.has("apiBaseUrl"))
			f.setApiBaseUrl(obj.getString("apiBaseUrl"));
		if (obj.has("deviceType"))
			f.setDeviceName(obj.getString("deviceType"));
		if (obj.has("appToken"))
			f.setAppToken(obj.getString("appToken"));
		
		return f;
	}
	
	public String getUid() { return this.uid; }
	public void setUid(String val) { this.uid = val; }
	
	public String getDeviceName() { return this.deviceName; }
	public void setDeviceName(String val) { this.deviceName = val; }
	
	public String getApiVersion() { return this.apiVersion; }
	public void setApiVersion(String val) { this.apiVersion = val; }
	
	//public String getApiBaseUrl() { return this.apiBaseUrl; }
	public void setApiBaseUrl(String val) { this.apiBaseUrl = val; }
	
	public String getDeviceType() { return this.deviceType; }
	public void setDeviceType(String val) { this.deviceType = val; }
	
	public String getAppToken() { return this.appToken; }
	public void setAppToken(String val) { this.appToken = val; }
}