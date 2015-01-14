package com.chteuchteu.freeboxstats.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.androidplot.xy.XYPlot;
import com.chteuchteu.freeboxstats.FooBox;
import com.chteuchteu.freeboxstats.R;

public class StackFragment extends Fragment {
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_stack, container, false);
		
		XYPlot stack_plot = (XYPlot) rootView.findViewById(R.id.xyPlot);
		
		FooBox.getInstance().setPlot(stack_plot, FooBox.PlotType.STACK);

		FooBox.getInstance().getActivity().initPlot(stack_plot, FooBox.PlotType.STACK);
		
		return rootView;
	}
}