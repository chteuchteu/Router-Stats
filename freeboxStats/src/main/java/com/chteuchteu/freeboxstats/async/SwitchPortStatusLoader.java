package com.chteuchteu.freeboxstats.async;

import android.os.AsyncTask;

import com.chteuchteu.freeboxstats.hlpr.Enums;
import com.chteuchteu.freeboxstats.net.NetHelper;
import com.chteuchteu.freeboxstats.obj.ErrorsLogger;
import com.chteuchteu.freeboxstats.obj.Freebox;
import com.chteuchteu.freeboxstats.obj.NetResponse;
import com.chteuchteu.freeboxstats.obj.SwitchDevice;
import com.chteuchteu.freeboxstats.obj.SwitchPortStatus;
import com.chteuchteu.freeboxstats.obj.SwitchPortStatuses;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class SwitchPortStatusLoader extends AsyncTask<Void, Void, Void> {
	private Freebox freebox;
	private Runnable onPostExecute;
	private SwitchPortStatuses switchPortStatuses;

	public SwitchPortStatusLoader(Freebox freebox, Runnable onPostExecute, SwitchPortStatuses switchPortStatuses) {
		this.freebox = freebox;
		this.onPostExecute = onPostExecute;
		this.switchPortStatuses = switchPortStatuses;
	}
	
	@Override
	protected Void doInBackground(Void... params) {
		NetResponse response = NetHelper.getSwitchStatus(freebox);
		
		if (response != null && response.hasSucceeded()) {
			JSONArray results = response.getJsonArray();

			try {
				for (int i=0; i<results.length(); i++) {
					JSONObject result = (JSONObject) results.get(i);
					int port = result.getInt("id");

					SwitchPortStatus switchPortStatus = switchPortStatuses.getSwitchPortStatus(port);
					String link = result.getString("link");
					switchPortStatus.setSwitchStatusLink(link.equals("up") ? Enums.SwitchStatusLink.UP : Enums.SwitchStatusLink.DOWN);

					List<SwitchDevice> devices = new ArrayList<>();
					if (result.has("mac_list")) {
						JSONArray mac_list = result.getJSONArray("mac_list");
						for (int y = 0; y < mac_list.length(); y++) {
							JSONObject macListItem = mac_list.getJSONObject(y);
							devices.add(new SwitchDevice(macListItem.getString("mac"), macListItem.getString("hostname")));
						}
					}

					switchPortStatus.setConnectedDevices(devices);
				}

			} catch (JSONException ex) {
				ex.printStackTrace();
			}

		} else {
			ErrorsLogger.log(response);
		}
		
		return null;
	}
	
	@Override
	protected void onPostExecute(Void res) {
		super.onPostExecute(res);
		
		onPostExecute.run();
	}
}
