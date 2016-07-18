package com.chteuchteu.freeboxstats.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.androidplot.xy.XYPlot;
import com.chteuchteu.freeboxstats.FooBox;
import com.chteuchteu.freeboxstats.R;
import com.chteuchteu.freeboxstats.hlpr.Enums;

/**
 * Single graph fragment, used for Temp + XDSL
 */
public class GraphFragment extends Fragment {
	public static final String ARG_GRAPH = "graphType";
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_graph, container, false);
		
		Bundle args = getArguments();
		String graphArg = args.getString(ARG_GRAPH);
		XYPlot plot = (XYPlot) rootView.findViewById(R.id.xyPlot);

		Enums.Graph graph = null;

		switch (graphArg) {
			case "temp": graph = Enums.Graph.Temp; break;
			case "xdsl": graph = Enums.Graph.XDSL; break;
		}

		TextView graphTitle = (TextView) rootView.findViewById(R.id.graphTitle);
		if (graph == Enums.Graph.Temp)
			graphTitle.setText(R.string.temp);
		else if (graph == Enums.Graph.XDSL)
			graphTitle.setText(R.string.noise);

		FooBox.getInstance().getPlots().put(graph, plot);
		FooBox.getInstance().getGraphsTitles().put(graph, graphTitle);
		FooBox.getInstance().getProgressBars().put(graph, (ProgressBar) rootView.findViewById(R.id.progressBar));
		FooBox.getInstance().getActivity().initPlot(graph);

		return rootView;
	}
}
