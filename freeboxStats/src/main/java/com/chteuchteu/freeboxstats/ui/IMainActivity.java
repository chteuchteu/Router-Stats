package com.chteuchteu.freeboxstats.ui;

import com.androidplot.xy.XYPlot;
import com.chteuchteu.freeboxstats.FooBox;
import com.chteuchteu.freeboxstats.hlpr.Enums;
import com.chteuchteu.freeboxstats.obj.GraphsContainer;

public interface IMainActivity {
    /* Actions results */
    public void pairingFinished(Enums.AuthorizeStatus authorizeStatus);
    public void sessionOpenFailed();
    public void finishedLoading();
    public void graphLoadingFailed();

    /* Actions */
    // App start
    public void displayLoadingScreen();
    public void hideLoadingScreen();
    public void displayLaunchPairingScreen();
    public void displayFreeboxUpdateNeededScreenBeforePairing();
    public void displayFreeboxSearchFailedScreen();
    public void displayFreeboxUpdateNeededScreen();

    public void refreshGraph();
    public void initPlot(XYPlot plot, FooBox.PlotType plotType);
    public void displayDebugMenuItem();
    public void startRefreshThread();
    public void stopRefreshThread();
    public void restartActivity();
    public void displayOutagesDialog();
    public void toggleSpinningMenuItem(boolean val);
    public void loadGraph(FooBox.PlotType plotType, GraphsContainer graph, Enums.Period period, Enums.Unit unit);

    /* Setters */
    public void setUpdating(boolean val);
}
