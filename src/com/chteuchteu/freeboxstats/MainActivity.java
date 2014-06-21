package com.chteuchteu.freeboxstats;

import java.util.ArrayList;
import java.util.Arrays;

import org.json.JSONArray;
import org.json.JSONException;

import android.app.Activity;
import android.content.Context;
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
import com.chteuchteu.freeboxstats.hlpr.Enums.AuthorizeStatus;
import com.chteuchteu.freeboxstats.hlpr.GraphHelper;
import com.chteuchteu.freeboxstats.net.AskForAppToken;
import com.chteuchteu.freeboxstats.net.GraphLoader;

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
		
		// Load singleton
		SingleBox.getInstance(this);
		SingleBox.getInstance(this).init();
	}
	
	public static void loadGraph(final JSONArray data, final ArrayList<String> fields) {
		((Activity) context).runOnUiThread(new Runnable() {
			@Override
			public void run() {
				try {
					plot = (XYPlot) ((Activity) context).findViewById(R.id.xyPlot);
					
					for (String field : fields) {
						Number[] serieData = GraphHelper.jsonArrayToData(field, data);
						
						XYSeries serie = new SimpleXYSeries(Arrays.asList(serieData), SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, field);
						
						LineAndPointFormatter serieFormat = new LineAndPointFormatter();
						serieFormat.setPointLabelFormatter(new PointLabelFormatter());
						serieFormat.configure(context, R.xml.line_point_formatter_with_plf1);
						plot.addSeries(serie, serieFormat);
					}
					
					plot.setTicksPerRangeLabel(3);
					plot.redraw();
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	private void updateGraph() {
		if (SingleBox.getInstance(context).getFreebox() != null)
			new GraphLoader(SingleBox.getInstance().getFreebox()).execute();
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