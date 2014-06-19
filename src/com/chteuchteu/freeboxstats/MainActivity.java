package com.chteuchteu.freeboxstats;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

import com.chteuchteu.freeboxstats.hlpr.Enums.AuthorizeStatus;
import com.chteuchteu.freeboxstats.net.AskForAppToken;

public class MainActivity extends ActionBarActivity {
	private static Context context;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		context = this;
		findViewById(R.id.firstLaunch_getAppToken).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				new AskForAppToken(SingleBox.getInstance().getFreebox()).execute();
			}
		});
		
		// Load singleton
		SingleBox.getInstance(this);
		SingleBox.getInstance(this).init();
	}
	
	public static void displayLaunchPairingButton() {
		((Activity) context).runOnUiThread(new Runnable() {
			@Override
			public void run() {
				((Activity) context).findViewById(R.id.firstLaunch).setVisibility(View.VISIBLE);
			}
		});
	}
	
	public static void pairingFinished(final AuthorizeStatus aStatus) {
		((Activity) context).runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(context, "Pairing terminé : " + aStatus.name() + ".", Toast.LENGTH_SHORT).show();
			}
		});
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
}
