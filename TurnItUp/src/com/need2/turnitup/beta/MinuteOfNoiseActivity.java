package com.need2.turnitup.beta;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;

public class MinuteOfNoiseActivity extends Activity implements
		OnItemSelectedListener {

	protected final Activity thisActivity = this;
	private String userID;
	private String eventID;
	private CountDownTimer localEventTimer;
	static private long localStartcountdown = -1;
	String filterChoice = "Drums";

	private SoundPool soundPool;

	// Sound resources
	static private int S1;
	static private int S2;
	static private int S3;
	static private int S4;
	static private int S5;
	static private int S6;

	// Sound IDs
	static private int S1_ID = 0;
	static private int S2_ID = 0;
	static private int S3_ID = 0;
	static private int S4_ID = 0;
	static private int S5_ID = 0;
	static private int S6_ID = 0;

	static private int currentStream = 0;

	public static final String USER_INFO = "UserInfoFile";
	private NotificationGenerator notif = new NotificationGenerator();

	static boolean isNotMovedToBG;
	static boolean enteredEvent;
	private int numbersOfEnteringMON = 0;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// clear enteredEvent flag
		enteredEvent = false;

		// handle the case where user shutdown app and turn screen off to force
		// it to BG
		if ((NotificationGenerator.isNoHistory || TIUAppLifecycleHandler.isAppInBG == false)
				&& !EventAlarmReciever.isScreenOn) {
			TIUAppLifecycleHandler.isAppInBG = true;
		}

		if (MainActivity.fromOffline) {
			TIUAppLifecycleHandler.isAppInBG = false;
		}

		// put MinuteOfNoiseActivity to BG if joiner reside outside of the app
		// in both sleep/awake mode of device while the event fires
		if (TIUAppLifecycleHandler.isAppInBG) {

			// set user to NOT entering the event in the first time that Noise
			// Generator screen is started when event starts in BG
			enteredEvent = false;
			moveTaskToBack(true);

		}

		setContentView(R.layout.mon_event);

		// set joiners to attending the event once they actually have entered
		// the MON activity
		// Set up shared prefs for flag editing
		// put flag in shared prefs so social media
		// screen does appear
		// Check to see what sound set the user has selected in their
		// preferences
		SharedPreferences prefs = getSharedPreferences("UserInfoFile",
				MODE_PRIVATE);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putBoolean("attendingFlag", true);
		editor.commit();

		/** GENERATE SPINNER FOR DIFFERENT SOUNDS **/
		Spinner spinner = (Spinner) findViewById(R.id.sound_spinner);
		// Create an ArrayAdapter using the string array and a default spinner
		// layout
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
				this, R.array.sound_array, R.layout.spinner_item);
		// Specify the layout to use when the list of choices appears
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// Apply the adapter to the spinner
		spinner.setAdapter(adapter);
		spinner.setOnItemSelectedListener(this);

		loadSounds();

		/** LEAVE **/
		// Declare and setup Leave button
		Button exitButton = (Button) findViewById(R.id.leave_button);

		if (MainActivity.fromOffline) {
			exitButton.setText("Return Home");
		} else {
			exitButton.setText("Leave Event");
		}

		exitButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				leaveEvent();
			}
		});

		/** SOUND CLIP 1 BUTTON **/

		Button clipOneButton = (Button) findViewById(R.id.sound_clip_1);
		clipOneButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// soundPool.stop(currentStream);
				currentStream = soundPool.play(S1_ID, 1, 1, 0, 0, 1);

			}
		});

		/** SOUND CLIP 2 BUTTON **/

		Button clipTwoButton = (Button) findViewById(R.id.sound_clip_2);
		clipTwoButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				currentStream = soundPool.play(S2_ID, 1, 1, 0, 0, 1);

			}
		});

		/** SOUND CLIP 3 BUTTON **/

		Button clipThreeButton = (Button) findViewById(R.id.sound_clip_3);
		clipThreeButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				currentStream = soundPool.play(S3_ID, 1, 1, 0, 0, 1);

			}
		});

		/** SOUND CLIP 4 BUTTON **/

		Button clipFourButton = (Button) findViewById(R.id.sound_clip_4);
		clipFourButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				currentStream = soundPool.play(S4_ID, 1, 1, 0, 0, 1);

			}
		});

		/** SOUND CLIP 5 BUTTON **/

		Button clipFiveButton = (Button) findViewById(R.id.sound_clip_5);
		clipFiveButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				currentStream = soundPool.play(S5_ID, 1, 1, 0, 0, 1);

			}
		});

		/** SOUND CLIP 6 BUTTON **/

		Button clipSixButton = (Button) findViewById(R.id.sound_clip_6);
		clipSixButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				currentStream = soundPool.play(S6_ID, 1, 1, 0, 0, 1);

			}
		});

		/** START MON COUNTDOWN **/
		// startCountDown();

	}

	/** LOAD SOUNDS **/
	private void loadSounds() {

		soundPool = new SoundPool(6, AudioManager.STREAM_MUSIC, 0);

		// Get the sound ID's ready for the sound set they have selected
		switch (filterChoice) {
		case "Drums":
			S1 = R.raw.s1_sound_1;
			S2 = R.raw.s1_sound_2;
			S3 = R.raw.s1_sound_3;
			S4 = R.raw.s1_sound_4;
			S5 = R.raw.s1_sound_5;
			S6 = R.raw.s1_sound_6;
			break;
		case "Heavy Bass":
			S1 = R.raw.s2_sound_1;
			S2 = R.raw.s2_sound_2;
			S3 = R.raw.s2_sound_3;
			S4 = R.raw.s2_sound_4;
			S5 = R.raw.s2_sound_5;
			S6 = R.raw.s2_sound_6;
			break;
		case "Voices":
			S1 = R.raw.s3_sound_1;
			S2 = R.raw.s3_sound_2;
			S3 = R.raw.s3_sound_3;
			S4 = R.raw.s3_sound_4;
			S5 = R.raw.s3_sound_5;
			S6 = R.raw.s3_sound_6;
			break;
		case "Random Noise":
			S1 = R.raw.s4_sound_1;
			S2 = R.raw.s4_sound_2;
			S3 = R.raw.s4_sound_3;
			S4 = R.raw.s4_sound_4;
			S5 = R.raw.s4_sound_5;
			S6 = R.raw.s4_sound_6;
			break;
		default:
			S1 = R.raw.s1_sound_1;
			S2 = R.raw.s1_sound_2;
			S3 = R.raw.s1_sound_3;
			S4 = R.raw.s1_sound_4;
			S5 = R.raw.s1_sound_5;
			S6 = R.raw.s1_sound_6;
		}

		S1_ID = soundPool.load(this, S1, 1);
		S2_ID = soundPool.load(this, S2, 1);
		S3_ID = soundPool.load(this, S3, 1);
		S4_ID = soundPool.load(this, S4, 1);
		S5_ID = soundPool.load(this, S5, 1);
		S6_ID = soundPool.load(this, S6, 1);

	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {

		if (hasFocus) {

			// remove event-start notification when a joiner enters the
			// MinuteOfNoiseActivity from outside of the app
			if (EventAlarmReciever.isNotifSet) {
				notif.notificationManager.cancel(0);
			}

			// track if user has enter the MON event
			enteredEvent = true;

		}

		super.onWindowFocusChanged(hasFocus);
	}

	/** PRESSING BACK TRIGGERS WARNING DIALOG **/
	@Override
	public void onBackPressed() {
		leaveEvent();
	}

	protected void leaveEvent() {

		if (MainActivity.fromOffline) {
			this.finish();

		} else {
			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
					this);

			// Set the title on AlertDialog
			alertDialogBuilder.setTitle("Warning!");

			alertDialogBuilder
					.setMessage("Do you really want to leave the event?")
					.setCancelable(false)
					.setPositiveButton("Yes",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {

									// stop BG timer service and clear/stop
									// local
									// event timer
									localStartcountdown = -1;
									CountdownMoNService.bgEventTimer.cancel();
									localEventTimer.cancel();

									// remove this joiner from event's joinerID
									// list
									removeEventJoiner();

									// Stop sounds from playing
									soundPool.stop(currentStream);
									soundPool.release();

									// clear stored eventid and transform
									// Initiator/Joiner back to Guest
									SharedPreferences prefs = getSharedPreferences(
											USER_INFO, MODE_PRIVATE);
									SharedPreferences.Editor editor = prefs
											.edit();
									editor.putString("storedID", null);
									editor.putString("role", "G");
									editor.commit();

									if (NotificationGenerator.isNoHistory) {
										Intent tiuIntent = new Intent(
												thisActivity,
												TurnItUpActivity.class);
										startActivity(tiuIntent);
									}

									dialog.cancel();
									thisActivity.finish();

									EventAlarmReciever.wakeLock.release();
									Log.d("WakeLock_end",
											"WakeLock is release. CPU went back to sleep!");

								}
							})
					.setNegativeButton("No",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {

									// Close dialog and return to MoN event
									// screen
									dialog.cancel();
								}
							});

			AlertDialog alertDialog = alertDialogBuilder.create();

			alertDialog.show();
		}

	}

	// This method will remove the joiner from the event's joiner list when the
	// joiner choose to leave in the middle of the ongoing event
	protected void removeEventJoiner() {

		ParseQuery<ParseObject> query = ParseQuery.getQuery("Event");
		query.getInBackground(eventID, new GetCallback<ParseObject>() {

			@Override
			public void done(final ParseObject event, ParseException e) {
				if (e == null) {

					// remove userID of the leaving joiner from the event's
					// joiner list
					List<String> joinerList = Arrays.asList(userID);
					event.removeAll("Joiners", joinerList);
					event.saveInBackground(new SaveCallback() {

						@Override
						public void done(ParseException arg0) {

							// Transform Initiator/Joiner back to Guest
							SharedPreferences prefs = getSharedPreferences(
									USER_INFO, MODE_PRIVATE);
							SharedPreferences.Editor editor = prefs.edit();
							editor.putString("role", "G");
							editor.commit();

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

				} else {
					// event doesn't exist :(
				}

			}
		});

	}

	/********** Event Countdown BroadcastReceiver ***********/
	@Override
	public void onResume() {
		super.onResume();

		// TODO register secCountdownReceiver to receive countdown tick in sec
		// from "countdown_event" intent in Service.
		LocalBroadcastManager.getInstance(this).registerReceiver(
				secCountdownReceiver, new IntentFilter("countdown_event"));

	}

	private BroadcastReceiver secCountdownReceiver = new BroadcastReceiver() {
		// TODO handler for the received "countdown_event" intent from Service.

		@Override
		public void onReceive(Context context, Intent intent) {

			// Extract the current tick (in second), eventID, userID from the
			// CountdownMoNService intent to start a local timer
			// Pull the timer tick from Service only once ( -1 indicates that
			// there is NO local timer running yet, so pull the start tick from
			// Service)
			if (localStartcountdown == -1) {

				Log.d("IntentOnReceive", " " + intent);

				localStartcountdown = intent.getLongExtra("secondCountdown", 0);
				eventID = intent.getStringExtra("runningEvent");
				userID = intent.getStringExtra("currentJoiner");

				Log.d("Event/userID", " " + eventID + " / " + userID);

				startLocalTimer();

			}

		}
	};

	@Override
	protected void onPause() {
		// Unregister secCountdownReceiver since the MON activity is not visible
		LocalBroadcastManager.getInstance(this).unregisterReceiver(
				secCountdownReceiver);
		super.onPause();
	}

	// This method will create and start a local ongoing-event timer for each
	// user who has
	// joined the event
	protected void startLocalTimer() {
		long localMilli = localStartcountdown * 1000;
		final TextView countdownTextField = (TextView) findViewById(R.id.countdown);

		localEventTimer = new CountDownTimer(localMilli, 1000) {

			@Override
			public void onTick(long millisUntilFinished) {

				// TODO Update the second tick displayed on MON activity screen
				Log.d("LocalCountdown", "  " + millisUntilFinished / 1000);

				countdownTextField.setText("" + millisUntilFinished / 1000);

			}

			@Override
			public void onFinish() {

				// Stop and release the sound resource, essentially destroyed
				// player when event is over
				soundPool.stop(currentStream);
				soundPool.release();

				// Clear start tick for local timer and indicate that no timer
				// running
				localStartcountdown = -1;

				displayEventCompleteDialog();

				// Transform Initiator/Joiner back to Guest
				SharedPreferences prefs = getSharedPreferences(USER_INFO,
						MODE_PRIVATE);
				SharedPreferences.Editor editor = prefs.edit();
				editor.putString("role", "G");
				editor.commit();

			}
		};

		// Start local event timer
		localEventTimer.start();

	}

	protected void displayEventCompleteDialog() {
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

		// Set the title on AlertDialog
		alertDialogBuilder.setTitle("Event is complated!");

		alertDialogBuilder
				.setMessage(
						"You have completed a Minute of Noise event! \n Would you like to tell your friends about the event?")
				.setCancelable(false)
				.setPositiveButton("Yes",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {

								Intent tiuIntent = new Intent(thisActivity,
										SocialMediaActivity.class);
								startActivity(tiuIntent);

								thisActivity.finish();

							}
						})
				.setNegativeButton("No", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {

						// restart a new TIU screen in case that user shutdown
						// the app causing whole history stack to be removed
						if (NotificationGenerator.isNoHistory) {
							Intent tiuIntent = new Intent(thisActivity,
									TurnItUpActivity.class);
							startActivity(tiuIntent);

						}

						// Close dialog and return to TIU screen
						// screen
						dialog.cancel();
						thisActivity.finish();
					}
				});

		AlertDialog alertDialog = alertDialogBuilder.create();

		alertDialog.show();

	}

	public void onItemSelected(AdapterView<?> parent, View view, int pos,
			long id) {

		filterChoice = parent.getItemAtPosition(pos).toString();
		loadSounds();
	}

	public void onNothingSelected(AdapterView<?> parent) {
		filterChoice = "Drums";
	}

	protected void displayFinsihedMsg() {
		Toast.makeText(this, "Event is completed!", Toast.LENGTH_LONG).show();
	}

}
