package com.chteuchteu.freeboxstats.obj;

public class Session {
	private String challenge;
	private String sessionToken;
	
	public Session() {
		this.challenge = "";
		this.sessionToken = "";
	}
	
	public void setChallenge(String val) { this.challenge = val; }
	
	public void setSessionToken(String val) { this.sessionToken = val; }
	public String getSessionToken() { return this.sessionToken; }
}
