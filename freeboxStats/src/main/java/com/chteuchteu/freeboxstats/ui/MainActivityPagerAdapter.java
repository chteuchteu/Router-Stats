package com.chteuchteu.freeboxstats.ui;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.chteuchteu.freeboxstats.R;
import com.chteuchteu.freeboxstats.hlpr.SettingsHelper;

public class MainActivityPagerAdapter extends FragmentStatePagerAdapter {
    private Context context;

    public MainActivityPagerAdapter(FragmentManager fm, Context context) {
        super(fm);
        this.context = context;
    }

    @Override
    public Fragment getItem(int i) {
        // displayXdslTab = true:
        //  0       1     2       3       4
        // RATE | TEMP | XDSL | STACK | SWITCH
        // displayXdslTab = false:
        //  0       1     2        3
        // RATE | TEMP | STACK | SWITCH

        boolean displayXdslTab = SettingsHelper.getInstance().getDisplayXdslTab();
        boolean isRate = i == 0;
        boolean isTemp = i == 1;
        boolean isXdsl = displayXdslTab && i == 2;
        boolean isStack = (displayXdslTab && i == 3 || !displayXdslTab && i == 2);
        boolean isSwitch = (displayXdslTab && i == 4 || !displayXdslTab && i == 3);

        if (isRate)
            return new TwoGraphsFragment();
        else if (isTemp || isXdsl) {
            Fragment fragment = new GraphFragment();
            Bundle args = new Bundle();
            args.putString(GraphFragment.ARG_GRAPHTYPE, isTemp ? "temp" : "xdsl");
            fragment.setArguments(args);
            return fragment;
        }
        else if (isStack)
            return new StackFragment();
        else if (isSwitch)
            return new SwitchFragment();

        return null;
    }

    @Override
    public int getCount() {
        if (SettingsHelper.getInstance().getDisplayXdslTab())
            return MainActivity.NB_TABS;
        else
            return MainActivity.NB_TABS-1;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0: return context.getString(R.string.tab1_name);
            case 1: return context.getString(R.string.tab3_name);
            case 2:
                if (SettingsHelper.getInstance().getDisplayXdslTab())
                    return context.getString(R.string.tab4_name);
                else
                    return context.getString(R.string.tab5_name);
            case 3:
                if (SettingsHelper.getInstance().getDisplayXdslTab())
                    return context.getString(R.string.tab5_name);
                else
                    return context.getString(R.string.tab6_name);
            case 4:
                return context.getString(R.string.tab6_name);
            default: return "";
        }
    }
}
