package com.chteuchteu.freeboxstats;

import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.androidplot.ui.AnchorPosition;
import com.androidplot.ui.DynamicTableModel;
import com.androidplot.ui.SizeLayoutType;
import com.androidplot.ui.SizeMetrics;
import com.androidplot.ui.XLayoutStyle;
import com.androidplot.ui.YLayoutStyle;
import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.PointLabelFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XValueMarker;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYSeries;
import com.androidplot.xy.XYStepMode;
import com.astuetz.PagerSlidingTabStrip;
import com.chteuchteu.freeboxstats.hlpr.Enums.AuthorizeStatus;
import com.chteuchteu.freeboxstats.hlpr.Enums.FieldType;
import com.chteuchteu.freeboxstats.hlpr.Enums.GraphPrecision;
import com.chteuchteu.freeboxstats.hlpr.Enums.Period;
import com.chteuchteu.freeboxstats.hlpr.Enums.Unit;
import com.chteuchteu.freeboxstats.hlpr.SettingsHelper;
import com.chteuchteu.freeboxstats.hlpr.Util;
import com.chteuchteu.freeboxstats.hlpr.Util.Fonts.CustomFont;
import com.chteuchteu.freeboxstats.net.AskForAppToken;
import com.chteuchteu.freeboxstats.net.BillingService;
import com.chteuchteu.freeboxstats.net.ManualGraphLoader;
import com.chteuchteu.freeboxstats.obj.DataSet;
import com.chteuchteu.freeboxstats.obj.GraphsContainer;
import com.crashlytics.android.Crashlytics;

public class MainActivity extends FragmentActivity {
	private static FragmentActivity activity;
	public static Context context;
	private static MainActivityPagerAdapter pagerAdapter;
	private static ViewPager viewPager;
	private ActionBarDrawerToggle drawerToggle;
	
	private static Thread refreshThread;
	private static boolean justRefreshed;
	public static final int AUTOREFRESH_TIME = 20000;
	private static boolean graphsDisplayed;
	public static int appLoadingStep; // steps : 0/1/2=finished
	
	private static final String tab1Title = "Débit down";
	private static final String tab2Title = "Débit up";
	private static final String tab3Title = "Temp.";
	
	private static XYPlot plot1;
	private static XYPlot plot2;
	private static XYPlot plot3;
	
	private static MenuItem refreshMenuItem;
	private static MenuItem periodMenuItem;
	private static MenuItem spinningMenuItem;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Crashlytics.start(this);
		setContentView(R.layout.activity_main);
		
		context = this;
		activity = this;
		graphsDisplayed = false;
		justRefreshed = false;
		
		initDrawer();
		
		ActionBar actionBar = getActionBar();
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
		
		
		FooBox.getInstance(this).init();
		
		// Set font
		Util.Fonts.setFont(this, (ViewGroup) findViewById(R.id.viewroot), CustomFont.RobotoCondensed_Light);
	}
	
	public static void displayLoadingScreen() {
		Util.Fonts.setFont(context, (TextView) ((Activity) context).findViewById(R.id.tv_loadingtxt), CustomFont.RobotoCondensed_Light);
		((Activity) context).findViewById(R.id.ll_loading).setVisibility(View.VISIBLE);
	}
	
	public static void hideLoadingScreen() {
		Animation fadeOut = AnimationUtils.loadAnimation(context, android.R.anim.fade_out);
		fadeOut.setDuration(10000);
		final LinearLayout loadingView = (LinearLayout) ((Activity) context).findViewById(R.id.ll_loading);
		fadeOut.setAnimationListener(new AnimationListener() {
			@Override public void onAnimationStart(Animation animation) { }
			@Override public void onAnimationRepeat(Animation animation) { }
			@Override
			public void onAnimationEnd(Animation animation) {
				loadingView.setVisibility(View.GONE);
			}
		});
		loadingView.startAnimation(fadeOut);
	}
	
	public static void startRefreshThread() {
		if (FooBox.getInstance(context).getFreebox() == null)
			return;
		
		if (refreshThread != null && refreshThread.isAlive())
			refreshThread.interrupt();
		
		refreshThread = new Thread(new Runnable() {
			@Override
			public void run() {
				while (true) {
					try {
						if (!Thread.interrupted()) {
							Thread.sleep(AUTOREFRESH_TIME);
							if (Util.isScreenOn(context)) {
								if (justRefreshed) {
									justRefreshed = false;
									continue;
								}
								activity.runOnUiThread(new Runnable() {
									@Override
									public void run() {
										refreshGraph();
									}
								});
							}
						} else return;
					} catch (InterruptedException e) {
						if (FooBox.DEBUG)
							e.printStackTrace();
						Thread.currentThread().interrupt();
						return;
					}
				}
			}
		});
		refreshThread.start();
	}
	
	public static void stopRefreshThread() {
		if (refreshThread != null && refreshThread.isAlive())
			refreshThread.interrupt();
	}
	
	public static void displayGraphs() {
		if (graphsDisplayed)
			return;
		
		refreshMenuItem.setVisible(true);
		periodMenuItem.setVisible(true);
		
		pagerAdapter = new MainActivityPagerAdapter(activity.getSupportFragmentManager());
		viewPager = (ViewPager) activity.findViewById(R.id.pager);
		viewPager.setAdapter(pagerAdapter);
		
		// Let Android load all the tabs at once (= disable lazy load)
		viewPager.setOffscreenPageLimit(2);
		
		PagerSlidingTabStrip tabs = (PagerSlidingTabStrip) activity.findViewById(R.id.tabs);
		tabs.setViewPager(viewPager);
		tabs.setTextColor(Color.WHITE);
		
		graphsDisplayed = true;
	}
	
	public static class MainActivityPagerAdapter extends FragmentStatePagerAdapter {
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
	
	private static void initPlot(final XYPlot plot, int plotIndex) {
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
		
		// Legend
		if (plotIndex == 1 || plotIndex == 2)
			plot.getLegendWidget().setTableModel(new DynamicTableModel(1, 2));
		else
			plot.getLegendWidget().setTableModel(new DynamicTableModel(2, 2));
		plot.getLegendWidget().setSize(new SizeMetrics(180, SizeLayoutType.ABSOLUTE, 460, SizeLayoutType.ABSOLUTE));
		Paint bgPaint = new Paint();
		bgPaint.setARGB(100, 0, 0, 0);
		bgPaint.setStyle(Paint.Style.FILL);
		plot.getLegendWidget().setBackgroundPaint(bgPaint);
		plot.getLegendWidget().setPadding(10, 1, 1, 3);
		plot.getLegendWidget().position(160, XLayoutStyle.ABSOLUTE_FROM_LEFT,
				40, YLayoutStyle.ABSOLUTE_FROM_TOP, AnchorPosition.LEFT_TOP);
		
		plot.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				plot.getLegendWidget().setVisible(!plot.getLegendWidget().isVisible());
				plot.redraw();
			}
		});
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
					XYSeries serie = new SimpleXYSeries(dSet.getValues(), SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, dSet.getField().getDisplayName());
					
					LineAndPointFormatter serieFormat = new LineAndPointFormatter();
					serieFormat.setPointLabelFormatter(new PointLabelFormatter());
					
					int xmlRef = -1;
					switch (dSet.getField()) {
						case BW_DOWN:
						case BW_UP:		xmlRef = R.xml.serieformat_bw;		break;
						case RATE_DOWN:	xmlRef = R.xml.serieformat_rateup;	break;
						case RATE_UP: 	xmlRef = R.xml.serieformat_ratedown;break;
						case CPUM:		xmlRef = R.xml.serieformat_cpum;	break;
						case CPUB:		xmlRef = R.xml.serieformat_cpub;	break;
						case SW:		xmlRef = R.xml.serieformat_sw;		break;
						case HDD:		xmlRef = R.xml.serieformat_hdd;		break;
						default: break;
					}
					if (xmlRef != -1)
						serieFormat.configure(context, xmlRef);
					
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
				
				if (plotIndex == 3)
					toggleSpinningMenuItem(false);
			}
		});
	}
	
	public static void toggleSpinningMenuItem(boolean visible) {
		spinningMenuItem.setVisible(visible);
		if (visible)
			spinningMenuItem.setActionView(new ProgressBar(context));
		else
			spinningMenuItem.setActionView(null);
	}
	
	public static void refreshGraph() { refreshGraph(false); }
	public static void refreshGraph(boolean manualRefresh) {
		if (FooBox.getInstance(context).getFreebox() == null)
			return;
		
		if (manualRefresh)
			justRefreshed = true;
		
		toggleSpinningMenuItem(true);
		
		new ManualGraphLoader(FooBox.getInstance().getFreebox(), FooBox.getInstance().getPeriod()).execute();
	}
	
	public static void displayLaunchPairingScreen() {
		activity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Util.Fonts.setFont(context, (TextView) activity.findViewById(R.id.firstlaunch_text1), CustomFont.RobotoCondensed_Light);
				Util.Fonts.setFont(context, (TextView) activity.findViewById(R.id.firstlaunch_text2), CustomFont.RobotoCondensed_Light);
				
				activity.findViewById(R.id.firstlaunch).setVisibility(View.VISIBLE);
				
				activity.findViewById(R.id.firstlaunch_ok).setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						Util.Fonts.setFont(context, (TextView) activity.findViewById(R.id.firstlaunch_text3), CustomFont.RobotoCondensed_Light);
						activity.findViewById(R.id.screen1).setVisibility(View.GONE);
						activity.findViewById(R.id.screen2).setVisibility(View.VISIBLE);
						new AskForAppToken(FooBox.getInstance().getFreebox(), context).execute();
					}
				});
			}
		});
	}
	
	public static void pairingFinished(final AuthorizeStatus aStatus) {
		activity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				activity.findViewById(R.id.screen2).setVisibility(View.GONE);
				displayGraphs();
			}
		});
	}
	
	public static void sessionOpenFailed() {
		activity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(context, "Erreur lors de la connexion avec la Freebox", Toast.LENGTH_SHORT).show();
			}
		});
	}
	
	public static void graphLoadingFailed() {
		activity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(context, "Erreur lors du chargement des graphiques", Toast.LENGTH_SHORT).show();
			}
		});
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		refreshMenuItem = menu.findItem(R.id.action_refresh);
		refreshMenuItem.setVisible(false);
		periodMenuItem = menu.findItem(R.id.period);
		periodMenuItem.setVisible(false);
		spinningMenuItem = menu.findItem(R.id.menu_progressbar);
		
		return true;
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
		if (spinningMenuItem != null) // if true, that means that the app has already started
			refreshGraph();
		
		if (refreshThread != null)
			startRefreshThread();
	}
	
	@Override
	public void onPause() {
		super.onPause();
		
		if (refreshThread != null)
			stopRefreshThread();
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		BillingService.getInstance().unbind();
	}
	
	private void initDrawer() {
		DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		drawerToggle = new ActionBarDrawerToggle(this, drawerLayout,
				R.drawable.ic_navigation_drawer, R.string.app_name, R.string.app_name) {
			public void onDrawerClosed(View view) {
				super.onDrawerClosed(view);
			}
			public void onDrawerOpened(View view) {
				super.onDrawerOpened(view);
			}
		};
		drawerLayout.setDrawerListener(drawerToggle);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setHomeButtonEnabled(true);
		
		findViewById(R.id.drawer_settings).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				LayoutInflater inflater = LayoutInflater.from(context);
				View dialog_layout = inflater.inflate(R.layout.settings_dialog, (ViewGroup) findViewById(R.id.root_layout));
				
				final CheckBox settings_autorefresh = (CheckBox) dialog_layout.findViewById(R.id.settings_autorefresh);
				settings_autorefresh.setChecked(SettingsHelper.getInstance().getAutoRefresh());
				final Spinner settings_graphPrecision = (Spinner) dialog_layout.findViewById(R.id.settings_graphprecision);
				ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, GraphPrecision.getStringArray());
				adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
				settings_graphPrecision.setAdapter(adapter);
				settings_graphPrecision.setSelection(SettingsHelper.getInstance().getGraphPrecision().getIndex());
				
				AlertDialog.Builder builder = new AlertDialog.Builder(context);
				builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						SettingsHelper.getInstance().setAutoRefresh(settings_autorefresh.isChecked());
						SettingsHelper.getInstance().setGraphPrecision(
								GraphPrecision.get(settings_graphPrecision.getSelectedItemPosition()));
						
						if (settings_autorefresh.isChecked())
							startRefreshThread();
						else
							stopRefreshThread();
						
						refreshGraph();
					}
				});
				builder.setNegativeButton("Annuler", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						settings_autorefresh.setChecked(SettingsHelper.getInstance().getAutoRefresh());
						settings_graphPrecision.setSelection(SettingsHelper.getInstance().getGraphPrecision().getIndex());
						dialog.dismiss();
					}
				});
				builder.setView(dialog_layout);
				builder.show();
			}
		});
		
		findViewById(R.id.drawer_premium).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				BillingService.getInstance().launchPurchase();
			}
		});
	}
	
	public static void isPremium() {
		//TODO activity.findViewById(R.id.drawer_premium).setVisibility(View.GONE);
	}
	
	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		drawerToggle.syncState();
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		drawerToggle.onConfigurationChanged(newConfig);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) { 
		if (requestCode == BillingService.REQUEST_CODE) {           
			//int responseCode = data.getIntExtra("RESPONSE_CODE", 0);
			String purchaseData = data.getStringExtra("INAPP_PURCHASE_DATA");
			//String dataSignature = data.getStringExtra("INAPP_DATA_SIGNATURE");
			
			if (resultCode == RESULT_OK) {
				try {
					JSONObject jo = new JSONObject(purchaseData);
					/*String sku = */jo.getString("productId");
					Toast.makeText(context, "Merci d'avoir acheté la version premium !", Toast.LENGTH_SHORT);
				} catch (JSONException e) {
					Toast.makeText(context, "Erreur lors de l'achat, veuillez réessayer...", Toast.LENGTH_SHORT);
					e.printStackTrace();
				}
			} else
				Toast.makeText(context, "Erreur lors de l'achat, veuillez réessayer...", Toast.LENGTH_SHORT);
		}
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (drawerToggle.onOptionsItemSelected(item))
			return true;
		
		switch (item.getItemId()) {
			case R.id.action_refresh:
				refreshGraph(true);
				break;
			case R.id.period:
				AlertDialog.Builder builderSingle = new AlertDialog.Builder(context);
				final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
						context, android.R.layout.simple_list_item_1);
				for (Period period : Period.values())
					arrayAdapter.add(period.getLabel());
				
				builderSingle.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						FooBox.getInstance().setPeriod(Period.get(which));
						periodMenuItem.setTitle(Period.get(which).getLabel());
						refreshGraph();
					}
				});
				builderSingle.show();
				break;
			default: super.onOptionsItemSelected(item); break;
		}
		return true;
	}
}