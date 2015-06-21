package com.chteuchteu.freeboxstats.ui;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.chteuchteu.freeboxstats.R;
import com.chteuchteu.freeboxstats.hlpr.SettingsHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivityPagerAdapter extends FragmentStatePagerAdapter {
    private Context context;

    public MainActivityPagerAdapter(FragmentManager fm, Context context) {
        super(fm);
        this.context = context;
    }

    public enum Tab {
        RATE(R.string.rate),
        TEMP(R.string.temp_tab),
        XDSL(R.string.xdsl),
        STACK(R.string.stack),
        SWITCH(R.string.switch_tab);

        Tab(int tabTitle) { this.tabTitle = tabTitle; }

        private int tabTitle;
    }

    /**
     * Make a list of tabs, and remove hidden ones.
     * Way easier than previous pfusch
     */
    private static Tab getTab(int index) {
        List<Tab> tabs = new ArrayList<>();
        Collections.addAll(tabs, Tab.values());

        boolean displayXdslTab = SettingsHelper.getInstance().getDisplayXdslTab();
        boolean displayStackTab = SettingsHelper.getInstance().getDisplayStackTab();

        if (!displayXdslTab)
            tabs.remove(Tab.XDSL);

        if (!displayStackTab)
            tabs.remove(Tab.STACK);

        return tabs.get(index);
    }

    @Override
    public Fragment getItem(int i) {
        Tab tab = getTab(i);

        switch (tab) {
            case RATE:
                return new TwoGraphsFragment();
            case TEMP:
            case XDSL:
                Fragment fragment = new GraphFragment();
                Bundle args = new Bundle();
                args.putString(GraphFragment.ARG_GRAPHTYPE, tab == Tab.TEMP ? "temp" : "xdsl");
                fragment.setArguments(args);
                return fragment;
            case STACK:
                return new StackFragment();
            case SWITCH:
                return new SwitchFragment();
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        int nbTabs = MainActivity.NB_TABS;

        if (!SettingsHelper.getInstance().getDisplayStackTab())
            nbTabs--;

        if (!SettingsHelper.getInstance().getDisplayXdslTab())
            nbTabs--;

        return nbTabs;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return context.getString(getTab(position).tabTitle);
    }
}
