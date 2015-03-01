package com.chteuchteu.freeboxstats.hlpr;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.support.v4.widget.DrawerLayout;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.Spinner;

import com.balysv.materialmenu.MaterialMenuDrawable;
import com.balysv.materialmenu.extras.toolbar.MaterialMenuIconToolbar;
import com.chteuchteu.freeboxstats.R;
import com.chteuchteu.freeboxstats.obj.Freebox;
import com.chteuchteu.freeboxstats.ui.MainActivity;

public class DrawerHelper {
	private MainActivity activity;
	private boolean isDrawerOpened;
	private Context context;
	private DrawerLayout drawerLayout;
	private MaterialMenuIconToolbar materialMenu;

	public DrawerHelper(MainActivity activity, Context context) {
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
						settings_graphPrecision.setSelection(SettingsHelper.getInstance().getGraphPrecision().getIndex());
						dialog.dismiss();
					}
				});
				builder.setView(dialog_layout);
				builder.show();
			}
		});

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

		activity.findViewById(R.id.drawer_outages).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				drawerLayout.closeDrawers();
				activity.displayOutagesDialog();
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
}
