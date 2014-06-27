package com.need2.turnitup.beta;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

public class CountdownMoNService extends Service {

	static boolean eventIsOver;
	static CountDownTimer bgEventTimer;
	static final String USER_INFO = "UserInfoFile";
	private NotificationGenerator notif = new NotificationGenerator();
	public static boolean isFinishNotifSet = false;
	final Service thisService = this;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO start timer counting down in second for MON event

		eventIsOver = false;

		Log.d("TimerBG", "Timer of the event start in BG");

		startCountDown(intent);

		// Does not get restarted when background timer service is terminated
		// until a future explicit call to start the service
		return Service.START_NOT_STICKY;
	}

	@Override
	public void onDestroy() {
		EventAlarmReciever.wakeLock.release();
		Log.d("WakeLock_end", "WakeLock is release. CPU went back to sleep!");

		// Destroy the service by system if it is no longer used
		Log.v("EventTimerSERVICE", "Service killed");
		super.onDestroy();
	}

	private void startCountDown(final Intent countdownServiceIntent) {
		// TODO Starts a timer from 60 second and broadcast each tick in
		// second to MON activity

		final Intent intent = new Intent("countdown_event");
		intent.putExtras(countdownServiceIntent);

		bgEventTimer = new CountDownTimer(60000, 1000) {

			public void onTick(long millisUntilFinished) {
				long secondUnit = millisUntilFinished / 1000;
				Log.d("ServiceTimer_sec", " " + secondUnit);

				// Pass EventAlarmReceiverActivity's extras and each tick (in
				// second) to MinuteOfNoiseActivity
				intent.putExtra("secondCountdown", secondUnit);
				LocalBroadcastManager.getInstance(CountdownMoNService.this)
						.sendBroadcast(intent);
			}

			public void onFinish() {

				// get attending flag out of shared prefs (default to true)
				SharedPreferences prefs = getSharedPreferences(USER_INFO,
						MODE_PRIVATE);

				// ***************************************************************
				// display the event-finish notification for joiners who don't
				// enter the MONActivity until the event is over
				if (TIUAppLifecycleHandler.isAppInBG) {
					notif.createEventFinishNotice(thisService);
					isFinishNotifSet = true;

					// check if a user has entered the event before event is
					// completed, so the Noise Generator screen will be remove
					// out of the history stack and start TIU screen
					if (!MinuteOfNoiseActivity.enteredEvent) {

						// Start TIU screen in Background when event finish in
						// BG
						startTIUScreen();

					}
				}
				// ****************************************************************

				// remove the finished event object (whole record) from Event
				// class (Parse DB) by using the eventID extracted from the
				// EventAlarmReceiverActivity intent
				Bundle info = countdownServiceIntent.getExtras();
				String eventID = info.getString("runningEvent");
				removeFinishedEvent(eventID);

				String userID = prefs.getString("UID", null);
				incrementMON(userID);

				// Transform Initiator/Joiner back to Guest
				SharedPreferences.Editor editor = prefs.edit();
				editor.putString("role", "G");
				// clear stored eventid
				editor.putString("storedID", null);
				editor.commit();

				// track finished event to handle the case where joiner press OK
				// button of event-start dialog too late
				eventIsOver = true;

				// TODO stop and kill the service when the timer finishes
				// counting down
				stopService(countdownServiceIntent);

			}
		};

		bgEventTimer.start();

	}

	protected void startTIUScreen() {

		Intent tiuIntent = new Intent();
		tiuIntent.setClass(this, TurnItUpActivity.class);
		tiuIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK
				| Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(tiuIntent);

		NotificationGenerator.isNoHistory = true;

	}

	private void incrementMON(String userID) {
		// increment user's mon count
		ParseQuery<ParseObject> query = ParseQuery.getQuery("User");
		query.getInBackground(userID, new GetCallback<ParseObject>() {
			public void done(ParseObject user, ParseException e) {
				if (e == null) {
					user.increment("MONcounter");
					user.saveInBackground();
				}
			}
		});

		// increment global mon count
		ParseQuery<ParseObject> globalQuery = ParseQuery.getQuery("MoN_Count");
		globalQuery.getInBackground("EkYWtbYCt9",
				new GetCallback<ParseObject>() {
					public void done(ParseObject counter, ParseException e) {
						if (e == null) {
							counter.increment("totalMinutes");
							counter.saveInBackground();
						}
					}
				});
	}

	// This method will remove the event once the event is over OR all joiners
	// have left the event
	public static void removeFinishedEvent(final String eventID) {

		ParseQuery<ParseObject> query = ParseQuery.getQuery("Event");
		query.getInBackground(eventID, new GetCallback<ParseObject>() {

			@Override
			public void done(ParseObject event, ParseException e) {
				if (e == null) {
					event.deleteInBackground();
					
				} else {
					// event doesn't exist :(
				}

			}
		});
	}

	protected void displayFinishedMsg() {
		Toast.makeText(this, "Event is completed!", Toast.LENGTH_LONG).show();
	}
}
