package com.chteuchteu.freeboxstats.net;

import java.util.ArrayList;

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

import com.android.vending.billing.IInAppBillingService;
import com.chteuchteu.freeboxstats.FooBox;

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
				//FooBox.getInstance(activityContext).setIsPremium(checkIfHasPurchased());
			}
		};
		isBound = activityContext.bindService(new Intent("com.android.vending.billing.InAppBillingService.BIND"), mServiceConn,
				Context.BIND_AUTO_CREATE);
	}
	
	public void launchPurchase() {
		try {
			Bundle buyIntentBundle = mService.getBuyIntent(3, activityContext.getPackageName(), ITEM_ID, "inapp", "");
			PendingIntent pendingIntent = buyIntentBundle.getParcelable("BUY_INTENT");
			((Activity) activityContext).startIntentSenderForResult(pendingIntent.getIntentSender(),
					REQUEST_CODE, new Intent(), Integer.valueOf(0), Integer.valueOf(0), Integer.valueOf(0));
		}
		catch (RemoteException ex) { ex.printStackTrace(); }
		catch (SendIntentException ex) { ex.printStackTrace(); }
	}
	
	public boolean checkIfHasPurchased() {
		try {
			Bundle ownedItems = mService.getPurchases(3, activityContext.getPackageName(), "inapp", null);
			int response = ownedItems.getInt("RESPONSE_CODE");
			if (response == 0) {
				ArrayList<String> purchaseDataList = ownedItems.getStringArrayList("INAPP_PURCHASE_DATA_LIST");
				FooBox.log("Purchased items : " + purchaseDataList.toString());
				return purchaseDataList.size() > 0;
			}
			else
				return false;
		} catch (RemoteException ex) {
			ex.printStackTrace();
			return false;
		}
	}
	
	public void unbind() {
		if (mService != null && isBound)
			activityContext.unbindService(mServiceConn);
	}
	
	public static boolean isLoaded() { return instance != null; }
	
	public static BillingService getInstance() { return instance; }
	
	public static synchronized BillingService getInstance(Context activityContext) {
		if (instance == null)
			instance = new BillingService(activityContext);
		return instance;
	}
}