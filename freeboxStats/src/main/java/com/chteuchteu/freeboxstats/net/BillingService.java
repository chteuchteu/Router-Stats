package com.chteuchteu.freeboxstats.net;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;

import com.android.vending.billing.IInAppBillingService;

public class BillingService {
	private static BillingService instance;
	
	private IInAppBillingService mService;
	private ServiceConnection mServiceConn;
	private Activity activity;
	
	private boolean isBound = false;

	private static final int REQUEST_CODE = 1665;

	public static final String DONATE_2 = "donate_2";
	public static final String DONATE_5 = "donate_5";

	private ProgressDialog progressDialog;
	private String productToBuy;

	private BillingService(Activity activity, String product, ProgressDialog progressDialog) {
		this.productToBuy = product;
		this.progressDialog = progressDialog;
		loadInstance(activity);
	}
	
	private void loadInstance(final Activity activity) {
		this.activity = activity;
		
		mServiceConn = new ServiceConnection() {
			@Override
			public void onServiceDisconnected(ComponentName name) {
				mService = null;
			}
			@Override
			public void onServiceConnected(ComponentName name, IBinder service) {
				mService = IInAppBillingService.Stub.asInterface(service);
				
				launchPurchase(productToBuy);

				unbind();
			}
		};
		Intent intent = new Intent("com.android.vending.billing.InAppBillingService.BIND");
		intent.setPackage("com.android.vending");
		isBound = activity.bindService(intent, mServiceConn, Context.BIND_AUTO_CREATE);
	}
	
	private void launchPurchase(String product) {
		progressDialog.dismiss();

		try {
			Bundle buyIntentBundle = mService.getBuyIntent(3, activity.getPackageName(), product, "inapp", "");
			PendingIntent pendingIntent = buyIntentBundle.getParcelable("BUY_INTENT");
			activity.startIntentSenderForResult(
					pendingIntent.getIntentSender(),
					REQUEST_CODE, new Intent(), 0, 0, 0);
		}
		catch (Exception ex) { ex.printStackTrace(); }
	}

	public void unbind() {
		if (mService != null && isBound) {
			try {
				isBound = false;
				activity.unbindService(mServiceConn);
			} catch (Exception ex) { ex.printStackTrace(); }
		}
	}
	
	public static boolean isLoaded() { return instance != null; }
	public static BillingService getInstance() { return instance; }
	private void setProductToBuy(String val) { this.productToBuy = val; }
	private void setProgressDialog(ProgressDialog val) { this.progressDialog = val; }
	
	public static synchronized BillingService getInstanceAndPurchase(Activity activity, String product,
	                                                                 ProgressDialog progressDialog) {
		if (instance == null)
			instance = new BillingService(activity, product, progressDialog);
		else {
			// Instance already defined: just have to loadInstance again
			instance.setProductToBuy(product);
			instance.setProgressDialog(progressDialog);
			instance.loadInstance(activity);
		}
		return instance;
	}
}
