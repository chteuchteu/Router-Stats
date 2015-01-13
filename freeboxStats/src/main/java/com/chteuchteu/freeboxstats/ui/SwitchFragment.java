package com.chteuchteu.freeboxstats.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.androidplot.xy.XYPlot;
import com.chteuchteu.freeboxstats.FooBox;
import com.chteuchteu.freeboxstats.R;

public class SwitchFragment extends Fragment {
    private static final int PLOT_INDEX1 = 6;
    private static final int PLOT_INDEX2 = 7;
    private static final int PLOT_INDEX3 = 8;
    private static final int PLOT_INDEX4 = 9;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_switch, container, false);

        XYPlot switch_plot1 = (XYPlot) rootView.findViewById(R.id.xyPlot_switch1);
        XYPlot switch_plot2 = (XYPlot) rootView.findViewById(R.id.xyPlot_switch2);
        XYPlot switch_plot3 = (XYPlot) rootView.findViewById(R.id.xyPlot_switch3);
        XYPlot switch_plot4 = (XYPlot) rootView.findViewById(R.id.xyPlot_switch4);

        FooBox.getInstance().setPlot(switch_plot1, PLOT_INDEX1);
        FooBox.getInstance().setPlot(switch_plot2, PLOT_INDEX2);
        FooBox.getInstance().setPlot(switch_plot3, PLOT_INDEX3);
        FooBox.getInstance().setPlot(switch_plot4, PLOT_INDEX4);

        MainActivity.initPlot(switch_plot1, PLOT_INDEX1);
        MainActivity.initPlot(switch_plot2, PLOT_INDEX2);
        MainActivity.initPlot(switch_plot3, PLOT_INDEX3);
        MainActivity.initPlot(switch_plot4, PLOT_INDEX4);

        return rootView;
    }
}
