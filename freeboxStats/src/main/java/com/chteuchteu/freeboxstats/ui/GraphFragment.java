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
	public static final String ARG_OBJECT = "object";
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_graph, container, false);
		
		Bundle args = getArguments();
		int index = args.getInt(ARG_OBJECT);
		XYPlot plot = (XYPlot) rootView.findViewById(R.id.xyPlot);
		
		FooBox.getInstance().setPlot(plot, index);
		FooBox.getInstance().setFragmentRootView(rootView, index);
		
		MainActivity.initPlot(plot, index);
		
		// Change container background color if needed
		if (index == 4)
			MainActivity.updateApplicationTheme();
		
		
		return rootView;
	}
}