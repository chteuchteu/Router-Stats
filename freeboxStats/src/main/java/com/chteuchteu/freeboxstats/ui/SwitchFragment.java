package com.chteuchteu.freeboxstats.ui;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.PopupMenu;

import com.androidplot.xy.XYPlot;
import com.chteuchteu.freeboxstats.FooBox;
import com.chteuchteu.freeboxstats.R;
import com.chteuchteu.freeboxstats.adptr.SwitchDevicesAdapter;
import com.chteuchteu.freeboxstats.async.SwitchPortStatusLoader;
import com.chteuchteu.freeboxstats.obj.SwitchDevice;
import com.chteuchteu.freeboxstats.obj.SwitchPortStatus;
import com.chteuchteu.freeboxstats.obj.SwitchPortStatuses;

import java.util.List;

public class SwitchFragment extends Fragment {
	private Context context;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.context = FooBox.getInstance().getContext();
	    View rootView = inflater.inflate(R.layout.fragment_switch, container, false);

        XYPlot switch_plot1 = (XYPlot) rootView.findViewById(R.id.xyPlot_switch1);
        XYPlot switch_plot2 = (XYPlot) rootView.findViewById(R.id.xyPlot_switch2);
        XYPlot switch_plot3 = (XYPlot) rootView.findViewById(R.id.xyPlot_switch3);
        XYPlot switch_plot4 = (XYPlot) rootView.findViewById(R.id.xyPlot_switch4);

	    FooBox fooBox = FooBox.getInstance();

        fooBox.setPlot(switch_plot1, FooBox.PlotType.SW1);
        fooBox.setPlot(switch_plot2, FooBox.PlotType.SW2);
        fooBox.setPlot(switch_plot3, FooBox.PlotType.SW3);
        fooBox.setPlot(switch_plot4, FooBox.PlotType.SW4);

        fooBox.getActivity().initPlot(switch_plot1, FooBox.PlotType.SW1);
        fooBox.getActivity().initPlot(switch_plot2, FooBox.PlotType.SW2);
        fooBox.getActivity().initPlot(switch_plot3, FooBox.PlotType.SW3);
        fooBox.getActivity().initPlot(switch_plot4, FooBox.PlotType.SW4);

	    // Init overflow actions
	    View.OnClickListener overflowListener = new View.OnClickListener() {
		    @Override
		    public void onClick(View v) {
			    final int switchIndex = Integer.parseInt((String) v.getTag());
			    PopupMenu popupMenu = new PopupMenu(context, v);
			    Menu menu = popupMenu.getMenu();
			    popupMenu.getMenuInflater().inflate(R.menu.switch_overflow, menu);
			    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
				    @Override
				    public boolean onMenuItemClick(MenuItem item) {
					    switch (item.getItemId()) {
						    case R.id.action_devicesList:
							    showConnectedDevices(switchIndex);
							    break;
					    }
					    return false;
				    }
			    });

			    popupMenu.show();
		    }
	    };
	    rootView.findViewById(R.id.switch1_overflow).setOnClickListener(overflowListener);
	    rootView.findViewById(R.id.switch2_overflow).setOnClickListener(overflowListener);
	    rootView.findViewById(R.id.switch3_overflow).setOnClickListener(overflowListener);
	    rootView.findViewById(R.id.switch4_overflow).setOnClickListener(overflowListener);

        return rootView;
    }

	private void showConnectedDevices(int switchIndex) {
		LayoutInflater inflater = LayoutInflater.from(context);
		@SuppressLint("InflateParams")
		final View dialogLayout = inflater.inflate(R.layout.switch_connecteddevicesdialog, null, false);

		SwitchPortStatuses switchPortStatuses = FooBox.getInstance().getFreebox().getSwitchPortStatuses();
		final SwitchPortStatus switchPortStatus = switchPortStatuses.getSwitchPortStatus(switchIndex);

		Runnable loadList = new Runnable() {
			@Override
			public void run() {
				List<SwitchDevice> connectedDevices = switchPortStatus.getConnectedDevices();
				switchPortStatus.setFetched();

				dialogLayout.findViewById(R.id.progressBar).setVisibility(View.GONE);
				if (connectedDevices.isEmpty())
					dialogLayout.findViewById(R.id.switch_noConnectedDevices).setVisibility(View.VISIBLE);
				else {
					ListView listView = (ListView) dialogLayout.findViewById(R.id.listView);
					SwitchDevicesAdapter adapter = new SwitchDevicesAdapter(context, connectedDevices);
					listView.setAdapter(adapter);
				}
			}
		};

		if (switchPortStatus.isFetched())
			loadList.run();
		else {
			dialogLayout.findViewById(R.id.progressBar).setVisibility(View.VISIBLE);
			new SwitchPortStatusLoader(FooBox.getInstance().getFreebox(), loadList, switchPortStatuses).execute();
		}

		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setView(dialogLayout);
		builder.setNeutralButton(R.string.close, null);
		builder.setTitle(getString(R.string.tab6_name) + " " + switchIndex + " - " + getString(R.string.connected_devices));
		builder.show();
	}
}
