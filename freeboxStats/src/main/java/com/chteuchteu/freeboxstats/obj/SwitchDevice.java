package com.chteuchteu.freeboxstats.obj;

public class SwitchDevice {
	private String mac;
	private String hostname;

	public SwitchDevice(String mac, String hostname) {
		this.mac = mac;
		this.hostname = hostname;
	}

	public String getMac() { return this.mac; }
	public String getHostName() { return this.hostname; }

	@Override
	public String toString() {
		return this.hostname + " - " + this.mac;
	}
}
