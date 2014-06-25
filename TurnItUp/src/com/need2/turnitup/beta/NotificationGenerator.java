package com.need2.turnitup.beta;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

@SuppressLint("NewApi")
public class NotificationGenerator {

	static boolean isNoHistory;
	static NotificationManager notificationManager;
	private String content = null;

	/*
	 * This method will set up an intent for event-start notification
	 */
	public void createEventStartNotice(Context context) {
		isNoHistory = false;
		content = "Minute of Noise event has started!";

		// Prepare intent which is triggered if the notification is selected
		Intent intent = new Intent(context, MinuteOfNoiseActivity.class);

		// check if the app is closed before event starts.
		if (TIUAppLifecycleHandler.resumed == 0) {

			isNoHistory = true;
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

		} else {
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
					| Intent.FLAG_ACTIVITY_SINGLE_TOP
					| Intent.FLAG_ACTIVITY_NEW_TASK
					| Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);

		}

		PendingIntent pIntent = PendingIntent.getActivity(context, 0, intent,
				PendingIntent.FLAG_CANCEL_CURRENT);
		generateNotification(context, pIntent);

	}

	/*
	 * This method will setup an intent for event-finish notification
	 */
	public void createEventFinishNotice(Context context) {
		content = "Minute of Noise event has completed!";
		Intent intent = null;

		if(MinuteOfNoiseActivity.enteredEvent){
			// Prepare intent which is triggered if the notification is selected
			intent = new Intent(context, MinuteOfNoiseActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
					| Intent.FLAG_ACTIVITY_SINGLE_TOP
					| Intent.FLAG_ACTIVITY_NEW_TASK
					| Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
			
		}else{
			intent = new Intent(context, TurnItUpActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
					| Intent.FLAG_ACTIVITY_SINGLE_TOP
					| Intent.FLAG_ACTIVITY_NEW_TASK
					| Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
			
		}

		PendingIntent pIntent = PendingIntent.getActivity(context, 0, intent,
				PendingIntent.FLAG_CANCEL_CURRENT);
		generateNotification(context, pIntent);
	}

	// This method will generate and display a notification for both start and finish event
	private void generateNotification(Context contex, PendingIntent pIntent) {

		Notification noti = new Notification.Builder(contex)
				.setContentTitle("Turn It Up").setContentText(content)
				.setSmallIcon(R.drawable.tiu_logo).setContentIntent(pIntent)
				.build();

		notificationManager = (NotificationManager) contex
				.getSystemService(contex.NOTIFICATION_SERVICE);
		// hide the notification after its selected
		noti.flags |= Notification.FLAG_AUTO_CANCEL;
		// Play default notification sound
		noti.defaults |= Notification.DEFAULT_SOUND;
		// Vibrate if vibrate is enabled
		noti.defaults |= Notification.DEFAULT_VIBRATE;

		notificationManager.notify(0, noti);

	}

}
