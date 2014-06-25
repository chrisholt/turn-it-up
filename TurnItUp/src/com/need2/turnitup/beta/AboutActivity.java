package com.need2.turnitup.beta;

import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.os.Build;

public class AboutActivity extends BaseActivity {

	private String need2URL = "http://need2.ca/";
	private String tiuURL = "http://turnitup.ca/";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// enables the activity icon as a 'home' button
		getActionBar().setHomeButtonEnabled(true);

		setContentView(R.layout.activity_about);
		
		/** SET UP Need2 IMAGEVIEW TO BE HYPERLINK **/
		ImageView need2Image = (ImageView) findViewById(R.id.about_need2);
		need2Image.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				// display offline dialog to notify to users that their device
				// is not connected to the network
				if (!BaseActivity.isDeviceConnected) {
					TIUdialogs.startOfflineDeviceDialog(AboutActivity.this);
				} else {

					Intent intent = new Intent();
					intent.setAction(Intent.ACTION_VIEW);
					intent.setData(Uri.parse(need2URL));
					startActivity(intent);

				}

			}
		});

		/** SET UP TIU IMAGEVIEW TO BE HYPERLINK **/
		ImageView logoImage = (ImageView) findViewById(R.id.about_tiu_logo);
		logoImage.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				// display offline dialog to notify to users that their device
				// is not connected to the network
				if (!BaseActivity.isDeviceConnected) {
					TIUdialogs.startOfflineDeviceDialog(AboutActivity.this);
				} else {

					Intent intent = new Intent();
					intent.setAction(Intent.ACTION_VIEW);
					intent.setData(Uri.parse(tiuURL));
					startActivity(intent);

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
