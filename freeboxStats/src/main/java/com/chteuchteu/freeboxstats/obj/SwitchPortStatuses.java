package com.chteuchteu.freeboxstats.obj;

import java.util.ArrayList;

public class SwitchPortStatuses extends ArrayList<SwitchPortStatus> {
	private boolean fetched;

	public static SwitchPortStatuses instantiate() {
		SwitchPortStatuses list = new SwitchPortStatuses();
		list.add(new SwitchPortStatus(1));
		list.add(new SwitchPortStatus(2));
		list.add(new SwitchPortStatus(3));
		list.add(new SwitchPortStatus(4));
		return list;
	}

	public SwitchPortStatus getSwitchPortStatus(int switchIndex) {
		for (SwitchPortStatus item : this) {
			if (item.getSwitchIndex() == switchIndex)
				return item;
		}
		return null;
	}

	public boolean isFetched() { return this.fetched; }
	public void setFetched() { this.fetched = true; }
}
