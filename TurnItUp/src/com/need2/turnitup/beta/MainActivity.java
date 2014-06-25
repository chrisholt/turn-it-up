package com.need2.turnitup.beta;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.GetCallback;
import com.parse.Parse;
import com.parse.ParseAnalytics;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

public class MainActivity extends BaseActivity {

	public static String userID = null;
	public static final String USER_INFO = "UserInfoFile";
	boolean isNotWelcome = false;
	public static boolean fromOffline = false;
	public static boolean fromMainAct = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Generate SharedPreferences file: where we will be storing/retrieving
		// values
		SharedPreferences sharedPref = getSharedPreferences(USER_INFO,
				MODE_PRIVATE);
		final String PREFTAG = "UID";

		// Check if a user ID hasn't been created and stored on phone
		// If it hasn't, create one
		if (sharedPref.getString(PREFTAG, null) == null) {
			setUpUser(sharedPref, PREFTAG);

		}
		// Set the layout
		setContentView(R.layout.activity_main);

		// Update odometer value
		new updateOdometer().execute();

		if (sharedPref.getString("notwelcome", null) == null) {
			displayAppWelcomeMsg();
		}

		// Setup the NEED2 message to link to the TIU site
		TextView need2Message = (TextView) findViewById(R.id.need2_message);
		need2Message.setMovementMethod(LinkMovementMethod.getInstance());

		/** TURN IT UP BUTTON **/
		// Declare and setup Turn It Up button
		Button tiuButton = (Button) findViewById(R.id.tiu_button);
		tiuButton.setOnClickListener(new OnClickListener() {

			// Call startTurnItUpActivity() when pressed
			@Override
			public void onClick(View v) {
				startTurnItUpActivity();
			}
		});

		/** MAKE NOISE BUTTON **/
		// Declare and setup Turn It Up button
		Button makeNoiseButton = (Button) findViewById(R.id.noiseGen_button);
		makeNoiseButton.setOnClickListener(new OnClickListener() {

			// Call startTurnItUpActivity() when pressed
			@Override
			public void onClick(View v) {
				startOfflineNoiseGenerator();
			}
		});

		/** YOUTHSPACE BUTTON **/
		// Declare and setup Youthspace Services button
		Button youthspaceButton = (Button) findViewById(R.id.youthspace_button);
		youthspaceButton.setOnClickListener(new OnClickListener() {

			// Call startYouthspaceServicesActivity() when pressed
			@Override
			public void onClick(View v) {
				startYouthspaceServicesActivity();
			}
		});

	}

	private void displayAppWelcomeMsg() {
		// create application welcome custom dialog
		final Dialog dialog = new Dialog(MainActivity.this);
		dialog.requestWindowFeature(Window.FEATURE_LEFT_ICON);
		dialog.setContentView(R.layout.appwelcome_dialog);
		dialog.setTitle("Welcome!");

		// LinearLayout container = (LinearLayout)
		// dialog.findViewById(R.id.welcome);
		// container.sets

		TextView welcome = (TextView) dialog
				.findViewById(R.id.home_welcome_msg);
		TextView tiu = (TextView) dialog.findViewById(R.id.home_welcome_msg);
		TextView noise = (TextView) dialog.findViewById(R.id.home_noise_msg);
		TextView youthspace = (TextView) dialog.findViewById(R.id.home_ys_msg);

		final CheckBox checkbox = (CheckBox) dialog
				.findViewById(R.id.checkBox1);

		Button ok_button = (Button) dialog.findViewById(R.id.customButton);
		ok_button.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				if (checkbox.isChecked()) {
					SharedPreferences sharedPref = getSharedPreferences(
							USER_INFO, MODE_PRIVATE);
					SharedPreferences.Editor editor = sharedPref.edit();
					editor.putString("notwelcome", "yes");
					editor.commit();

				}

				dialog.dismiss();

			}
		});

		dialog.show();
		dialog.setFeatureDrawableResource(Window.FEATURE_LEFT_ICON,
				R.drawable.tiu_logo);
	}

	/** SET UP USER **/
	// This method launches an Activity that gets and stores user information
	// for each user that starts up the application
	protected void setUpUser(final SharedPreferences sharedPref,
			final String tag) {

		final String LOGTAG = "UserInsert";
		final ParseObject userObject = new ParseObject("User");

		// Add a new user record into the User table
		// To retrieve the objectID, you need to save the object and
		// register for the save callback.
		// ObjectId doesnt't exist until a save operation is completed
		userObject.saveInBackground(new SaveCallback() {

			@Override
			public void done(ParseException e) {

				if (e == null) {
					// Saved new record successfully
					Log.d(LOGTAG, "User insert saved!");
					userID = userObject.getObjectId();

					// Add the newly generated userID to the SharedPreferences
					// file
					SharedPreferences.Editor editor = sharedPref.edit();
					editor.putString(tag, userID);
					editor.commit();

				} else {
					// The save failed
					Log.d(LOGTAG, "User insert error: " + e);
					Toast.makeText(
							MainActivity.this,
							"Server is unavailable at the moment \n"
									+ "Please try again later",
							Toast.LENGTH_LONG).show();

					// TODO gray out TIU button
				}
			}
		});

	}

	/** LAUNCH TURN IT UP ACTIVITY **/
	// This method will launch the TurnItUpActivity where Guests can become
	// Joiners/Initiators for MoN events
	protected void startTurnItUpActivity() {

		// Open SharedPreferences file: where we will bestoring/retrieving
		// values
		SharedPreferences sharedPref = getSharedPreferences(USER_INFO,
				MODE_PRIVATE);
		final String PLEDGETAG = "pledgeDisplayed";
		Activity thisActivity = this;

		// display offline dialog to notify to users that their device is not
		// connected to the network
		if (!BaseActivity.isDeviceConnected) {
			fromMainAct = true;
			TIUdialogs.startOfflineDeviceDialog(this);

		} else {

			// Check if the user hasn't been prompted with the pledge before
			// If user hasn't, then display AlertDialog for emailing pledge
			if (sharedPref.getString(PLEDGETAG, null) == null) {
				// Record pledge-from-Settings flag into the SharedPreferences
				// file to handle the cases properly after closing down the
				// pledge
				// dialog
				SharedPreferences.Editor editor = sharedPref.edit();
				editor.putString("fromSettingPledge", "no");
				editor.commit();

				TIUdialogs.startEmailPledge(sharedPref, PLEDGETAG,
						thisActivity, null);
			} else {

				// Start TIU main activity screen
				Intent tiuIntent = new Intent(MainActivity.this,
						TurnItUpActivity.class);
				startActivity(tiuIntent);
			}

		}
	}

	/** LAUNCH YOUTHSPACE ACTIVITY **/
	// This method will launch the YouthspaceServicesActivity where Guests
	// remain Guests and access Youthspace's help resources
	protected void startYouthspaceServicesActivity() {

		// Start the Youthspace Services Activty screen
		Intent youthspaceIntent = new Intent(MainActivity.this,
				YouthspaceServicesActivity.class);
		startActivity(youthspaceIntent);

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

	public class updateOdometer extends AsyncTask<String, Void, String> {

		@Override
		protected String doInBackground(String... params) {
			// Create HTTP Client
			HttpClient httpclient = new DefaultHttpClient();
			HttpGet httpget = new HttpGet("http://turnitup.ca");
			HttpResponse response;
			String countStr = "";

			try {
				response = httpclient.execute(httpget);
				HttpEntity entity = response.getEntity();

				// Create an InputStream with the response
				InputStream is = entity.getContent();
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(is, "iso-8859-1"), 8);
				StringBuilder sb = new StringBuilder();
				String line = null;

				// Read line by line
				while ((line = reader.readLine()) != null) {
					// when the odometer line has been found, get data
					if (line.contains("counterEndValue")) {
						Pattern p = Pattern.compile("[0-9]+");
						Matcher m = p.matcher(line);
						while (m.find()) {
							countStr = countStr + m.group();
						}
					}
				}

				// Close the stream
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

			// update odometer textView
			while (countStr.length() < 7) {
				countStr = 0 + countStr;
			}

			return countStr;
		}

		@Override
		protected void onPostExecute(String result) {
			TextView odoText = (TextView) findViewById(R.id.odometerText);
			odoText.setText(result);
		}
	}

	protected void startOfflineNoiseGenerator() {
		fromOffline = true;

		// Start offline NoiseGenerator screen
		Intent noiseIntent = new Intent(MainActivity.this,
				MinuteOfNoiseActivity.class);
		startActivity(noiseIntent);

	}

	@Override
	public void onResume() {
		super.onResume();
		fromOffline = false;
	}
}
