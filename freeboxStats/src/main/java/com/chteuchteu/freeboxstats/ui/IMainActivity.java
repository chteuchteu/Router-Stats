package com.chteuchteu.freeboxstats.ui;

import com.androidplot.xy.XYPlot;
import com.chteuchteu.freeboxstats.FooBox;
import com.chteuchteu.freeboxstats.hlpr.Enums;
import com.chteuchteu.freeboxstats.obj.GraphsContainer;

public interface IMainActivity {
    /* Actions results */
    void pairingFinished(Enums.AuthorizeStatus authorizeStatus);
    void sessionOpenFailed();
    void finishedLoading();
    void graphLoadingFailed();

    /* Actions */
    // App start
    void displayLoadingScreen();
    void hideLoadingScreen();
    void displayLaunchPairingScreen();
    void displayFreeboxUpdateNeededScreenBeforePairing();
    void displayFreeboxSearchFailedScreen();
    void displayFreeboxUpdateNeededScreen();

    void refreshGraph();
    void initPlot(XYPlot plot, FooBox.PlotType plotType);
    void displayDebugMenuItem();
    void startRefreshThread();
    void stopRefreshThread();
    void restartActivity();
    void displayOutagesDialog();
    void toggleProgressBar(boolean val);
    void loadGraph(FooBox.PlotType plotType, GraphsContainer graph, Enums.Period period, Enums.Unit unit);

    /* Setters */
    void setUpdating(boolean val);
}
