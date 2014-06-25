package com.need2.turnitup.beta;

import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.telephony.SmsManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.os.Build;

public class DonateActivity extends BaseActivity {
	static private final String donateURL = "https://www.canadahelps.org/dn/18287";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// enables the activity icon as a 'home' button
		getActionBar().setHomeButtonEnabled(true);
		setContentView(R.layout.activity_donate);
		
		Button textButton = (Button) findViewById(R.id.textButton);
		textButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// text NOISE to 45678
				
				Intent sendIntent = new Intent(Intent.ACTION_VIEW);
		        sendIntent.setData(Uri.parse("sms:"+"45678"));
		        sendIntent.putExtra("sms_body", "NOISE");
		        startActivity(sendIntent);				
			}
		});

		Button customButton = (Button) findViewById(R.id.customButton);
		customButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

				// display offline dialog to notify to users that their device
				// is not connected to the network
				if (!BaseActivity.isDeviceConnected) {
					TIUdialogs.startOfflineDeviceDialog(DonateActivity.this);
				} else {

					Intent i = new Intent(Intent.ACTION_VIEW);
					i.setData(Uri.parse(donateURL));
					startActivity(i);
				}

			}
		});

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.

		switch (item.getItemId()) {
		case android.R.id.home:
			Intent intent = new Intent(this, MainActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
}
