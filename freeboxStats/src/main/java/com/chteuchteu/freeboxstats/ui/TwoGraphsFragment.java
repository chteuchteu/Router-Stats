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
 * TwoGraphsFragment - Used for up & down rate
 */
public class TwoGraphsFragment extends Fragment {
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_twographs, container, false);
		
		XYPlot plot1 = (XYPlot) rootView.findViewById(R.id.xyPlot1);
		XYPlot plot2 = (XYPlot) rootView.findViewById(R.id.xyPlot2);

		FooBox fooBox = FooBox.getInstance();
		fooBox.getPlots().put(Enums.Graph.RateDown, plot1);
		fooBox.getPlots().put(Enums.Graph.RateUp, plot2);
		fooBox.getGraphsTitles().put(Enums.Graph.RateDown, (TextView) rootView.findViewById(R.id.graphTitle1));
		fooBox.getGraphsTitles().put(Enums.Graph.RateUp, (TextView) rootView.findViewById(R.id.graphTitle2));
		fooBox.getProgressBars().put(Enums.Graph.RateDown, (ProgressBar) rootView.findViewById(R.id.progressBar1));
		fooBox.getProgressBars().put(Enums.Graph.RateUp, (ProgressBar) rootView.findViewById(R.id.progressBar2));

		fooBox.getActivity().initPlot(Enums.Graph.RateDown);
		fooBox.getActivity().initPlot(Enums.Graph.RateUp);

		return rootView;
	}
}
