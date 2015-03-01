package com.chteuchteu.freeboxstats.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.androidplot.xy.XYPlot;
import com.chteuchteu.freeboxstats.FooBox;
import com.chteuchteu.freeboxstats.R;

public class GraphFragment extends Fragment {
	public static final String ARG_GRAPHTYPE = "graphType";
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_graph, container, false);
		
		Bundle args = getArguments();
		String type = args.getString(ARG_GRAPHTYPE);
		XYPlot plot = (XYPlot) rootView.findViewById(R.id.xyPlot);

		FooBox.PlotType plotType;

		switch (type) {
			case "temp": plotType = FooBox.PlotType.TEMP; break;
			case "xdsl": plotType = FooBox.PlotType.XDSL; break;
			default: plotType = null; break;
		}

		FooBox.getInstance().setPlot(plot, plotType);
		FooBox.getInstance().getActivity().initPlot(plot, plotType);
		
		
		return rootView;
	}
}
