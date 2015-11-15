package com.chteuchteu.freeboxstats.hlpr;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.Spinner;

import com.chteuchteu.freeboxstats.FooBox;
import com.chteuchteu.freeboxstats.R;
import com.chteuchteu.freeboxstats.net.BillingService;
import com.chteuchteu.freeboxstats.obj.ErrorsLogger;
import com.chteuchteu.freeboxstats.obj.Freebox;
import com.chteuchteu.freeboxstats.ui.IMainActivity;
import com.chteuchteu.freeboxstats.ui.MainActivity;
import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DrawerHelper {
	private IMainActivity iActivity;
    private Activity activity;
	private Context context;
	private Toolbar toolbar;

	private HashMap<DrawerMenuItem, IDrawerItem> drawerItems;

	public enum DrawerMenuItem {
		Parametres(0),
		Coupures(1),
		Debug(2),
		Don(3);

		private int identifier;
		public int getIdentifier() { return this.identifier; }
		DrawerMenuItem(int identifier) { this.identifier = identifier; }
		public static DrawerMenuItem from(int identifier) {
			for (DrawerMenuItem item : DrawerMenuItem.values()) {
				if (item.getIdentifier() == identifier)
					return item;
			}
			return Parametres;
		}
	}


	public DrawerHelper(MainActivity activity, Toolbar toolbar) {
		this.iActivity = activity;
        this.activity = activity;
		this.context = activity;
		this.toolbar = toolbar;
		this.drawerItems = new HashMap<>();
	}

	public IDrawerItem getDrawerItem(DrawerMenuItem type) {
		return this.drawerItems.get(type);
	}

	public void initDrawer() {
		// Paramètres
		this.drawerItems.put(DrawerMenuItem.Parametres,
				new PrimaryDrawerItem()
						.withName(R.string.settings)
						.withIdentifier(DrawerMenuItem.Parametres.getIdentifier())
						.withIcon(CommunityMaterial.Icon.cmd_settings)
						.withSelectable(false)
		);

		// Coupures
		this.drawerItems.put(DrawerMenuItem.Coupures,
				new PrimaryDrawerItem()
						.withName(R.string.outages)
						.withIdentifier(DrawerMenuItem.Coupures.getIdentifier())
						.withIcon(CommunityMaterial.Icon.cmd_flash)
						.withSelectable(false)
		);

		// Debug
		this.drawerItems.put(DrawerMenuItem.Debug,
				new PrimaryDrawerItem()
						.withName(R.string.debug)
						.withIdentifier(DrawerMenuItem.Debug.getIdentifier())
						.withIcon(CommunityMaterial.Icon.cmd_help_circle)
						.withSelectable(false)
						.withEnabled(false)
		);

		// Don
		this.drawerItems.put(DrawerMenuItem.Don,
				new SecondaryDrawerItem()
						.withName(R.string.donate)
						.withIdentifier(DrawerMenuItem.Don.getIdentifier())
						.withIcon(CommunityMaterial.Icon.cmd_heart)
						.withSelectable(false)
		);

		DrawerBuilder builder = new DrawerBuilder()
				.withActivity(this.activity)
				.withToolbar(this.toolbar)
				.withSelectedItem(-1);

		// Add items
		builder.addDrawerItems(
				this.drawerItems.get(DrawerMenuItem.Parametres),
				this.drawerItems.get(DrawerMenuItem.Coupures),
				this.drawerItems.get(DrawerMenuItem.Debug),
				new DividerDrawerItem(),
				this.drawerItems.get(DrawerMenuItem.Don)
		);

		builder.withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
			@Override
			public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
				DrawerMenuItem item = DrawerMenuItem.from(drawerItem.getIdentifier());

				switch (item) {
					case Parametres: {
						LayoutInflater inflater = LayoutInflater.from(context);
						View dialog_layout = inflater.inflate(R.layout.settings_dialog, (ViewGroup) activity.findViewById(R.id.root_layout));

						// Auto refresh
						final CheckBox settings_autorefresh = (CheckBox) dialog_layout.findViewById(R.id.settings_autorefresh);
						settings_autorefresh.setChecked(SettingsHelper.getInstance().getAutoRefresh());

						// Display xDSL tab
						final CheckBox settings_displayXdslTab = (CheckBox) dialog_layout.findViewById(R.id.settings_displayxdsltab);
						settings_displayXdslTab.setChecked(SettingsHelper.getInstance().getDisplayXdslTab());

						// Display stack tab
						final CheckBox settings_displayStackTab = (CheckBox) dialog_layout.findViewById(R.id.settings_displayStackTab);
						settings_displayStackTab.setChecked(SettingsHelper.getInstance().getDisplayStackTab());

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

								boolean displayStackTabChanged = SettingsHelper.getInstance().getDisplayStackTab()
										!= settings_displayStackTab.isChecked();
								SettingsHelper.getInstance().setDisplayStackTab(settings_displayStackTab.isChecked());

								SettingsHelper.getInstance().setGraphPrecision(
										Enums.GraphPrecision.get(settings_graphPrecision.getSelectedItemPosition()));

								if (settings_autorefresh.isChecked())
									iActivity.startRefreshThread();
								else
									iActivity.stopRefreshThread();

								// Remove tab
								if (displayXdslTabChanged || displayStackTabChanged)
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
						return true;
					}

					case Coupures:
						iActivity.displayOutagesDialog();
						return true;

					case Don:
						donate();
						return true;

					case Debug: {
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
						return true;
					}

					default:
						return false;
				}
			}
		});


        // Freebox
		/*activity.findViewById(R.id.drawer_freebox).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				PopupMenu popupMenu = new PopupMenu(context, activity.findViewById(R.id.drawer_freebox));
				Menu menu = popupMenu.getMenu();
				popupMenu.getMenuInflater().inflate(R.menu.freebox, menu);
				popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
					@Override
					public boolean onMenuItemClick(MenuItem item) {
						switch (item.getItemId()) {
							case R.id.action_dissociate:
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
								return true;

							case R.id.action_options:
								LayoutInflater inflater = LayoutInflater.from(context);
								View dialog_layout = inflater.inflate(R.layout.freeboxsettings_dialog, (ViewGroup) activity.findViewById(R.id.root_layout));

								final RadioButton radioButtonIp = (RadioButton) dialog_layout.findViewById(R.id.radio_ip);
								RadioButton radioButtonLocal = (RadioButton) dialog_layout.findViewById(R.id.radio_local);

								final Freebox freebox = FooBox.getInstance().getFreebox();
								String ip = freebox.getIp();
								radioButtonIp.setText(ip == null || ip.equals("") ? context.getString(R.string.unknown) : ip);
								radioButtonLocal.setText(Freebox.ApiUri.substring("http://".length()));

								radioButtonIp.setChecked(freebox.getApiRemoteAccess() == Enums.SpecialBool.TRUE
										|| freebox.getApiRemoteAccess() == Enums.SpecialBool.UNKNOWN);
								radioButtonLocal.setChecked(freebox.getApiRemoteAccess() == Enums.SpecialBool.FALSE);

								AlertDialog.Builder builder2 = new AlertDialog.Builder(context);
								builder2.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog, int which) {
										freebox.setApiRemoteAccess(radioButtonIp.isChecked() ? Enums.SpecialBool.TRUE : Enums.SpecialBool.FALSE);
										try {
											freebox.save(context);
										} catch (JSONException ex) {
											ex.printStackTrace();
										}
										((TextView) activity.findViewById(R.id.drawer_freebox_uri)).setText(freebox.getDisplayUrl());
									}
								});
								builder2.setNegativeButton(R.string.cancel, null);
								builder2.setView(dialog_layout);
								builder2.show();
								return true;
						}
						return false;
					}
				});
				popupMenu.show();
			}
		});*/

		// Header
		AccountHeader header = new AccountHeaderBuilder()
				.withActivity(this.activity)
				.withCompactStyle(true)
				.addProfiles(
						new ProfileDrawerItem().withName("Freebox").withEmail("88888888").withIcon(CommunityMaterial.Icon.cmd_server)
				)
				.build();
		builder.withAccountHeader(header);

		builder.build();
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
