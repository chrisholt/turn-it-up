package com.need2.turnitup.beta;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.parse.Parse;
import com.parse.ParseAnalytics;

public class YouthspaceServicesActivity extends BaseActivity {

	static private final String chatURL = "http://youthspace.ca/chat";
	static private final String forumURL = "http://youthspace.ca/index.php?action=user_login";
	static private final String phoneURL = "http://youthspace.ca/phone";

	/** ON CREATE **/
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// enables the activity icon as a 'home' button
		getActionBar().setHomeButtonEnabled(true);

		setContentView(R.layout.youthspace);
		

		/** CHAT BUTTON **/
		// Declare and setup Chat button
		Button chatButton = (Button) findViewById(R.id.chat_button);
		chatButton.setOnClickListener(new OnClickListener() {

			// Pass the chat URL to the browser when pressed
			@Override
			public void onClick(View v) {

				startBrowser(chatURL);

			}
		});

		/** FORUM BUTTON **/
		// Declare and setup Forum button
		Button forumButton = (Button) findViewById(R.id.forum_button);
		forumButton.setOnClickListener(new OnClickListener() {

			// Pass the forum URL to the browser when pressed
			@Override
			public void onClick(View v) {

				startBrowser(forumURL);

			}
		});

		/** PHONE DIRECTORY BUTTON **/
		// Declare and setup Phone Directory button
		Button phoneDirectoryButton = (Button) findViewById(R.id.phone_directory_button);
		phoneDirectoryButton.setOnClickListener(new OnClickListener() {

			// Pass the Phone Directory URL to the browser when pressed
			@Override
			public void onClick(View v) {

				startBrowser(phoneURL);

			}
		});
		
		
		//display offline dialog to notify to users that their device is not connected to the network
		if (!BaseActivity.isDeviceConnected) {
			TIUdialogs.startOfflineDeviceDialog(this);
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
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
		case R.id.donate:
			// User chose Donate, open the Donate activity
			startActivity(new Intent(this, DonateActivity.class));
			return true;
		case R.id.settings:
			// User chose Settings, open the Settings activity
			startActivity(new Intent(this, SettingsActivity.class));
			return true;
		case R.id.about:
			// User chose About, open the About activity
			startActivity(new Intent(this, AboutActivity.class));
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}

	}

	/** LAUNCH BROWSER **/
	protected void startBrowser(String URL) {

		Uri uri;
		Intent intent;

		switch (URL) {
		// Goto the chat in the browser
		case chatURL:
			uri = Uri.parse(chatURL);
			intent = new Intent(Intent.ACTION_VIEW, uri);
			startActivity(intent);
			break;
		// Goto the forum in the browser
		case forumURL:
			uri = Uri.parse(forumURL);
			intent = new Intent(Intent.ACTION_VIEW, uri);
			startActivity(intent);
			break;
		// Goto the phone directory in the browser
		case phoneURL:
			uri = Uri.parse(phoneURL);
			intent = new Intent(Intent.ACTION_VIEW, uri);
			startActivity(intent);
			break;

		default:

			break;
		}

	}

}
