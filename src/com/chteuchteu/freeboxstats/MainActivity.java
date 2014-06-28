package com.chteuchteu.freeboxstats;

import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;
import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.PointLabelFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYSeries;
import com.androidplot.xy.XYStepMode;
import com.chteuchteu.freeboxstats.hlpr.Enums.AuthorizeStatus;
import com.chteuchteu.freeboxstats.hlpr.Enums.Field;
import com.chteuchteu.freeboxstats.hlpr.Enums.Period;
import com.chteuchteu.freeboxstats.hlpr.Util;
import com.chteuchteu.freeboxstats.net.AskForAppToken;
import com.chteuchteu.freeboxstats.net.GraphLoader;
import com.chteuchteu.freeboxstats.obj.DataSet;
import com.chteuchteu.freeboxstats.obj.GraphsContainer;

public class MainActivity extends ActionBarActivity {
	private static Context context;
	private static XYPlot plot;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		context = this;
		findViewById(R.id.firstLaunch_getAppToken).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				new AskForAppToken(SingleBox.getInstance().getFreebox()).execute();
			}
		});
		
		findViewById(R.id.xyPlot).setVisibility(View.GONE);
		
		// Load singleton
		SingleBox.getInstance(this).init();
	}
	
	public static void loadGraph(final GraphsContainer graphsContainer, final Period period) {
		((Activity) context).runOnUiThread(new Runnable() {
			@SuppressLint("SimpleDateFormat")
			@SuppressWarnings("serial")
			@Override
			public void run() {
				plot = (XYPlot) ((Activity) context).findViewById(R.id.xyPlot);
				plot.setVisibility(View.VISIBLE);
				
				// Styling
				plot.setBorderStyle(XYPlot.BorderStyle.NONE, null, null);
				plot.setPlotMargins(0, 0, 0, 0);
				plot.setPlotPadding(0, 0, 0, 0);
				plot.setGridPadding(0, 10, 5, 0);
				plot.getGraphWidget().getBackgroundPaint().setColor(Color.TRANSPARENT);
				plot.getGraphWidget().getGridBackgroundPaint().setColor(Color.TRANSPARENT);
				plot.getGraphWidget().getDomainLabelPaint().setColor(Color.BLACK);
				plot.getGraphWidget().getRangeLabelPaint().setColor(Color.BLACK);
				plot.getGraphWidget().getDomainOriginLabelPaint().setColor(Color.BLACK);
				plot.getGraphWidget().getDomainOriginLinePaint().setColor(Color.BLACK);
				plot.getGraphWidget().getRangeOriginLinePaint().setColor(Color.BLACK);
				
				plot.setTitle("Débit");
				
				plot.clear();
				plot.getSeriesSet().clear();
				
				for (DataSet dSet : graphsContainer.getDataSets()) {
					XYSeries serie = new SimpleXYSeries(dSet.getValues(), SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, dSet.getField().name());
					
					LineAndPointFormatter serieFormat = new LineAndPointFormatter();
					serieFormat.setPointLabelFormatter(new PointLabelFormatter());
					serieFormat.configure(context, R.xml.line_point_formatter_with_plf1);
					if (dSet.getField() != Field.BW_DOWN) {
						Paint paint = new Paint();
						paint.setColor(Color.GREEN);
						serieFormat.setFillPaint(paint);
					}
					
					plot.addSeries(serie, serieFormat);
				}
				plot.setDomainStep(XYStepMode.INCREMENT_BY_VAL, 1);
				plot.setDomainValueFormat(new Format() {
					@Override
					public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
						int position = ((Number) obj).intValue();
						String label = Util.Times.getLabel(period, graphsContainer.getSerie().get(position), position, graphsContainer.getSerie());
						return new StringBuffer(label);
					}
					
					@Override
					public Object parseObject(String source, ParsePosition pos) {
						return null;
					}
				});
				
				
				//plot.setTicksPerRangeLabel(3);
				plot.redraw();
			}
		});
	}
	
	private void updateGraph() {
		ArrayList<Field> fields = new ArrayList<Field>();
		fields.add(Field.RATE_DOWN);
		fields.add(Field.BW_DOWN);
		
		if (SingleBox.getInstance(context).getFreebox() != null)
			new GraphLoader(SingleBox.getInstance().getFreebox(), Period.HOUR, fields).execute();
	}
	
	public static void displayLaunchPairingButton() {
		((Activity) context).runOnUiThread(new Runnable() {
			@Override
			public void run() {
				((Activity) context).findViewById(R.id.firstLaunch).setVisibility(View.VISIBLE);
			}
		});
	}
	
	public static void hideLaunchPairingButton() {
		((Activity) context).runOnUiThread(new Runnable() {
			@Override
			public void run() {
				((Activity) context).findViewById(R.id.firstLaunch).setVisibility(View.GONE);
			}
		});
	}
	
	public static void pairingFinished(final AuthorizeStatus aStatus) {
		((Activity) context).runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(context, "Pairing terminé : " + aStatus.name() + ".", Toast.LENGTH_SHORT).show();
			}
		});
	}
	
	public static void sessionOpenFailed() {
		((Activity) context).runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(context, "Erreur lors de la connexion avec la Freebox", Toast.LENGTH_SHORT).show();
			}
		});
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_refresh:
				updateGraph();
				break;
			default: super.onOptionsItemSelected(item); break;
		}
		return true;
	}
}