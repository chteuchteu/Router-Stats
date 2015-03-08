package com.chteuchteu.freeboxstats.obj;

import com.chteuchteu.freeboxstats.hlpr.Enums;

import java.util.ArrayList;
import java.util.List;

public class SwitchPortStatus {
	private int switchIndex;
	private Enums.SwitchStatusLink switchStatusLink;
	private List<SwitchDevice> connectedDevices;

	public SwitchPortStatus(int switchIndex) {
		this.switchIndex = switchIndex;
		this.switchStatusLink = Enums.SwitchStatusLink.UNKNOWN;
		this.connectedDevices = new ArrayList<>();
	}

	public int getSwitchIndex() { return this.switchIndex; }

	public Enums.SwitchStatusLink getSwitchStatusLink() { return this.switchStatusLink; }
	public void setSwitchStatusLink(Enums.SwitchStatusLink val) { this.switchStatusLink = val; }

	public List<SwitchDevice> getConnectedDevices() { return this.connectedDevices; }
	public void setConnectedDevices(List<SwitchDevice> devices) {
		this.connectedDevices.clear();
		this.connectedDevices.addAll(devices);
	}

}
