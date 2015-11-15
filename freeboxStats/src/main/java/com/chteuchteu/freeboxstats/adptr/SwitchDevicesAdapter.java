package com.chteuchteu.freeboxstats.adptr;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.chteuchteu.freeboxstats.R;
import com.chteuchteu.freeboxstats.obj.SwitchDevice;

import java.util.List;

public class SwitchDevicesAdapter extends ArrayAdapter<SwitchDevice> {
	private Context context;

	public SwitchDevicesAdapter(Context context, List<SwitchDevice> items) {
		super(context, R.layout.switch_deviceslist, items);
		this.context = context;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
		
		if (v == null) {
			LayoutInflater vi = LayoutInflater.from(context);
			v = vi.inflate(R.layout.switch_deviceslist, parent, false);
		}
		
		SwitchDevice device = getItem(position);

		if (device != null) {
			TextView line1 = (TextView) v.findViewById(R.id.line_a);
			TextView line2 = (TextView) v.findViewById(R.id.line_b);

			line1.setText(device.getHostName());
			line2.setText(device.getMac());
		}

		return v;
	}
}
