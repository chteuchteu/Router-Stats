package com.chteuchteu.freeboxstats.hlpr;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.widget.DrawerLayout;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.Spinner;

import com.balysv.materialmenu.MaterialMenuDrawable;
import com.balysv.materialmenu.extras.toolbar.MaterialMenuIconToolbar;
import com.chteuchteu.freeboxstats.FooBox;
import com.chteuchteu.freeboxstats.R;
import com.chteuchteu.freeboxstats.net.BillingService;
import com.chteuchteu.freeboxstats.obj.ErrorsLogger;
import com.chteuchteu.freeboxstats.obj.Freebox;
import com.chteuchteu.freeboxstats.ui.IMainActivity;
import com.chteuchteu.freeboxstats.ui.MainActivity;

import java.util.ArrayList;
import java.util.List;

public class DrawerHelper {
	private IMainActivity iActivity;
    private Activity activity;
	private boolean isDrawerOpened;
	private Context context;
	private DrawerLayout drawerLayout;
	private MaterialMenuIconToolbar materialMenu;

	public DrawerHelper(MainActivity activity, Context context) {
		this.iActivity = activity;
        this.activity = activity;
		this.context = context;
		drawerLayout = (DrawerLayout) activity.findViewById(R.id.drawer_layout);
	}

	public void initDrawer() {
		this.materialMenu = new MaterialMenuIconToolbar(activity, Color.WHITE, MaterialMenuDrawable.Stroke.THIN) {
			@Override public int getToolbarViewId() {
				return R.id.toolbar;
			}
		};
		this.materialMenu.setNeverDrawTouch(true);

		// Set font
		Util.Fonts.setFont(context, (ViewGroup) activity.findViewById(R.id.drawer), Util.Fonts.CustomFont.Roboto_Regular);

		// Settings
		activity.findViewById(R.id.drawer_settings).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				drawerLayout.closeDrawers();
				LayoutInflater inflater = LayoutInflater.from(context);
				View dialog_layout = inflater.inflate(R.layout.settings_dialog, (ViewGroup) activity.findViewById(R.id.root_layout));

				// Auto refresh
				final CheckBox settings_autorefresh = (CheckBox) dialog_layout.findViewById(R.id.settings_autorefresh);
				settings_autorefresh.setChecked(SettingsHelper.getInstance().getAutoRefresh());

				// Display xDSL tab
				final CheckBox settings_displayXdslTab = (CheckBox) dialog_layout.findViewById(R.id.settings_displayxdsltab);
				settings_displayXdslTab.setChecked(SettingsHelper.getInstance().getDisplayXdslTab());

				// Graph precision
				final Spinner settings_graphPrecision = (Spinner) dialog_layout.findViewById(R.id.settings_graphprecision);
				ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, Enums.GraphPrecision.getStringArray());
				adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
				settings_graphPrecision.setAdapter(adapter);
				settings_graphPrecision.setSelection(SettingsHelper.getInstance().getGraphPrecision().getIndex());

				AlertDialog.Builder builder = new AlertDialog.Builder(context);
				builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						SettingsHelper.getInstance().setAutoRefresh(settings_autorefresh.isChecked());
						boolean displayXdslTabChanged = SettingsHelper.getInstance().getDisplayXdslTab()
								!= settings_displayXdslTab.isChecked();
						SettingsHelper.getInstance().setDisplayXdslTab(settings_displayXdslTab.isChecked());
						SettingsHelper.getInstance().setGraphPrecision(
								Enums.GraphPrecision.get(settings_graphPrecision.getSelectedItemPosition()));

						if (settings_autorefresh.isChecked())
							iActivity.startRefreshThread();
						else
                            iActivity.stopRefreshThread();

						// Remove tab
						if (displayXdslTabChanged)
                            iActivity.restartActivity();
					}
				});
				builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
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

        // Freebox
		activity.findViewById(R.id.drawer_freebox).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				drawerLayout.closeDrawers();
				AlertDialog.Builder builder = new AlertDialog.Builder(context)
						.setMessage(R.string.dissociate)
						.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								Freebox.delete(context);
								Util.restartApp(context);
							}
						})
						.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								dialog.dismiss();
							}
						}).setIcon(android.R.drawable.ic_dialog_alert);
				// Avoid error when the app is closing or something
				if (!activity.isFinishing())
					builder.show();
			}
		});

        // Outages
		activity.findViewById(R.id.drawer_outages).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				drawerLayout.closeDrawers();
				iActivity.displayOutagesDialog();
			}
		});

        // Donate
		activity.findViewById(R.id.drawer_donate).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				drawerLayout.closeDrawers();
				donate();
			}
		});

        // Debug
        activity.findViewById(R.id.drawer_debug).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                LayoutInflater inflater = LayoutInflater.from(context);
                View dialog_layout = inflater.inflate(R.layout.debug_dialog, (ViewGroup) activity.findViewById(R.id.root_layout));

                final ListView lv = (ListView) dialog_layout.findViewById(R.id.debug_lv);
                ArrayAdapter<ErrorsLogger.AppError> arrayAdapter = new ArrayAdapter<>(context, android.R.layout.simple_list_item_1,
                        FooBox.getInstance().getErrorsLogger().getErrors());
                lv.setAdapter(arrayAdapter);

                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setPositiveButton(R.string.send_dev, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();

                        new AlertDialog.Builder(context)
                                .setTitle(R.string.send_errors)
                                .setMessage(R.string.send_errors_explanation)
                                .setPositiveButton(R.string.send, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        String txt = "Version de l'application : " + FooBox.getInstance().getAppVersion() + "\r\n"
                                                + "Freebox: " + Freebox.staticToString(FooBox.getInstance().getFreebox()) + "\r\n"
                                                + "API URL: " + FooBox.getInstance().getFreebox().getApiCallUrl() + "\r\n"
                                                + "\r\nListe des erreurs : \r\n"
                                                + FooBox.getInstance().getErrorsLogger().getErrorsString();
                                        Intent send = new Intent(Intent.ACTION_SENDTO);
                                        String uriText = "mailto:" + Uri.encode("chteuchteu@gmail.com") +
                                                "?subject=" + Uri.encode("Rapport de bug") +
                                                "&body=" + Uri.encode(txt);
                                        Uri uri = Uri.parse(uriText);

                                        send.setData(uri);
                                        activity.startActivity(Intent.createChooser(send, context.getString(R.string.send_errors)));
                                    }
                                })
                                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                })
                                .setIcon(R.drawable.ic_action_error_light)
                                .show();
                    }
                });
                builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.setTitle(R.string.debug);
                builder.setView(dialog_layout);
                // Avoid error when the app is closing or something
                if (!activity.isFinishing())
                    builder.show();
            }
        });
	}

	public MaterialMenuIconToolbar getToolbarIcon() { return this.materialMenu; }

	public void setupAnimatedIcon() {
		drawerLayout.setDrawerListener(new DrawerLayout.DrawerListener() {
			@Override
			public void onDrawerSlide(View view, float slideOffset) {
				materialMenu.setTransformationOffset(
						MaterialMenuDrawable.AnimationState.BURGER_ARROW,
						isDrawerOpened ? 2 - slideOffset : slideOffset);
			}

			@Override
			public void onDrawerOpened(View view) {
				isDrawerOpened = true;
				materialMenu.animatePressedState(MaterialMenuDrawable.IconState.ARROW);
			}

			@Override
			public void onDrawerClosed(View view) {
				isDrawerOpened = false;
				materialMenu.animatePressedState(MaterialMenuDrawable.IconState.BURGER);
			}

			@Override
			public void onDrawerStateChanged(int i) { }
		});
	}

	public void toggleDrawer() {
		if (isDrawerOpened)
			drawerLayout.closeDrawer(Gravity.START);
		else
			drawerLayout.openDrawer(Gravity.START);
	}

    @SuppressLint("InflateParams")
	private void donate() {
		new AlertDialog.Builder(activity)
				.setTitle(R.string.donate)
				.setMessage(R.string.donate_text)
				.setPositiveButton(R.string.donate, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        View view = inflater.inflate(R.layout.dialog_donate, null);

						final Spinner spinnerAmount = (Spinner) view.findViewById(R.id.donate_amountSpinner);
						List<String> list = new ArrayList<>();
						list.add("2 \u20Ac");
						list.add("5 \u20Ac");
						ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(activity, android.R.layout.simple_spinner_item, list);
						dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
						spinnerAmount.setAdapter(dataAdapter);

						new AlertDialog.Builder(activity)
								.setTitle(R.string.donate)
								.setView(view)
								.setPositiveButton(R.string.donate, new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog, int which) {
										// Launch BillingService, and then purchase the thing
										String product = "";
										switch (spinnerAmount.getSelectedItemPosition()) {
											case 0: product = BillingService.DONATE_2; break;
											case 1: product = BillingService.DONATE_5; break;
										}
										new DonateAsync(activity, product).execute();
									}
								})
								.setNegativeButton(R.string.cancel, null)
								.show();
					}
				})
				.setNegativeButton(R.string.cancel, null)
				.show();
	}

	private class DonateAsync extends AsyncTask<Void, Integer, Void> {
		private ProgressDialog dialog;
		private Activity activity;
		private String product;

		private DonateAsync(Activity activity, String product) {
			this.product = product;
			this.activity = activity;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();

			dialog = ProgressDialog.show(activity, "", activity.getString(R.string.loading), true);
			dialog.setCancelable(true);
		}

		@Override
		protected Void doInBackground(Void... arg0) {
			BillingService.getInstanceAndPurchase(activity, product, dialog);
			// Dialog will be dismissed in the BillingService.

			return null;
		}
	}
}
