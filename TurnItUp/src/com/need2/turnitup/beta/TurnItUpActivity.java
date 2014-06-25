package com.need2.turnitup.beta;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class TurnItUpActivity extends BaseActivity implements
		OnItemSelectedListener {

	// Declare Variables

	Toast joinedToast;
	String userID;
	String eventID;
	String storedID;
	String filterChoice = "All Events";

	// setup vars for listview
	ListView listView;
	Integer imageId = R.drawable.teal_square;
	ArrayList<String> nameList;
	ArrayList<String> timeList;
	ArrayList<String> idList;

	ParseObject selectedEvent;
	// TODO might need progress dialog if takes long time to retrieve
	ArrayList<ParseObject> allEvents = new ArrayList<ParseObject>();

	// Join/create button is global so event list item's onClick can set it to
	// enabled
	Button joinButton;
	public static Button createButton;
	public static final String USER_INFO = "UserInfoFile";
	private NotificationGenerator notif = new NotificationGenerator();

	/** ON CREATE **/
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// put TIU screen in BG if app is closed before event starts and remains
		// in BG when event finishes.
		if (TIUAppLifecycleHandler.isAppInBG) {
			moveTaskToBack(true);
		}

		// enables the activity icon as a 'home' button
		getActionBar().setHomeButtonEnabled(true);

		setContentView(R.layout.turnitup);

		/** GENERATE SPINNER FOR FILTERING RESULTS **/
		Spinner spinner = (Spinner) findViewById(R.id.filter_spinner);
		// Create an ArrayAdapter using the string array and a default spinner
		// layout
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
				this, R.array.filter_array, R.layout.spinner_item);
		// Specify the layout to use when the list of choices appears
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// Apply the adapter to the spinner
		spinner.setAdapter(adapter);
		spinner.setOnItemSelectedListener(this);

		/** REFRESH BUTTON **/
		ImageButton refreshButton = (ImageButton) findViewById(R.id.refresh_button);
		refreshButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				refreshEvents();

			}
		});

		/** CREATE BUTTON **/
		createButton = (Button) findViewById(R.id.create_button);
		createButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				startCreateMinuteOfNoiseActivity();

			}
		});

		/** JOIN BUTTON **/
		joinButton = (Button) findViewById(R.id.join_button);
		joinButton.setEnabled(false);
		joinButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				handleJoinBtnPressed();

			}
		});

		// Refresh list of events
		refreshEvents();

	}

	protected void handleJoinBtnPressed() {
		if (!BaseActivity.isDeviceConnected) {
			TIUdialogs.startOfflineDeviceDialog(this);

		} else {
			String status = (String) joinButton.getText();

			if (status.equals("Join Event")) {
				startJoinerDialog();

			} else {
				startLeavingToast();

			}

		}

	}
	
	/** ON START **/
	@Override
	public void onStart() {
	
		super.onStart();
	}

	/** ON RESUME **/
	@Override
	public void onResume() {
		// Reload activity so you can get new events
		refreshEvents();
		super.onResume();

		new waitTask().execute();
	}

	/** ON PAUSE **/
	@Override
	public void onPause() {
		// Reload activity so you can get new events
		super.onPause();
	}

	@Override
	public void onBackPressed() {

		if (NotificationGenerator.isNoHistory) {
			Intent homeIntent = new Intent(this, MainActivity.class);
			homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK
					| Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(homeIntent);
			this.finish();
		}

		super.onBackPressed();
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		// remove event-start notification when a joiner enters the
		// MinuteOfNoiseActivity from outside of the app
		if (EventAlarmReciever.isNotifSet && hasFocus) {
			notif.notificationManager.cancel(0);
		}

		super.onWindowFocusChanged(hasFocus);
	}

	/** REFRESH THE EVENT LIST **/
	private void refreshEvents() {

		// make sure all events is empty to start with
		allEvents.clear();

		ParseQuery<ParseObject> query = ParseQuery.getQuery("Event");

		// Set up longitude and latitude
		double longitude = 0.0;
		double latitude = 0.0;

		// Get location of user to calculate distance between them and events
		try {
			LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
			Location location = lm
					.getLastKnownLocation(LocationManager.GPS_PROVIDER);
			longitude = location.getLongitude();
			latitude = location.getLatitude();

		} catch (NullPointerException ex) {
			// If unable to get location data, set filterChoice to 200m
			// to display nearby results
			filterChoice = "All Events";
		}

		// Create parse geo point
		final ParseGeoPoint point;
		point = new ParseGeoPoint(latitude, longitude);

		// If they have a filter on, filter the Parse query as well
		if (filterChoice.equals("Nearby Events")) {
			query.whereWithinKilometers("location", point, .5);
		}

		query.findInBackground(new FindCallback<ParseObject>() {
			public void done(List<ParseObject> objects, ParseException e) {
				if (e == null) {

					// Get userId, storedId from sharedpreferences
					SharedPreferences prefs = getSharedPreferences(USER_INFO,
							MODE_PRIVATE);
					storedID = prefs.getString("storedID", null);
					userID = prefs.getString("UID", null);
					Log.d("USERID", "USER ID: " + userID);

					// initialize empty lists for listview
					nameList = new ArrayList<String>();
					timeList = new ArrayList<String>();
					idList = new ArrayList<String>();

					// Retrieve event object from Parse.com database
					for (ParseObject event : objects) {

						// get variables of event for displaying in list
						String ID = (String) event.getObjectId();
						String eventName = (String) event.get("name");
						Date eventTime = (Date) event.get("dateTime");
						Date now = new Date();

						// If the event has already occurred, don't display it
						if (eventTime.after(now)) {

							// format date
							SimpleDateFormat formattedTime = new SimpleDateFormat(
									"h:mm a");

							nameList.add(eventName);
							timeList.add(formattedTime.format(eventTime));
							idList.add(ID);

							TextView timeDisplay = (TextView) findViewById(R.id.startTimeDisplay);
							if (ID.equals(storedID)) {
								timeDisplay.setText("Your event begins at "
										+ formattedTime.format(eventTime));
							} else if (storedID == null) {
								timeDisplay
										.setText("You are not in any events");
							}

							// Add to list of events to pull full object when
							// user selects it
							allEvents.add(event);
						}
					}

					// Pass the results into an Custom adapter
					CustomList adapter = new CustomList(TurnItUpActivity.this,
							nameList, timeList, idList, imageId);
					// Locate the listview in listview_main.xml
					listView = (ListView) findViewById(R.id.events_list);
					// Bind the Adapter to the ListView
					listView.setAdapter(adapter);

					listView.setOnItemClickListener(new OnItemClickListener() {
						public void onItemClick(AdapterView<?> parent,
								View view, int position, long id) {

							// Get storedId from sharedpreferences
							SharedPreferences prefs = getSharedPreferences(
									USER_INFO, MODE_PRIVATE);
							storedID = prefs.getString("storedID", null);

							try {
								// get event's objectID
								selectedEvent = allEvents.get(position);
								eventID = selectedEvent.getObjectId();

								// if they are already in event, they can leave
								// it
								if (eventID.equals(storedID)) {
									joinButton.setText("Leave Event");
									joinButton.setEnabled(true);

									// Otherwise let them join the event
								} else {

									// disable join button if user has already
									// joined an event
									if (storedID != null) {
										joinButton.setEnabled(false);

									} else {
										joinButton.setText("Join Event");
										joinButton.setEnabled(true);
									}

								}

							} catch (IndexOutOfBoundsException e) {
								refreshToast();
							}

						}

					});
				} else {
					// Failed to retrieve event
					final String LOGTAG = "ObjectRetrieval";
					Log.d(LOGTAG, "Object retrieval error: " + e);
				}
			}
		});

		// handle disabling create button
		SharedPreferences prefs = getSharedPreferences(USER_INFO, MODE_PRIVATE);
		String joinedEvent = prefs.getString("role", "G");

		if (joinedEvent.equals("G")) {
			createButton.setEnabled(true);

		} else {
			createButton.setEnabled(false);
		}

	}

	protected void refreshToast() {
		Toast refreshToast = Toast
				.makeText(
						this,
						"We're having trouble finding your event, please give us a second",
						Toast.LENGTH_SHORT);
		refreshToast.show();
	}

	protected void startLeavingToast() {
		Toast leaveToast = Toast.makeText(this, "You left the event!",
				Toast.LENGTH_SHORT);
		leaveToast.show();

		// Get userId from shared preferences
		SharedPreferences prefs = getSharedPreferences(USER_INFO, MODE_PRIVATE);
		final String userID = prefs.getString("UID", null);

		ParseQuery<ParseObject> query = ParseQuery.getQuery("Event");
		query.getInBackground(eventID, new GetCallback<ParseObject>() {

			@Override
			public void done(final ParseObject event, ParseException e) {
				if (e == null) {

					// remove userID of the leaving joiner from the event's
					// joiner list
					List<String> leavingJoinerParse = Arrays.asList(userID);
					event.removeAll("Joiners", leavingJoinerParse);
					event.saveInBackground(new SaveCallback() {

						@Override
						public void done(ParseException arg0) {

							// Remove the event when the last joiner left the
							// event
							// (reuse removeFinishedEvent() from the
							// CountdownMoNService class)
							List<String> currentJoinerList = event
									.getList("Joiners");
							if (currentJoinerList.isEmpty()) {
								CountdownMoNService
										.removeFinishedEvent(eventID);
							}

						}
					});

					Log.d("JoinerLeft", "event: " + eventID + "  " + userID);
				}
			}
		});

		// Cancel alarm that has been started
		Intent intent = new Intent(TurnItUpActivity.this,
				EventAlarmReciever.class);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(
				TurnItUpActivity.this, 0, intent,
				PendingIntent.FLAG_CANCEL_CURRENT);

		AlarmManager alarmMan = (AlarmManager) getSystemService(Activity.ALARM_SERVICE);
		alarmMan.cancel(pendingIntent);

		// clear stored eventid and transform Initiator/Joiner back to Guest
		storedID = null;
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString("storedID", storedID);
		editor.putString("role", "G");
		editor.commit();

		// Change button back to Join
		joinButton.setText("Join Event");
		joinButton.setEnabled(false);

		refreshEvents();

	}

	protected void startJoinerDialog() {
		// Get userId from shared preferences
		SharedPreferences prefs = getSharedPreferences(USER_INFO, MODE_PRIVATE);
		final String userID = prefs.getString("UID", null);

		final String eventID = selectedEvent.getObjectId();

		// Retrieve correct event from Parse
		ParseQuery<ParseObject> query = ParseQuery.getQuery("Event");
		query.getInBackground(eventID, new GetCallback<ParseObject>() {

			@SuppressWarnings("deprecation")
			public void done(ParseObject eventObject, ParseException e) {
				if (e == null) {

					// insert new joiner into the Event joiner list
					eventObject.add("Joiners", userID);
					eventObject.saveInBackground();

					// Get the selected event-start date/time
					Date eventStart = eventObject.getDate("dateTime");
					Log.d("eventStart", "" + eventStart);

					// Set the specific time on a Calendar to fire the event
					Calendar cal = Calendar.getInstance();
					cal.set(Calendar.DAY_OF_YEAR, eventStart.getYear());
					cal.set(Calendar.MONTH, eventStart.getMonth());
					cal.set(Calendar.DATE, eventStart.getDate());
					cal.set(Calendar.HOUR_OF_DAY, eventStart.getHours());
					cal.set(Calendar.MINUTE, eventStart.getMinutes());
					cal.set(Calendar.SECOND, 0);
					cal.set(Calendar.MILLISECOND, 0);

					// put eventID in storedID and transform Guest to Joiner
					storedID = eventID;
					SharedPreferences prefs = getSharedPreferences(USER_INFO,
							MODE_PRIVATE);
					SharedPreferences.Editor editor = prefs.edit();
					editor.putString("storedID", storedID);
					editor.commit();

					// Start clock alarm
					setAlarmEventStart(cal);

					SimpleDateFormat formattedTime = new SimpleDateFormat(
							"h:mm a");

					TextView timeDisplay = (TextView) findViewById(R.id.startTimeDisplay);
					timeDisplay.setText("Your event begins at "
							+ formattedTime.format(eventStart));

				} else {
					// Event doesn't exist

					// TODO Might need to handle something where two users join
					// at the exact same time
					// Mutex or something?
				}
			}
		});

		// transform Guest to Joiner
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString("role", "J");
		editor.commit();

		refreshEvents();

		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

		// Set the title on AlertDialog
		alertDialogBuilder.setTitle("Event joined!");

		alertDialogBuilder.setMessage(
				"You will be notified when the event starts.")
				.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {

						// Change button back to leave
						joinButton.setText("Leave Event");
						joinButton.setEnabled(false);

						dialog.dismiss();

					}
				});

		AlertDialog alertDialog = alertDialogBuilder.create();
		alertDialog.show();
	}

	// This method will set up an alarm to trigger the MinuteOfNoiseActivity
	// when it is at that time of the created event to start
	private void setAlarmEventStart(Calendar eventStart) {

		// Create a new PendingIntent and add it to the AlarmManager
		Intent intent = new Intent(TurnItUpActivity.this,
				EventAlarmReciever.class);
		intent.putExtra("runningEvent", eventID);
		intent.putExtra("currentJoiner", userID);

		PendingIntent pendingIntent = PendingIntent.getBroadcast(
				TurnItUpActivity.this, 0, intent,
				PendingIntent.FLAG_CANCEL_CURRENT);

		// Trigger MON event at the set time
		AlarmManager alarmMan = (AlarmManager) getSystemService(Activity.ALARM_SERVICE);
		alarmMan.set(AlarmManager.RTC_WAKEUP, eventStart.getTimeInMillis(),
				pendingIntent);

	}

	/** LAUNCH CREATE MINUTE OF NOISE ACTIVITY **/
	protected void startCreateMinuteOfNoiseActivity() {

		if (!BaseActivity.isDeviceConnected) {
			TIUdialogs.startOfflineDeviceDialog(this);

		} else {
			Intent createMonIntent = new Intent(TurnItUpActivity.this,
					CreateMinuteOfNoiseActivity.class);
			startActivity(createMonIntent);

		}

	}

	public void onItemSelected(AdapterView<?> parent, View view, int pos,
			long id) {

		filterChoice = parent.getItemAtPosition(pos).toString();
		refreshEvents();
	}

	public void onNothingSelected(AdapterView<?> parent) {
		filterChoice = "All Events";
	}

	private class waitTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... arg0) {

			new Timer().schedule(new TimerTask() {

				@Override
				public void run() {
					refreshEvents();

				}

			}, 1000);

			return null;
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

}
