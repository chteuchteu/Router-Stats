package com.chteuchteu.freeboxstats.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.androidplot.xy.XYPlot;
import com.chteuchteu.freeboxstats.FooBox;
import com.chteuchteu.freeboxstats.R;
import com.chteuchteu.freeboxstats.hlpr.Enums;

public class GraphFragment extends Fragment {
	public static final String ARG_GRAPH = "graphType";
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_graph, container, false);
		
		Bundle args = getArguments();
		String graphArg = args.getString(ARG_GRAPH);
		XYPlot plot = (XYPlot) rootView.findViewById(R.id.xyPlot);

		Enums.Graph graph;

		switch (graphArg) {
			case "temp": graph = Enums.Graph.Temp; break;
			case "xdsl": graph = Enums.Graph.XDSL; break;
			default: graph = null; break;
		}

		FooBox.getInstance().getPlots().put(graph, plot);
		FooBox.getInstance().getProgressBars().put(graph, (ProgressBar) rootView.findViewById(R.id.progressBar));
		FooBox.getInstance().getActivity().initPlot(graph);
		
		
		return rootView;
	}
}
