package com.chteuchteu.freeboxstats.net;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.widget.Toast;

import com.android.vending.billing.IInAppBillingService;
import com.chteuchteu.freeboxstats.FooBox;
import com.chteuchteu.freeboxstats.MainActivity;
import com.chteuchteu.freeboxstats.R;
import com.chteuchteu.freeboxstats.ex.BillingServiceBadResponse;
import com.crashlytics.android.Crashlytics;

import java.util.ArrayList;

public class BillingService {
	private static BillingService instance;
	
	private IInAppBillingService mService;
	private ServiceConnection mServiceConn;
	private Context activityContext;
	
	private boolean isBound = false;
	
	public static final String ITEM_ID = "premium";
	public static final int REQUEST_CODE = 1664;
	
	private BillingService(Context activityContext) {
		loadInstance(activityContext);
	}
	
	private void loadInstance(final Context activityContext) {
		if (activityContext != null && this.activityContext == null)
			this.activityContext = activityContext;
		
		mServiceConn = new ServiceConnection() {
			@Override
			public void onServiceDisconnected(ComponentName name) {
				mService = null;
			}
			@Override
			public void onServiceConnected(ComponentName name, IBinder service) {
				mService = IInAppBillingService.Stub.asInterface(service);
				
				// Binding finished : check if premium
				boolean premium = checkIfHasPurchased();
				FooBox.getInstance().setIsPremium(premium);
				MainActivity.finishedLoading();
			}
		};
		Intent intent = new Intent("com.android.vending.billing.InAppBillingService.BIND");
		intent.setPackage("com.android.vending");
		isBound = activityContext.bindService(intent, mServiceConn, Context.BIND_AUTO_CREATE);
	}
	
	public void launchPurchase() {
		try {
			Bundle buyIntentBundle = mService.getBuyIntent(3, activityContext.getPackageName(), ITEM_ID, "inapp", "");
			PendingIntent pendingIntent = buyIntentBundle.getParcelable("BUY_INTENT");
			((Activity) activityContext).startIntentSenderForResult(pendingIntent.getIntentSender(),
					REQUEST_CODE, new Intent(), 0, 0, 0);
		}
		catch (Exception ex) { launchPurchase_retry(); }
	}
	
	public void launchPurchase_retry() {
		try {
			Bundle buyIntentBundle = mService.getBuyIntent(3, activityContext.getPackageName(), ITEM_ID, "inapp", "");
			PendingIntent pendingIntent = buyIntentBundle.getParcelable("BUY_INTENT");
			MainActivity.activity.startIntentSenderForResult(pendingIntent.getIntentSender(),
					REQUEST_CODE, new Intent(), 0, 0, 0);
		}
		catch (RemoteException | SendIntentException | NullPointerException ex) {
			ex.printStackTrace();
			Crashlytics.logException(ex);
			displayErrorToast();
		}
	}
	
	public void displayErrorToast() {
		Toast.makeText(activityContext, R.string.buying_failed, Toast.LENGTH_SHORT).show();
	}
	
	public boolean checkIfHasPurchased() {
		try {
			Bundle ownedItems = mService.getPurchases(3, activityContext.getPackageName(), "inapp", null);
			int response = ownedItems.getInt("RESPONSE_CODE");
			if (response == 0) {
				ArrayList<String> purchaseDataList = ownedItems.getStringArrayList("INAPP_PURCHASE_DATA_LIST");
				FooBox.log("Purchased items : " + purchaseDataList.toString());
				
				if (FooBox.DEBUG_INAPPPURCHASE)
					return true;
				return purchaseDataList.size() > 0;
			}
			else {
				Crashlytics.logException(new BillingServiceBadResponse("RESPONSE_CODE = " + response));
				return false;
			}
		} catch (RemoteException ex) {
			ex.printStackTrace();
			Crashlytics.logException(ex);
			return false;
		}
	}
	
	public void unbind() {
		if (mService != null && isBound) {
			try {
				isBound = false;
				activityContext.unbindService(mServiceConn);
			} catch (Exception ex) { ex.printStackTrace(); }
		}
	}
	
	public static boolean isLoaded() { return instance != null; }
	public static BillingService getInstance() { return instance; }
	
	public static synchronized BillingService getInstance(Context activityContext) {
		if (instance == null)
			instance = new BillingService(activityContext);
		return instance;
	}
}