package com.chteuchteu.freeboxstats.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.chteuchteu.freeboxstats.FooBox;
import com.chteuchteu.freeboxstats.R;
import com.chteuchteu.freeboxstats.net.StackLoader;
import com.chteuchteu.freeboxstats.obj.Freebox;

public class StackFragment extends Fragment {
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_stack, container, false);
		
		Freebox freebox = FooBox.getInstance().getFreebox();
		
		FooBox.getInstance().stack_down = (TextView) rootView.findViewById(R.id.stack_down);
		FooBox.getInstance().stack_downUnit = (TextView) rootView.findViewById(R.id.stack_down_unit);
		FooBox.getInstance().stack_up = (TextView) rootView.findViewById(R.id.stack_up);
		FooBox.getInstance().stack_upUnit = (TextView) rootView.findViewById(R.id.stack_up_unit);
		
		if (freebox != null)
			new StackLoader(freebox).execute();
		
		return rootView;
	}
}