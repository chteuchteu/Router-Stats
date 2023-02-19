package com.chteuchteu.freeboxstats.hlpr;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.RadioButton;

import com.chteuchteu.freeboxstats.FooBox;
import com.chteuchteu.freeboxstats.R;
import com.chteuchteu.freeboxstats.obj.ErrorsLogger;
import com.chteuchteu.freeboxstats.obj.Freebox;
import com.chteuchteu.freeboxstats.ui.MainActivity;
import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileSettingDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;

import org.json.JSONException;

public class DrawerHelper {
	private MainActivity activity;
	private Toolbar toolbar;
	private Context context;
	private DrawerLayout drawerLayout;

	private AccountHeader header;
	private Drawer drawer;

	private static final int Settings = 10000;
	private static final int Outages = 10001;
	private static final int Contribute = 10003;
	private static final int Debug = 10004;
	private static final int Freebox_Config = 10005;
	private static final int Freebox_Dissociate = 10006;

	public DrawerHelper(MainActivity activity, Toolbar toolbar) {
		this.activity = activity;
		this.context = activity;
		this.toolbar = toolbar;
		drawerLayout = (DrawerLayout) activity.findViewById(R.id.drawer_layout);
	}

	public void initDrawer() {
		DrawerBuilder builder = new DrawerBuilder()
				.withActivity(activity)
				.withToolbar(toolbar);

		PrimaryDrawerItem settings = new PrimaryDrawerItem()
				.withName(R.string.settings)
				.withIcon(CommunityMaterial.Icon.cmd_settings)
				.withSelectable(false)
				.withIdentifier(Settings);

		PrimaryDrawerItem outages = new PrimaryDrawerItem()
				.withName(R.string.outages)
				.withIcon(CommunityMaterial.Icon.cmd_flash)
				.withSelectable(false)
				.withIdentifier(Outages);

		PrimaryDrawerItem contribute = new SecondaryDrawerItem()
				.withName(R.string.contribute)
				.withIcon(CommunityMaterial.Icon.cmd_github_circle)
				.withSelectable(false)
				.withIdentifier(Contribute);

		builder.addDrawerItems(
				settings,
				outages,
				new DividerDrawerItem(),
				contribute
		);

		header = new AccountHeaderBuilder()
				.withActivity(activity)
				.withHeaderBackground(R.color.primary_dark)
				.withProfileImagesVisible(false)
				.withCompactStyle(true)
				.withOnAccountHeaderListener(new AccountHeader.OnAccountHeaderListener() {
					@Override
					public boolean onProfileChanged(View view, IProfile profile, boolean current) {
						switch ((int) profile.getIdentifier()) {
							case Freebox_Config:
								freeboxConfig();
								break;
							case Freebox_Dissociate:
								freeboxDissociate();
								break;
						}

						return false;
					}
				})
				.build();
		builder.withAccountHeader(header);

		builder.withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
			@Override
			public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
				switch ((int) drawerItem.getIdentifier()) {
					case Settings:
						settings();
						break;
					case Outages:
						activity.displayOutagesDialog();
						break;
					case Contribute:
						contribute();
						break;
					case Debug:
						debug();
						break;
					default:
						FooBox.log("Unknown drawer identifier " + drawerItem.getIdentifier());
				}

				return false;
			}
		});

		builder.withSelectedItem(-1);
		drawer = builder.build();
	}

	public void onFreeboxLoaded(Freebox freebox) {
		ProfileDrawerItem profile = new ProfileDrawerItem()
				.withName(context.getString(R.string.freebox))
				.withIcon(CommunityMaterial.Icon.cmd_router_wireless)
				.withEmail(freebox.getDisplayUrl());

		ProfileSettingDrawerItem manageFreebox = new ProfileSettingDrawerItem()
				.withName(context.getString(R.string.menu_config))
				.withIcon(CommunityMaterial.Icon.cmd_settings)
				.withIdentifier(Freebox_Config);

		ProfileSettingDrawerItem dissociate = new ProfileSettingDrawerItem()
				.withName(context.getString(R.string.menu_dissociate))
				.withIcon(CommunityMaterial.Icon.cmd_link_variant_off)
				.withIdentifier(Freebox_Dissociate);

		header.addProfiles(
				profile,
				manageFreebox,
				dissociate
		);
	}

	public void showDebug() {
		if (drawer.getDrawerItem(Debug) != null)
			return;

		PrimaryDrawerItem debug = new SecondaryDrawerItem()
				.withName(R.string.debug)
				.withIcon(CommunityMaterial.Icon.cmd_bug)
				.withSelectable(false)
				.withIdentifier(Debug);

		drawer.addItem(debug);
	}

	private void contribute() {
		activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(FooBox.REPO_URL)));
	}

	private void settings() {
		drawerLayout.closeDrawers();
		LayoutInflater inflater = LayoutInflater.from(context);
		View dialog_layout = inflater.inflate(R.layout.settings_dialog, (ViewGroup) activity.findViewById(R.id.root_layout));

		// Auto refresh
		final CheckBox settings_autorefresh = (CheckBox) dialog_layout.findViewById(R.id.settings_autorefresh);
		settings_autorefresh.setChecked(SettingsHelper.getInstance().getAutoRefresh());

		// Display xDSL tab
		final CheckBox settings_displayXdslTab = (CheckBox) dialog_layout.findViewById(R.id.settings_displayxdsltab);
		settings_displayXdslTab.setChecked(SettingsHelper.getInstance().getDisplayXdslTab());

		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				SettingsHelper.getInstance().setAutoRefresh(settings_autorefresh.isChecked());

				boolean displayXdslTabChanged = SettingsHelper.getInstance().getDisplayXdslTab()
						!= settings_displayXdslTab.isChecked();
				SettingsHelper.getInstance().setDisplayXdslTab(settings_displayXdslTab.isChecked());

				if (settings_autorefresh.isChecked())
					activity.startRefreshThread();
				else
					activity.stopRefreshThread();

				// Remove tab
				if (displayXdslTabChanged)
					activity.restartActivity();
			}
		});
		builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				settings_autorefresh.setChecked(SettingsHelper.getInstance().getAutoRefresh());
				dialog.dismiss();
			}
		});
		builder.setView(dialog_layout);
		builder.show();
	}

	private void freeboxConfig() {
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
					header.getActiveProfile().withEmail(freebox.getDisplayUrl());
				} catch (JSONException ex) {
					ex.printStackTrace();
				}
			}
		});
		builder2.setNegativeButton(R.string.cancel, null);
		builder2.setView(dialog_layout);
		builder2.show();
	}

	private void freeboxDissociate() {
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

	private void debug() {
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
}
