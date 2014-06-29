package com.chteuchteu.freeboxstats;

import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;
import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.PointLabelFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XValueMarker;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYSeries;
import com.androidplot.xy.XYStepMode;
import com.chteuchteu.freeboxstats.hlpr.Enums.AuthorizeStatus;
import com.chteuchteu.freeboxstats.hlpr.Enums.Field;
import com.chteuchteu.freeboxstats.hlpr.Enums.FieldType;
import com.chteuchteu.freeboxstats.hlpr.Enums.Period;
import com.chteuchteu.freeboxstats.hlpr.Enums.Unit;
import com.chteuchteu.freeboxstats.hlpr.Util;
import com.chteuchteu.freeboxstats.net.AskForAppToken;
import com.chteuchteu.freeboxstats.net.GraphLoader;
import com.chteuchteu.freeboxstats.obj.DataSet;
import com.chteuchteu.freeboxstats.obj.GraphsContainer;

public class MainActivity extends FragmentActivity {
	private static Context context;
	private MainActivityPagerAdapter pagerAdapter;
	private ViewPager viewPager;
	
	private static final String tab1Title = "Débit down";
	private static final String tab2Title = "Débit up";
	private static final String tab3Title = "Temp.";
	
	private static XYPlot plot1;
	private static XYPlot plot2;
	private static XYPlot plot3;
	
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
		
		pagerAdapter = new MainActivityPagerAdapter(getSupportFragmentManager());
		viewPager = (ViewPager) findViewById(R.id.pager);
		viewPager.setAdapter(pagerAdapter);
		viewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
			@Override
			public void onPageSelected(int position) {
				getActionBar().setSelectedNavigationItem(position);
			}
		});
		// Let Android load all the tabs at once (= disable lazy load)
		viewPager.setOffscreenPageLimit(2);
		
		final ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		ActionBar.TabListener tabListener = new ActionBar.TabListener() {
			@Override public void onTabUnselected(Tab tab, FragmentTransaction ft) { }
			
			@Override
			public void onTabSelected(Tab tab, FragmentTransaction ft) {
				viewPager.setCurrentItem(tab.getPosition());
			}
			
			@Override public void onTabReselected(Tab tab, FragmentTransaction ft) { }
		};
		actionBar.addTab(actionBar.newTab().setText(tab1Title).setTabListener(tabListener));
		actionBar.addTab(actionBar.newTab().setText(tab2Title).setTabListener(tabListener));
		actionBar.addTab(actionBar.newTab().setText(tab3Title).setTabListener(tabListener));
		
		// Some design
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			int id = getResources().getIdentifier("config_enableTranslucentDecor", "bool", "android");
			if (id != 0 && getResources().getBoolean(id)) { // Translucent available
				Window w = getWindow();
				w.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION, WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
				w.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
			}
		}
		actionBar.setBackgroundDrawable(new ColorDrawable(0xff3367D6));
		actionBar.setTitle("");
		
		
		// Load singleton
		SingleBox.getInstance(this).init();
	}
	
	public class MainActivityPagerAdapter extends FragmentStatePagerAdapter {
		public MainActivityPagerAdapter(FragmentManager fm) {
			super(fm);
		}
		
		@Override
		public Fragment getItem(int i) {
			Fragment fragment = new GraphFragment();
			Bundle args = new Bundle();
			args.putInt(GraphFragment.ARG_OBJECT, i+1);
			fragment.setArguments(args);
			return fragment;
		}
		
		@Override
		public int getCount() {
			return 3;
		}
		
		@Override
		public CharSequence getPageTitle(int position) {
			switch (position) {
				case 0: return tab1Title;
				case 1: return tab2Title;
				case 2: return tab3Title;
				default: return "Unknown";
			}
		}
	}
	
	public static class GraphFragment extends Fragment {
		public static final String ARG_OBJECT = "object";
		
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_graph, container, false);
			
			Bundle args = getArguments();
			int index = args.getInt(ARG_OBJECT);
			XYPlot plot = (XYPlot) rootView.findViewById(R.id.xyPlot);
			switch (index) {
				case 1: plot1 = plot; break;
				case 2: plot2 = plot; break;
				case 3: plot3 = plot; break;
			}
			initPlot(plot, index);
			
			return rootView;
		}
	}
	
	private static void initPlot(XYPlot plot, int plotIndex) {
		plot.setVisibility(View.GONE);
		
		// Styling
		plot.setBorderStyle(XYPlot.BorderStyle.NONE, null, null);
		plot.setPlotMargins(10, 0, 0, 10);
		plot.setPlotPadding(0, 0, 0, 0);
		plot.setGridPadding(4, 10, 15, 0);
		plot.getGraphWidget().getBackgroundPaint().setColor(Color.TRANSPARENT);
		plot.getGraphWidget().getGridBackgroundPaint().setColor(Color.TRANSPARENT);
		plot.getGraphWidget().getDomainLabelPaint().setColor(Color.GRAY);
		plot.getGraphWidget().getDomainGridLinePaint().setColor(Color.TRANSPARENT);
		plot.getGraphWidget().getDomainOriginLabelPaint().setColor(Color.GRAY);
		plot.getGraphWidget().getDomainOriginLinePaint().setColor(Color.GRAY);
		plot.getGraphWidget().getRangeLabelPaint().setColor(Color.GRAY);
		plot.getGraphWidget().getRangeOriginLabelPaint().setColor(Color.GRAY);
		plot.getGraphWidget().getRangeOriginLinePaint().setColor(Color.GRAY);
		
		if (plotIndex == 3) {
			plot.setRangeStep(XYStepMode.INCREMENT_BY_VAL, 10);
			plot.setRangeLowerBoundary(0, BoundaryMode.FIXED);
		}
	}
	
	public static void loadGraph(final int plotIndex, final GraphsContainer graphsContainer, final Period period, final FieldType fieldType, final Unit unit) {
		((Activity) context).runOnUiThread(new Runnable() {
			@SuppressWarnings("serial")
			@SuppressLint("SimpleDateFormat")
			@Override
			public void run() {
				XYPlot plot = null;
				switch (plotIndex) {
					case 1: plot = plot1; break;
					case 2: plot = plot2; break;
					case 3: plot = plot3; break;
				}
				plot.setVisibility(View.VISIBLE);
				
				// Reset plot
				plot.clear();
				plot.getSeriesSet().clear();
				plot.removeMarkers();
				
				for (DataSet dSet : graphsContainer.getDataSets()) {
					XYSeries serie = new SimpleXYSeries(dSet.getValues(), SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, dSet.getField().name());
					
					LineAndPointFormatter serieFormat = new LineAndPointFormatter();
					serieFormat.setPointLabelFormatter(new PointLabelFormatter());
					
					if (fieldType == FieldType.DATA) {
						if (dSet.getField() == Field.BW_DOWN || dSet.getField() == Field.BW_UP)
							serieFormat.configure(context, R.xml.serieformat_bw);
						else
							serieFormat.configure(context, R.xml.serieformat_ratedown);
					} else {
						switch (dSet.getField()) {
							case CPUM:	serieFormat.configure(context, R.xml.serieformat_cpum);	break;
							case CPUB:	serieFormat.configure(context, R.xml.serieformat_cpub);	break;
							case SW:	serieFormat.configure(context, R.xml.serieformat_sw);	break;
							case HDD:	serieFormat.configure(context, R.xml.serieformat_hdd);	break;
								default: break;
							
						}
					}
					
					plot.addSeries(serie, serieFormat);
				}
				
				// Add markers (vertical lines)
				for (XValueMarker m : Util.Times.getMarkers(period, graphsContainer.getSerie())) {
					m.getLinePaint().setARGB(30, 255, 255, 255);
					plot.addMarker(m);
				}
				
				// Add labels
				plot.setDomainStep(XYStepMode.INCREMENT_BY_VAL, 1);
				plot.setDomainValueFormat(new Format() {
					@Override
					public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
						int position = ((Number) obj).intValue();
						String label = Util.Times.getLabel(period, graphsContainer.getSerie().get(position), position, graphsContainer.getSerie());
						return new StringBuffer(label);
					}
					
					@Override public Object parseObject(String source, ParsePosition pos) { return null; }
				});
				
				// Set range label
				if (plotIndex == 1 || plotIndex == 2)
					plot.setRangeLabel("Débit (" + unit.name() + "/s)");
				else
					plot.setRangeLabel("Température (°C)");
				
				
				plot.redraw();
			}
		});
	}
	
	public static void updateGraph() {
		if (SingleBox.getInstance(context).getFreebox() == null)
			return;
		
		// First tab
		ArrayList<Field> fields = new ArrayList<Field>();
		fields.add(Field.RATE_DOWN);
		fields.add(Field.BW_DOWN);
		new GraphLoader(SingleBox.getInstance().getFreebox(), Period.HOUR, fields, 1, FieldType.DATA).execute();
		
		// Second tab
		ArrayList<Field> fields2 = new ArrayList<Field>();
		fields2.add(Field.RATE_UP);
		fields2.add(Field.BW_UP);
		new GraphLoader(SingleBox.getInstance().getFreebox(), Period.HOUR, fields2, 2, FieldType.DATA).execute();
		
		// Third tab
		ArrayList<Field> fields3 = new ArrayList<Field>();
		fields3.add(Field.CPUM);
		fields3.add(Field.CPUB);
		fields3.add(Field.SW);
		fields3.add(Field.HDD);
		new GraphLoader(SingleBox.getInstance().getFreebox(), Period.HOUR, fields3, 3, FieldType.TEMP).execute();
	}
	
	public static void displayLaunchPairingButton() {
		((Activity) context).runOnUiThread(new Runnable() {
			@Override
			public void run() {
				//((Activity) context).findViewById(R.id.firstLaunch).setVisibility(View.VISIBLE);
			}
		});
	}
	
	public static void hideLaunchPairingButton() {
		((Activity) context).runOnUiThread(new Runnable() {
			@Override
			public void run() {
				//((Activity) context).findViewById(R.id.firstLaunch).setVisibility(View.GONE);
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