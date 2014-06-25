package com.need2.turnitup.beta;


import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils.TruncateAt;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class SocialMediaActivity extends Activity {
	final private Activity thisActivity = this;
	private NotificationGenerator notif = new NotificationGenerator();
	static private final String twitterURL = "twitter://post?message=@need2turnitup I just made some noise for the Turn It Up Campaign! #NEED2 #TURNITUP";
	static private final String facebookText = "I just made some noise for the Turn It Up Campaign! #NEED2 #TURNITUP";
	static private final String googlePlusURL = "https://plus.google.com/";
	static private final String facebookURL = "https://facebook.com/";
	static final String USER_INFO = "UserInfoFile";

	/** ON CREATE **/
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// enables the activity icon as a 'home' button
		getActionBar().setHomeButtonEnabled(true);

		setContentView(R.layout.social_media_activity);

		// Create a shared preferences object to change role
		SharedPreferences prefs = getSharedPreferences(USER_INFO, MODE_PRIVATE);

		// Change role to G for guest
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString("role", "G");
		editor.commit();

		/** TWITTER BUTTON **/

		Button twitterButton = (Button) findViewById(R.id.twitter_button);
		twitterButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				startMedia(twitterURL);

			}
		});

		/** FACEBOOK BUTTON **/

		Button facebookButton = (Button) findViewById(R.id.facebook_button);
		facebookButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				startMedia(facebookText);

			}
		});

		/** GOOGLE PLUS BUTTON **/

		Button googlePlusButton = (Button) findViewById(R.id.gplus_button);
		googlePlusButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				startMedia(googlePlusURL);

			}
		});

		/** RETURN TO MAIN MENU BUTTON **/
		Button mainMenuButton = (Button) findViewById(R.id.main_menu_button);
		mainMenuButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// return to Main activity (homescreen) if the app has been
				// completely terminated before the event starts
				// because once the app is closed down, there is no previous
				// activities in the history stack,
				// So need to restart the MainActivity again.
				if (!TIUAppLifecycleHandler.isAppInBG) {
					Intent homeIntent = new Intent(thisActivity,
							MainActivity.class);
					homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK
							| Intent.FLAG_ACTIVITY_NEW_TASK);
					thisActivity.startActivity(homeIntent);

					Log.d("MMM", "start");
				}

				thisActivity.finish();

			}
		});

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

	@Override
	public void onBackPressed() {
		super.onBackPressed();

		if (NotificationGenerator.isNoHistory) {
			Intent tiuIntent = new Intent(this, TurnItUpActivity.class);
			startActivity(tiuIntent);

			Log.d("OOO", " " + NotificationGenerator.isNoHistory);
		}

		thisActivity.finish();
	}
	

	/** LAUNCH SOCIAL MEDIA APP **/
	protected void startMedia(String URL) {

		Intent intent;

		switch (URL) {

		case twitterURL:

			try {
				intent = new Intent(Intent.ACTION_VIEW);
				intent.setData(Uri.parse(twitterURL));
				startActivity(intent);

			} catch (android.content.ActivityNotFoundException e) {
				Toast.makeText(this,
						"Can't send tweet! Twitter app not found.",
						Toast.LENGTH_SHORT).show();
			}

			break;

		case googlePlusURL:

			try {
				intent = new Intent(Intent.ACTION_VIEW);
				intent.setData(Uri.parse(googlePlusURL));
				startActivity(intent);

			} catch (android.content.ActivityNotFoundException e) {
				Toast.makeText(this,
						"Can't make post! Google + app not found.",
						Toast.LENGTH_SHORT).show();
			}
			break;

		case facebookText:

			try {
				intent = new Intent(Intent.ACTION_VIEW);
				intent.setData(Uri.parse(facebookURL));
				startActivity(intent);

			} catch (android.content.ActivityNotFoundException e) {
				Toast.makeText(this,
						"Can't make post! Facebook app not found.",
						Toast.LENGTH_SHORT).show();
			}
			break;

		default:

			break;
		}

	}

}
