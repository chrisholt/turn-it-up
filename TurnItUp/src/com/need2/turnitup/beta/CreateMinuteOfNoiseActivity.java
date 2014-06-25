package com.need2.turnitup.beta;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;

public class CreateMinuteOfNoiseActivity extends FragmentActivity {

	public static String userID;
	public static String eventID;
	EditText eventName;
	EditText email;
	static TextView timeDisplay;
	Button timePick;
	Button createButton;
	String eventNameStr = null;
	String emailStr = null;
	static Calendar cal;

	public static final String USER_INFO = "UserInfoFile";

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// enables the activity icon as a 'home' button
		getActionBar().setHomeButtonEnabled(true);

		final Boolean isRequired = true;
		final Boolean notRequired = false;

		// Set the layout
		setContentView(R.layout.create_mon_activity);

		eventName = (EditText) findViewById(R.id.event_name);
		email = (EditText) findViewById(R.id.email);

		timeDisplay = (TextView) findViewById(R.id.time_display);
		Date now = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("h:mm a");
		String formattedTime = sdf.format(now);
		timeDisplay.setText(formattedTime);

		/** FINISH BUTTON **/
		// Declare and setup create event button
		createButton = (Button) findViewById(R.id.finish_button);
		createButton.setOnClickListener(new OnClickListener() {

			// Create the MoN Event when pressed
			@Override
			public void onClick(View v) {
				boolean inputError = false;

				if (validateEachInput(eventName, isRequired, "eventname")) {
					eventNameStr = eventName.getEditableText().toString()
							.trim();
				} else {
					inputError = true;
				}

				if (validateEachInput(email, notRequired, "email")) {
					emailStr = email.getEditableText().toString().trim();
				} else {
					inputError = true;
				}

				// If eventName and email are both validated, create the event
				// and back out of the activity
				if (!inputError) {
					
					// check for available an Internet connection before creating an event
					if (!detectDeviceActiveNetwork()) {
						displayOfflineDeviceDialog();
						
					}else{
						createEvent();
						
						// Transform Guest to Initiator
						SharedPreferences prefs = getSharedPreferences(
								USER_INFO, MODE_PRIVATE);
						SharedPreferences.Editor editor = prefs.edit();
						editor.putString("role", "I");
						editor.commit();
						
						finish();
						
					}
				}
			}
		});

		/** TIME PICKER BUTTON **/
		// Declare and setup timePicker button
		timePick = (Button) findViewById(R.id.time_picker);
		timePick.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				// Show the Time Picker
				DialogFragment newFragment = new TimePickerFragment();
				newFragment.show(getSupportFragmentManager(), "timePicker");

			}
		});
	}

	protected void displayOfflineDeviceDialog() {
		TIUdialogs.startOfflineDeviceDialog(this);
		
	}

	protected boolean detectDeviceActiveNetwork() {
		
		ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = cm.getActiveNetworkInfo();

		// check if a user's device is connected if there is active network
		// available
		if (netInfo != null) {
			return netInfo.isConnected();
		}

		return false;
	}

	// go to timePicker if blue clock is clicked
	public void timeClick(View v) {
		// Show the Time Picker
		DialogFragment newFragment = new TimePickerFragment();
		newFragment.show(getSupportFragmentManager(), "timePicker");
	}

	// This method will validate inputs from a user
	protected boolean validateEachInput(EditText editTextBox, Boolean required,
			String tag) {
		boolean ret = true;

		// Validate email ,which is optional, and must match general email
		// pattern
		if (tag.equals("email")) {
			if (!required && !Validation.hasText(editTextBox)) {
				ret = true;
			} else {
				if (!Validation.isEmailAddress(editTextBox, required)) {
					ret = false;
				}
			}

			// Validate event name
		} else {

			if (!Validation.isEventName(editTextBox, required)) {
				ret = false;
			}

		}

		return ret;

	}

	protected void createEvent() {
		// Get userId from shared preferences
		final SharedPreferences prefs = getSharedPreferences(USER_INFO,
				MODE_PRIVATE);
		userID = prefs.getString("UID", null);

		// Get the time from form
		int hour = cal.get(Calendar.HOUR_OF_DAY);
		int minute = cal.get(Calendar.MINUTE);
		int AM_orPM = cal.get(Calendar.AM_PM);

		// Set the event time
		Date eventStart = new Date();
		eventStart.setHours(hour);
		eventStart.setMinutes(minute);

		double longitude;
		double latitude;

		// Get location of user
		try {
			LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
			Location location = lm
					.getLastKnownLocation(LocationManager.GPS_PROVIDER);
			longitude = location.getLongitude();
			latitude = location.getLatitude();

		} catch (NullPointerException e) {
			// If unable to get location data, set lat and long to 0
			longitude = 0.0;
			latitude = 0.0;
		}
		// Put location in a ParseGeoPoint object
		ParseGeoPoint point = new ParseGeoPoint(latitude, longitude);

		// Push event data to Parse
		final ParseObject eventObject = new ParseObject("Event");
		eventObject.put("name", eventNameStr);
		eventObject.put("dateTime", eventStart);
		eventObject.put("initiatorID", userID);
		eventObject.put("location", point);
		eventObject.add("Joiners", userID);

		eventObject.saveInBackground(new SaveCallback() {
			public void done(ParseException e) {
				if (e == null) {
					// Saved event

					// Get eventID for removing finished event Object later on
					eventID = eventObject.getObjectId();

					// put eventID into storedID
					SharedPreferences prefs = getSharedPreferences(USER_INFO,
							MODE_PRIVATE);
					SharedPreferences.Editor editor = prefs.edit();
					editor.putString("storedID", eventID);
					editor.commit();

					// Save event start time locally
					setAlarmEventStart(cal);
				}
			}
		});
	}

	public static class TimePickerFragment extends DialogFragment implements
			TimePickerDialog.OnTimeSetListener {

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			// Use the current time as the default values for the picker
			cal = Calendar.getInstance();
			int hour = cal.get(Calendar.HOUR_OF_DAY);
			int minute = cal.get(Calendar.MINUTE);

			// Create a new instance of TimePickerDialog and return it
			return new TimePickerDialog(getActivity(), this, hour, minute,
					DateFormat.is24HourFormat(getActivity()));
		}

		public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

			// Set time in calendar
			cal.set(Calendar.HOUR_OF_DAY, hourOfDay);
			cal.set(Calendar.MINUTE, minute);
			cal.set(Calendar.SECOND, 0);

			Date eventStart = new Date();
			eventStart.setHours(hourOfDay);
			eventStart.setMinutes(minute);

			// Display the time user has selected
			SimpleDateFormat sdf = new SimpleDateFormat("h:mm a");
			String formattedTime = sdf.format(eventStart);
			timeDisplay.setText(formattedTime);
		}
	}

	// This method will set up an alarm to trigger the MinuteOfNoiseActivity
	// when the event start time occurs
	private void setAlarmEventStart(Calendar eventStart) {

		// Create a new PendingIntent and add it to the AlarmManager
		Intent intent = new Intent(CreateMinuteOfNoiseActivity.this,
				EventAlarmReciever.class);
		// Pass eventID and userID to CountdownMoNService for removing the
		// finished event and leaving of joiner
		intent.putExtra("runningEvent", eventID);
		intent.putExtra("currentJoiner", userID);

		PendingIntent pendingIntent = PendingIntent.getBroadcast(
				CreateMinuteOfNoiseActivity.this, 0, intent,
				PendingIntent.FLAG_CANCEL_CURRENT);

		// Trigger MON event at the set time
		AlarmManager alarmMan = (AlarmManager) getSystemService(Activity.ALARM_SERVICE);
		alarmMan.set(AlarmManager.RTC_WAKEUP, eventStart.getTimeInMillis(),
				pendingIntent);
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

}
