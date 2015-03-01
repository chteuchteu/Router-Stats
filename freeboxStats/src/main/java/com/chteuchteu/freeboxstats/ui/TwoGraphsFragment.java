package com.chteuchteu.freeboxstats.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.androidplot.xy.XYPlot;
import com.chteuchteu.freeboxstats.FooBox;
import com.chteuchteu.freeboxstats.R;

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

		fooBox.setPlot(plot1, FooBox.PlotType.RATEDOWN);
		fooBox.setPlot(plot2, FooBox.PlotType.RATEUP);

		fooBox.getActivity().initPlot(plot1, FooBox.PlotType.RATEDOWN);
		fooBox.getActivity().initPlot(plot2, FooBox.PlotType.RATEUP);

		
		return rootView;
	}
}
