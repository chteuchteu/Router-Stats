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
		// androidPlot.graphWidget.marginTop="@dimen/graphwidget_margin_top"
		// plot1.getGraphWidget().setMarginTop(R.dimen.graphwidget_margin_top);
		// androidPlot.graphWidget.marginLeft="@dimen/graphwidget_margin_left"
		// plot1.getGraphWidget().setMarginLeft(R.dimen.graphwidget_margin_left);
		// androidPlot.graphWidget.marginBottom="@dimen/graphwidget_margin_bottom"
		// plot1.getGraphWidget().setMarginBottom(R.dimen.graphwidget_margin_bottom);
		// androidPlot.graphWidget.marginRight="@dimen/graphwidget_margin_right"
		// plot1.getGraphWidget().setMarginRight(R.dimen.graphwidget_margin_right);
		// androidPlot.domainLabelWidget.labelPaint.textSize="@dimen/graph_labels_font_size"
		plot1.getDomainLabelWidget().getLabelPaint().setTextSize(R.dimen.graph_labels_font_size);
		// androidPlot.graphWidget.rangeTickLabelPaint.textSize="@dimen/graph_labels_font_size"
		plot1.getGraphWidget().getRangeTickLabelPaint().setTextSize(R.dimen.graph_labels_font_size);
		// androidPlot.graphWidget.rangeOriginTickLabelPaint.textSize="@dimen/graph_labels_font_size"
		plot1.getGraphWidget().getRangeOriginTickLabelPaint().setTextSize(R.dimen.graph_labels_font_size);
		// androidPlot.graphWidget.domainTickLabelPaint.textSize="@dimen/graph_labels_font_size"
		plot1.getGraphWidget().getDomainTickLabelPaint().setTextSize(R.dimen.graph_labels_font_size);
		// androidPlot.graphWidget.domainOriginTickLabelPaint.textSize="@dimen/graph_labels_font_size"
		plot1.getGraphWidget().getDomainOriginTickLabelPaint().setTextSize(R.dimen.graph_labels_font_size);

		XYPlot plot2 = (XYPlot) rootView.findViewById(R.id.xyPlot2);
		// androidPlot.graphWidget.marginTop="@dimen/graphwidget_margin_top"
		// plot2.getGraphWidget().setMarginTop(R.dimen.graphwidget_margin_top);
		// androidPlot.graphWidget.marginLeft="@dimen/graphwidget_margin_left"
		// plot2.getGraphWidget().setMarginLeft(R.dimen.graphwidget_margin_left);
		// androidPlot.graphWidget.marginBottom="@dimen/graphwidget_margin_bottom"
		// plot2.getGraphWidget().setMarginBottom(R.dimen.graphwidget_margin_bottom);
		// androidPlot.graphWidget.marginRight="@dimen/graphwidget_margin_right"
		// plot2.getGraphWidget().setMarginRight(R.dimen.graphwidget_margin_right);
		// androidPlot.domainLabelWidget.labelPaint.textSize="@dimen/graph_labels_font_size"
		plot2.getDomainLabelWidget().getLabelPaint().setTextSize(R.dimen.graph_labels_font_size);
		// androidPlot.graphWidget.rangeTickLabelPaint.textSize="@dimen/graph_labels_font_size"
		plot2.getGraphWidget().getRangeTickLabelPaint().setTextSize(R.dimen.graph_labels_font_size);
		// androidPlot.graphWidget.rangeOriginTickLabelPaint.textSize="@dimen/graph_labels_font_size"
		plot2.getGraphWidget().getRangeOriginTickLabelPaint().setTextSize(R.dimen.graph_labels_font_size);
		// androidPlot.graphWidget.domainTickLabelPaint.textSize="@dimen/graph_labels_font_size"
		plot2.getGraphWidget().getDomainTickLabelPaint().setTextSize(R.dimen.graph_labels_font_size);
		// androidPlot.graphWidget.domainOriginTickLabelPaint.textSize="@dimen/graph_labels_font_size"
		plot2.getGraphWidget().getDomainOriginTickLabelPaint().setTextSize(R.dimen.graph_labels_font_size);

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
