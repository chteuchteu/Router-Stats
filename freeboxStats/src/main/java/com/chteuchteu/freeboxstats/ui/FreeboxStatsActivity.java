package com.chteuchteu.freeboxstats.ui;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.Window;
import android.view.WindowManager;

import com.chteuchteu.freeboxstats.R;
import com.chteuchteu.freeboxstats.hlpr.DrawerHelper;
import com.crashlytics.android.Crashlytics;

public class FreeboxStatsActivity extends ActionBarActivity {
    protected Context context;
    protected DrawerHelper drawerHelper;

    protected int viewToInflate;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Crashlytics.start(this);

        setContentView(viewToInflate);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        context = this;

        // Some design
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            int id = getResources().getIdentifier("config_enableTranslucentDecor", "bool", "android");
            if (id != 0 && getResources().getBoolean(id)) { // Translucent available
                Window w = getWindow();
                w.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION, WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
                w.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            }
        }
    }
}
