package com.need2.turnitup.beta;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

public class EventAlarmReciever extends BroadcastReceiver {

	final String USER_INFO = "UserInfoFile";
	public static WakeLock wakeLock = null;
	private NotificationGenerator notif = new NotificationGenerator();
	public static boolean isFromScreenOffFG;
	public static boolean isScreenOn;
	public static boolean isNotifSet;

	@Override
	public void onReceive(final Context context, Intent intent) {
		// clear up all the app flow control flags
		isFromScreenOffFG = false;
		isScreenOn = false;
		isNotifSet = false;

		PowerManager powerManager = (PowerManager) context
				.getSystemService(context.POWER_SERVICE);
		isScreenOn = powerManager.isScreenOn();
		wakeLock = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK
				| PowerManager.ACQUIRE_CAUSES_WAKEUP, "EventAlarmWakeLock");
		wakeLock.acquire();

		SharedPreferences prefs = context.getSharedPreferences("UserInfoFile",
				context.MODE_PRIVATE);

		// put flag in shared prefs so social media
		// screen doesn't appear
		// clear stored eventid
		SharedPreferences.Editor editor = prefs.edit();
		editor.putBoolean("attendingFlag", false);
		editor.putString("storedID", null);
		editor.putBoolean("isFromNotif", false);
		editor.commit();

		// Start MON event timer service in background when the MON event starts
		Intent countdownServiceIntent = new Intent(context,
				CountdownMoNService.class);

		// Copy eventID and userID from the previous activity
		// (CreateMinuteOfNoiseActivity/ TurnItUpActivity) intent and pass it to
		// CountdownMoNService
		countdownServiceIntent.putExtras(intent);
		context.startService(countdownServiceIntent);

		// check if joiner close and reopen the app
		// to display a proper event-start notification
		// It handles a special case when joiners joined an event and closed
		// down the the app before the event starts.
		if (TIUAppLifecycleHandler.resumed == 0 && isScreenOn) {
			TIUAppLifecycleHandler.isAppInBG = true;
		}

		// check for three possible conditions that may happen at the time
		// before event starts: device is in sleep mode, app is in BG, app in
		// FG, to navigate joiners to proper results
		if (!isScreenOn || TIUAppLifecycleHandler.isAppInBG) {

			// Display MON event screen in BG
			Intent joinMonIntent = new Intent(context,
					MinuteOfNoiseActivity.class);
			joinMonIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(joinMonIntent);

			notif.createEventStartNotice(context);
			isNotifSet = true;
			
			editor.putBoolean("isFromNotif", true);
			editor.commit();

		} else {

			// display event-start dialog if a join remains within the
			// application
			// when the event starts
			if (!TIUAppLifecycleHandler.isAppInBG) {

				Intent eventDialog = new Intent(context,
						DialogOfEventStartActivity.class);
				// Call startActivity() from outside of an Activity need to set
				// flag
				// to start the
				// DialogOfEventStartActivity as
				// the beginning of a new task on history stack
				eventDialog.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				context.startActivity(eventDialog);

			}
		}

	}

}
