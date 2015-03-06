package com.chteuchteu.freeboxstats.obj;

import android.content.Context;

import com.chteuchteu.freeboxstats.hlpr.Enums;
import com.chteuchteu.freeboxstats.hlpr.Util;

import org.json.JSONException;
import org.json.JSONObject;

public class Freebox {
	public static final String ApiUri = "http://mafreebox.freebox.fr";
	
	private String uid;
	private String deviceName;
	private String apiVersion;
	private String apiBaseUrl;
	private String deviceType;
	private String appToken;
	private String ip;
	private Enums.SpecialBool apiRemoteAccess;
	
	public Freebox() {
		this.uid = "";
		this.deviceName = "";
		this.apiVersion = "";
		this.apiBaseUrl = "";
		this.deviceType = "";
		this.appToken = "";
		this.ip = "";
	}
	
	public Freebox(String uid, String deviceName, String apiVersion, String apiBaseUrl, String deviceType) {
		this.uid = uid;
		this.deviceName = deviceName;
		this.apiVersion = apiVersion;
		this.apiBaseUrl = apiBaseUrl;
		this.deviceType = deviceType;
		this.appToken = "";
		this.ip = "";
		this.apiRemoteAccess = Enums.SpecialBool.UNKNOWN;
	}
	
	@Override
	public String toString() {
		return "Freebox[uid=" + this.uid + ", deviceName=" + this.deviceName + ", apiVersion=" + this.apiVersion
				+ ", apiBaseUrl=" + this.apiBaseUrl + ", deviceType=" + this.deviceType + ", ip=" + this.ip
				+ ", apiRemoteAccess=" + this.apiRemoteAccess.name() + ", apiCallurl=" + getApiCallUrl() + "]";
	}
	
	public boolean isApiVersionOk() {
		// Sometimes, the apiVersion is "null", which can make the versionCompare fail
		if (this.apiVersion == null || this.apiVersion.equals("null"))
			return false;
		
		int val = Util.versionCompare(this.apiVersion, "3.0");
		// val == 0 : OK
		// val  < 0 => str1<str2
		// val  > 0 => str1>str2
		return val >= 0;
	}
	
	public String getApiCallUrl() {
		if (this.apiRemoteAccess != Enums.SpecialBool.FALSE && !this.ip.equals(""))
			return "http://" + this.ip + this.apiBaseUrl + "v3/";
		else
			return Freebox.ApiUri + this.apiBaseUrl + "v3/";
	}
	
	public String getDisplayUrl() {
		if (this.apiRemoteAccess == Enums.SpecialBool.FALSE || this.ip.equals(""))
			return Freebox.ApiUri.substring("http://".length());
		else {
			if (this.ip.contains(":"))
				return this.ip.substring(0, this.ip.indexOf(':'));
			return this.ip;
		}
	}
	
	public void save(Context c) throws JSONException {
		JSONObject obj = new JSONObject();
		obj.put("uid", this.uid);
		obj.put("deviceName", this.deviceName);
		obj.put("apiVersion", this.apiVersion);
		obj.put("apiBaseUrl", this.apiBaseUrl);
		obj.put("deviceType", this.deviceType);
		obj.put("appToken", this.appToken);
		obj.put("publicIp", this.ip);
		obj.put("apiRemoteAccess", this.apiRemoteAccess.getSerializedValue());
		
		Util.setPref(c, "freebox", obj.toString());
	}
	
	public static void delete(Context c) {
		Util.removePref(c, "freebox");
	}
	
	public boolean isAlreadySaved(Context context) {
		return !Util.getPrefString(context, "freebox").equals("");
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
		if (obj.has("publicIp"))
			f.setIp(obj.getString("publicIp"));
		if (obj.has("apiRemoteAccess"))
			f.setApiRemoteAccess(Enums.SpecialBool.get(obj.getString("apiRemoteAccess")));
		
		return f;
	}

	public void setUid(String val) { this.uid = val; }
	public void setDeviceName(String val) { this.deviceName = val; }
	public void setApiVersion(String val) { this.apiVersion = val; }
	public void setApiBaseUrl(String val) { this.apiBaseUrl = val; }
	public String getAppToken() { return this.appToken; }
	public void setAppToken(String val) { this.appToken = val; }
	public String getIp() { return this.ip; }
	public void setIp(String val) { this.ip = val; }
	public Enums.SpecialBool getApiRemoteAccess() { return this.apiRemoteAccess; }
	public void setApiRemoteAccess(Enums.SpecialBool val) { this.apiRemoteAccess = val; }

    public static String staticToString(Freebox freebox) {
        return freebox == null ? "(null)" : freebox.toString();
    }
}
