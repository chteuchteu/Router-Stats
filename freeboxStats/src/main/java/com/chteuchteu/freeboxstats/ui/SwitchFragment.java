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
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
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

        return rootView;
    }
}
