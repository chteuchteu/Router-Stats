package com.chteuchteu.freeboxstats.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Paint;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.androidplot.ui.AnchorPosition;
import com.androidplot.ui.DynamicTableModel;
import com.androidplot.ui.Size;
import com.androidplot.ui.SizeLayoutType;
import com.androidplot.ui.XLayoutStyle;
import com.androidplot.ui.YLayoutStyle;
import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.PointLabelFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XValueMarker;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYStepMode;
import com.chteuchteu.freeboxstats.FooBox;
import com.chteuchteu.freeboxstats.R;
import com.chteuchteu.freeboxstats.async.AskForAppToken;
import com.chteuchteu.freeboxstats.async.FreeboxDiscoverer;
import com.chteuchteu.freeboxstats.async.GraphLoader;
import com.chteuchteu.freeboxstats.async.OutagesFetcher;
import com.chteuchteu.freeboxstats.async.SessionOpener;
import com.chteuchteu.freeboxstats.hlpr.DrawerHelper;
import com.chteuchteu.freeboxstats.hlpr.Enums;
import com.chteuchteu.freeboxstats.hlpr.Enums.AuthorizeStatus;
import com.chteuchteu.freeboxstats.hlpr.Enums.Period;
import com.chteuchteu.freeboxstats.hlpr.SettingsHelper;
import com.chteuchteu.freeboxstats.hlpr.Util;
import com.chteuchteu.freeboxstats.net.BillingService;
import com.chteuchteu.freeboxstats.obj.DataSet;
import com.chteuchteu.freeboxstats.obj.ValuesBag;

import java.text.DecimalFormat;
import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;

public class MainActivity extends FreeboxStatsActivity {
	private FooBox fooBox;
    private MainActivity activity;

	public static final int NB_TABS = 4;

	private Thread refreshThread;
	private boolean justRefreshed;
	private final int AUTOREFRESH_TIME = 20000;
	
	private MenuItem refreshMenuItem;
	private MenuItem periodMenuItem;
	private MenuItem validerMenuItem;

	private boolean appStarted = false;
	private boolean updating;
    private boolean graphsDisplayed;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
        this.viewToInflate = R.layout.activity_main;
		super.onCreate(savedInstanceState);

        activity = this;
		graphsDisplayed = false;
		justRefreshed = false;
		updating = false;

		this.drawerHelper = new DrawerHelper(this);
		this.drawerHelper.initDrawer();

		fooBox = FooBox.getInstance().init(this);
	}

	public void displayLoadingScreen() {
		findViewById(R.id.ll_loading).setVisibility(View.VISIBLE);
	}

	public void hideLoadingScreen() {
		findViewById(R.id.ll_loading).setVisibility(View.GONE);
	}

	public void startRefreshThread() {
		if (fooBox.getFreebox() == null)
			return;
		
		if (!SettingsHelper.getInstance().getAutoRefresh())
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
								runOnUiThread(new Runnable() {
									@Override
									public void run() {
										refreshGraph();
									}
								});
							}
						} else return;
					} catch (InterruptedException ex) {
						Thread.currentThread().interrupt();
						return;
					}
				}
			}
		});
		refreshThread.start();
	}

	public void stopRefreshThread() {
		if (refreshThread != null && refreshThread.isAlive())
			refreshThread.interrupt();
	}
	
	private void displayGraphs() {
		if (graphsDisplayed)
			return;
		
		refreshMenuItem.setVisible(true);
		periodMenuItem.setVisible(true);

		MainActivityPagerAdapter pagerAdapter = new MainActivityPagerAdapter(getSupportFragmentManager(), this);
		ViewPager viewPager = (ViewPager) findViewById(R.id.pager);
		viewPager.setAdapter(pagerAdapter);
		
		// Let Android load all the tabs at once (= disable lazy load)
		viewPager.setOffscreenPageLimit(NB_TABS - 1);

		TabLayout tabs = (TabLayout) findViewById(R.id.tabs);
		tabs.setupWithViewPager(viewPager);
		graphsDisplayed = true;
	}

	public void initPlot(Enums.Graph graph) {
		XYPlot plot = fooBox.getPlots().get(graph);
		plot.setVisibility(View.GONE);

		// Styling
		Paint transparentPaint = new Paint();
		transparentPaint.setAlpha(0);

		// Background
		plot.setBackgroundPaint(transparentPaint);
		plot.getGraphWidget().setBackgroundPaint(transparentPaint);
		plot.getGraphWidget().setGridBackgroundPaint(transparentPaint);

		// Border
		plot.setBorderPaint(transparentPaint);
		plot.getGraphWidget().setDomainGridLinePaint(transparentPaint);

		plot.setRangeLowerBoundary(0, BoundaryMode.FIXED);
		switch (graph) {
			case Temp:
				plot.setRangeStep(XYStepMode.INCREMENT_BY_VAL, 10);
				break;
			case XDSL:
				plot.setRangeStep(XYStepMode.INCREMENT_BY_VAL, 1);
				break;
			case Switch1:
			case Switch2:
			case Switch3:
			case Switch4:
				plot.setRangeStep(XYStepMode.INCREMENT_BY_PIXELS, 100);
				break;
		}

		// Legend
		switch (graph) {
			case RateDown:
			case RateUp:
				/* No legend */
				break;
			default:
				// Show legend
				if (graph == Enums.Graph.Temp)
					plot.getLegendWidget().setTableModel(new DynamicTableModel(2, 2));
				else
					plot.getLegendWidget().setTableModel(new DynamicTableModel(1, 2));

				plot.getLegendWidget().setSize(new Size(180, SizeLayoutType.ABSOLUTE, 460, SizeLayoutType.ABSOLUTE));

				Paint bgPaint = new Paint();
				bgPaint.setARGB(100, 0, 0, 0);
				bgPaint.setStyle(Paint.Style.FILL);
				plot.getLegendWidget().setBackgroundPaint(bgPaint);
				plot.getLegendWidget().setPadding(10, 1, 1, 3);
				plot.getLegendWidget().position(160, XLayoutStyle.ABSOLUTE_FROM_LEFT,
						40, YLayoutStyle.ABSOLUTE_FROM_TOP, AnchorPosition.LEFT_TOP);
				break;
		}

		// Set range label
		if (graph == Enums.Graph.Temp)
			plot.setRangeLabel(getString(R.string.temp));
		else if (graph == Enums.Graph.XDSL)
			plot.setRangeLabel(getString(R.string.noise));
		
		if (graph == Enums.Graph.Temp || graph == Enums.Graph.XDSL)
			plot.setRangeValueFormat(new DecimalFormat("#"));
	}

	@SuppressWarnings("serial")
	public void loadGraph(Enums.Graph graph) {
		final ValuesBag valuesBag = fooBox.getValuesBags().get(graph);
		final Period period = fooBox.getPeriod();
		XYPlot plot = fooBox.getPlots().get(graph);
		Enums.Unit unit = valuesBag.getValuesUnit();

		if (plot == null)
			return;

		if (plot.getVisibility() != View.VISIBLE)
			plot.setVisibility(View.VISIBLE);

		for (DataSet dataSet : valuesBag.getDataSets()) {
			if (dataSet.getXySerieRef() == null) {
				SimpleXYSeries serie = new SimpleXYSeries(dataSet.getValues(), SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, dataSet.getField().getDisplayName());

				// Configure serie
				dataSet.setXySerieRef(serie);

				LineAndPointFormatter serieFormat = new LineAndPointFormatter();
				serieFormat.setPointLabelFormatter(new PointLabelFormatter());

				int xmlRef = -1;
				switch (dataSet.getField()) {
					case BW_DOWN:
					case BW_UP:		xmlRef = R.xml.serieformat_bw;		break;
					case RATE_DOWN:	xmlRef = R.xml.serieformat_rateup;	break;
					case RATE_UP: 	xmlRef = R.xml.serieformat_ratedown;break;
					case CPUM:		xmlRef = R.xml.serieformat_cpum;	break;
					case CPUB:		xmlRef = R.xml.serieformat_cpub;	break;
					case SW:		xmlRef = R.xml.serieformat_sw;		break;
					case HDD:		xmlRef = R.xml.serieformat_hdd;		break;
					case SNR_UP:	xmlRef = R.xml.serieformat_snr_up;	break;
					case SNR_DOWN:	xmlRef = R.xml.serieformat_snr_down;break;
					case RX_1: case RX_2: case RX_3: case RX_4:
						xmlRef = R.xml.serieformat_ratedown; break;
					case TX_1: case TX_2: case TX_3: case TX_4:
						xmlRef = R.xml.serieformat_rateup; break;
					default: break;
				}
				if (xmlRef != -1)
					serieFormat.configure(context, xmlRef);

				plot.addSeries(serie, serieFormat);
			}
			else {
				SimpleXYSeries serie = dataSet.getXySerieRef();

				for (Number number : dataSet.getValues()) {
					serie.removeFirst();
					serie.addLast(number, number); // TODO put 0 in one of the args??
				}
			}
		}
		
		// Add markers (vertical lines)
		for (XValueMarker m : Util.Times.getMarkers(period, valuesBag.getSerie())) {
			m.getLinePaint().setARGB(30, 255, 255, 255);
			plot.addMarker(m);
		}
		
		// Add labels
		plot.setDomainStep(XYStepMode.INCREMENT_BY_VAL, 1);
		plot.setDomainValueFormat(new Format() {
			@Override
			public StringBuffer format(Object obj, @NonNull StringBuffer toAppendTo, @NonNull FieldPosition pos) {
				int position = ((Number) obj).intValue();
				String label = Util.Times.getLabel(period, valuesBag.getSerie().get(position), position, valuesBag.getSerie());
				return new StringBuffer(label);
			}
			
			@Override
			public Object parseObject(String source, @NonNull ParsePosition pos) {
				return null;
			}
		});
		
		// Set range label
		switch (graph) {
			case RateDown:
				plot.setRangeLabel(getString(R.string.rate_down) + " (" + unit.name() + "/s)");
				break;
			case RateUp:
				plot.setRangeLabel(getString(R.string.rate_up) + " (" + unit.name() + "/s)");
				break;
			case Switch1:
			case Switch2:
			case Switch3:
			case Switch4:
				plot.setRangeLabel(getString(R.string.rate) + " (" + unit.name() + "/s)");
				break;
		}

		plot.redraw();
	}

	public void displayDebugMenuItem() {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				View drawer_debug = findViewById(R.id.drawer_debug);
				if (drawer_debug.getVisibility() != View.VISIBLE)
					drawer_debug.setVisibility(View.VISIBLE);
			}
		});
	}

	public void displayOutagesDialog() {
		if (fooBox.getFreebox() == null)
			return;

		LayoutInflater inflater = LayoutInflater.from(context);
		View dialog_layout = inflater.inflate(R.layout.outages_dialog, (ViewGroup) findViewById(R.id.root_layout));
		
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setPositiveButton(R.string.close, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		builder.setView(dialog_layout);
		// Avoid error when the app is closing or something
		if (!isFinishing()) {
			AlertDialog dialog = builder.show();
			
			new OutagesFetcher(this, fooBox.getFreebox(), dialog, dialog_layout).execute();
		}
	}

	public void setUpdating(boolean val) { this.updating = val; }

	public void refreshGraph() { refreshGraph(false); }
	private void refreshGraph(boolean manualRefresh) {
		if (fooBox.getFreebox() == null)
			return;
		if (updating)
			return;
		
		updating = true;
		
		if (manualRefresh)
			justRefreshed = true;
		
		for (ProgressBar progressBar : fooBox.getProgressBars().values())
			progressBar.setVisibility(View.VISIBLE);

		for (Enums.Graph graph : fooBox.getGraphs())
			new GraphLoader(fooBox, this, graph).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
	}

	public void displayLaunchPairingScreen() {
		findViewById(R.id.firstlaunch).setVisibility(View.VISIBLE);
		findViewById(R.id.screen1).setVisibility(View.VISIBLE);
		
		
		findViewById(R.id.firstlaunch_ok).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				findViewById(R.id.screen1).setVisibility(View.GONE);
				findViewById(R.id.screen2).setVisibility(View.VISIBLE);
				new AskForAppToken(fooBox.getFreebox(), activity).execute();
			}
		});
	}

	public void pairingFinished(final AuthorizeStatus aStatus) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (aStatus == AuthorizeStatus.GRANTED) {
					findViewById(R.id.screen2).setVisibility(View.GONE);
					displayGraphs();
				}
			}
		});
	}

	public void sessionOpenFailed() {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(context, R.string.freebox_connection_fail, Toast.LENGTH_SHORT).show();
				if (findViewById(R.id.ll_loading).getVisibility() == View.VISIBLE) {
					// App loading
					findViewById(R.id.loadingprogressbar).setVisibility(View.GONE);
					findViewById(R.id.retrybutton).setVisibility(View.VISIBLE);
					final TextView chargement = (TextView) findViewById(R.id.tv_loadingtxt);
					chargement.setText(R.string.connection_failed);
					final TextView loadingFail = (TextView) findViewById(R.id.sessionfailmessage);
					loadingFail.setText(Html.fromHtml(getText(R.string.sessionopening_error).toString()));

					loadingFail.setVisibility(View.VISIBLE);
					findViewById(R.id.retrybutton).setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							findViewById(R.id.loadingprogressbar).setVisibility(View.VISIBLE);
							findViewById(R.id.retrybutton).setVisibility(View.GONE);
							new SessionOpener(fooBox.getFreebox(), activity).execute();
							chargement.setText(R.string.loading);
							loadingFail.setVisibility(View.GONE);
						}
					});
				}
			}
		});
	}

	public void displayFreeboxSearchFailedScreen() {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				findViewById(R.id.ll_loading).setVisibility(View.VISIBLE);

				findViewById(R.id.loadingprogressbar).setVisibility(View.GONE);
				findViewById(R.id.retrybutton).setVisibility(View.VISIBLE);
				final TextView chargement = (TextView) findViewById(R.id.tv_loadingtxt);
				chargement.setText(R.string.connection_failed);
				final TextView loadingFail = (TextView) findViewById(R.id.freeboxsearchfailmessage);
				loadingFail.setVisibility(View.VISIBLE);
				findViewById(R.id.retrybutton).setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						findViewById(R.id.loadingprogressbar).setVisibility(View.VISIBLE);
						findViewById(R.id.retrybutton).setVisibility(View.GONE);
						new FreeboxDiscoverer(activity).execute();
						chargement.setText(R.string.loading);
						loadingFail.setVisibility(View.GONE);
					}
				});
			}
		});
	}

	public void displayFreeboxUpdateNeededScreen() {
		findViewById(R.id.ll_loading).setVisibility(View.VISIBLE);
		
		findViewById(R.id.loadingprogressbar).setVisibility(View.GONE);
		findViewById(R.id.retrybutton).setVisibility(View.VISIBLE);
		final TextView chargement = (TextView) findViewById(R.id.tv_loadingtxt);
		chargement.setText(R.string.connection_failed);
		final TextView loadingFail = (TextView) findViewById(R.id.updatefreeboxmessage);
		loadingFail.setVisibility(View.VISIBLE);
		findViewById(R.id.retrybutton).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				findViewById(R.id.loadingprogressbar).setVisibility(View.VISIBLE);
				findViewById(R.id.retrybutton).setVisibility(View.GONE);
				chargement.setText(R.string.loading);
				loadingFail.setVisibility(View.GONE);
				findViewById(R.id.ll_loading).setVisibility(View.GONE);
				refreshGraph();
			}
		});
	}

	public void displayFreeboxUpdateNeededScreenBeforePairing() {
		findViewById(R.id.loadingprogressbar).setVisibility(View.GONE);
		findViewById(R.id.retrybutton).setVisibility(View.VISIBLE);
		final TextView chargement = (TextView) findViewById(R.id.tv_loadingtxt);
		chargement.setText(R.string.connection_failed);
		final TextView loadingFail = (TextView) findViewById(R.id.updatefreeboxmessage);
		loadingFail.setVisibility(View.VISIBLE);
		
		findViewById(R.id.retrybutton).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				findViewById(R.id.loadingprogressbar).setVisibility(View.VISIBLE);
				findViewById(R.id.retrybutton).setVisibility(View.GONE);
				new FreeboxDiscoverer(activity).execute();
				chargement.setText(R.string.loading);
				loadingFail.setVisibility(View.GONE);
			}
		});
	}

	public void graphLoadingFailed() {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(context, R.string.graphs_loading_fail, Toast.LENGTH_SHORT).show();
			}
		});
	}

	public void restartActivity() {
		Intent i = getBaseContext().getPackageManager().getLaunchIntentForPackage(getBaseContext().getPackageName());
		i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(i);
	}

	public void finishedLoading() {
		TextView freeboxUri = (TextView) findViewById(R.id.drawer_freebox_uri);
		freeboxUri.setText(fooBox.getFreebox().getDisplayUrl());
		findViewById(R.id.drawer_freebox).setVisibility(View.VISIBLE);
		
		appStarted = true;
		
		hideLoadingScreen();

		displayGraphs();
		refreshGraph();

		
		if (SettingsHelper.getInstance().getAutoRefresh())
			startRefreshThread();

		displayOpenSourceAlertIfNeeded();
	}
	
	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		drawerHelper.getToolbarIcon().syncState(savedInstanceState);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		drawerHelper.getToolbarIcon().onSaveInstanceState(outState);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		refreshMenuItem = menu.findItem(R.id.action_refresh);
		refreshMenuItem.setVisible(false);
		periodMenuItem = menu.findItem(R.id.menu_period);
		periodMenuItem.setVisible(false);
		validerMenuItem = menu.findItem(R.id.action_valider);
		validerMenuItem.setVisible(false);

		// Set menu labels
		menu.findItem(R.id.menu_period).setTitle(Period.HOUR.getLabel());
		menu.findItem(R.id.period_hour).setTitle(Period.HOUR.getLabel());
		menu.findItem(R.id.period_day).setTitle(Period.DAY.getLabel());
		menu.findItem(R.id.period_week).setTitle(Period.WEEK.getLabel());
		menu.findItem(R.id.period_month).setTitle(Period.MONTH.getLabel());

		drawerHelper.setupAnimatedIcon();
		
		return true;
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
		if (appStarted) {
			context = this;
			activity = this;
			
			refreshGraph();
			
			startRefreshThread();
		}
	}
	
	@Override
	public void onPause() {
		super.onPause();
		
		stopRefreshThread();
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		if (BillingService.isLoaded())
			BillingService.getInstance().unbind();
	}
	
	@Override
	public void onStart() {
		super.onStart();
		
		if (appStarted && findViewById(R.id.ll_loading).getVisibility() == View.VISIBLE) {
			graphsDisplayed = false;
			finishedLoading();
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				drawerHelper.toggleDrawer();
				break;
			case R.id.action_refresh:
				refreshGraph(true);
				break;
			case R.id.period_hour:
				fooBox.setPeriod(Period.HOUR);
				periodMenuItem.setTitle(Period.HOUR.getLabel());
				refreshGraph();
				break;
			case R.id.period_day:
				fooBox.setPeriod(Period.DAY);
				periodMenuItem.setTitle(Period.DAY.getLabel());
				refreshGraph();
				break;
			case R.id.period_week:
				fooBox.setPeriod(Period.WEEK);
				periodMenuItem.setTitle(Period.WEEK.getLabel());
				refreshGraph();
				break;
			case R.id.period_month:
				fooBox.setPeriod(Period.MONTH);
				periodMenuItem.setTitle(Period.MONTH.getLabel());
				refreshGraph();
				break;
			case R.id.action_valider:
				validerMenuItem.setVisible(false);
				refreshMenuItem.setVisible(true);
				periodMenuItem.setVisible(true);
				refreshGraph();
				break;
			default: super.onOptionsItemSelected(item); break;
		}
		return true;
	}

	private void displayOpenSourceAlertIfNeeded() {
		if (!Util.getPrefBoolean(this, "openSourceDialogShown", false) && !this.isFinishing()) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage(R.string.dialog_opensource)
					.setCancelable(true)
					.setPositiveButton(R.string.contribute, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialogInterface, int i) {
							startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/chteuchteu/Freebox-Stats/")));
						}
					})
					.setNegativeButton(R.string.close, null);
			builder.create().show();

			Util.setPref(this, "openSourceDialogShown", true);
		}
	}
}
